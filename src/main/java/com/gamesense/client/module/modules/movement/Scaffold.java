package com.gamesense.client.module.modules.movement;

import com.gamesense.api.event.events.PacketEvent;
import com.gamesense.api.event.events.PlayerJumpEvent;
import com.gamesense.api.event.events.PlayerMoveEvent;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.misc.MessageBus;
import com.gamesense.api.util.player.PlacementUtil;
import com.gamesense.api.util.player.PlayerUtil;
import com.gamesense.api.util.player.PredictUtil;
import com.gamesense.api.util.player.RotationUtil;
import com.gamesense.api.util.world.BlockUtil;
import com.gamesense.api.util.world.MotionUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.ModuleManager;
import com.gamesense.client.module.modules.gui.ColorMain;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;

/**
 * @author Doogie13
 * */

@Module.Declaration(name = "Scaffold", category = Category.Movement)
public class Scaffold extends Module {

    ModeSetting logic = registerMode("Place Logic", Arrays.asList("Predict", "Player"), "Predict");

    IntegerSetting distance = registerInteger("Distance Predict", 2, 0, 20, () -> logic.getValue().equalsIgnoreCase("Predict"));
    IntegerSetting distanceP = registerInteger("Distance Player", 2, 0, 20, () -> logic.getValue().equalsIgnoreCase("Player"));
    ModeSetting towerMode = registerMode("Tower Mode", Arrays.asList("Jump", "Motion", "FakeJump", "None"), "Motion");DoubleSetting downSpeed = registerDouble("DownSpeed", 0, 0, 0.2);
    IntegerSetting delay = registerInteger("Jump Delay", 2,1,10, () -> towerMode.getValue().equalsIgnoreCase("FakeJump"));
    BooleanSetting rotate = registerBoolean("Rotate", false);

    int timer;

    int oldSlot;
    int newSlot;

    double oldTower;

    EntityPlayer predPlayer;

    BlockPos scaffold;
    BlockPos towerPos;
    BlockPos downPos;
    BlockPos rotateTo;

    @Override
    protected void onEnable() {
        timer = 0;
    }

    @EventHandler
    private final Listener<PlayerMoveEvent> moveEventListener = new Listener<>(event -> {

        oldSlot = mc.player.inventory.currentItem;

        towerPos = new BlockPos(mc.player.posX, mc.player.posY - 1, mc.player.posZ);
        downPos = new BlockPos(mc.player.posX, mc.player.posY - 2, mc.player.posZ);


        if (logic.getValue().equalsIgnoreCase("Predict")) {

            PredictUtil.PredictSettings predset = new PredictUtil.PredictSettings((Integer) (distance.getValue()), false, 0, 0, 0, 0, 0, 0, false, 0, false, false, false, false, false, 0, 696969);

            predPlayer = PredictUtil.predictPlayer(mc.player, predset);

            scaffold = (new BlockPos(predPlayer.posX, predPlayer.posY - 1, predPlayer.posZ));

        } else if (logic.getValue().equalsIgnoreCase("Player")) {

            double[] dir = MotionUtil.forward(MotionUtil.getMotion(mc.player) * distanceP.getValue());

            scaffold = new BlockPos(mc.player.posX + dir[0], mc.player.posY, mc.player.posZ + dir[1]).down();

        }

        // Courtesy of KAMI, this block finding algo
        newSlot = -1;
        if (!Block.getBlockFromItem(mc.player.getHeldItemMainhand().item).getDefaultState().isFullBlock()) {
            for (int i = 0; i < 9; i++) {
                // filter out non-block items
                ItemStack stack =
                        mc.player.inventory.getStackInSlot(i);

                if (stack == ItemStack.EMPTY || !(stack.getItem() instanceof ItemBlock)) {
                    continue;
                }

                // filter out non-solid blocks
                if (!Block.getBlockFromItem(stack.getItem()).getDefaultState()
                        .isFullBlock())
                    continue;

                // don't use falling blocks if it'd fall
                if (((ItemBlock) stack.getItem()).getBlock() instanceof BlockFalling) {
                    if (mc.world.getBlockState(scaffold).getMaterial().isReplaceable()) continue;
                }

                newSlot = i;
                break;
            }
        } else {
            newSlot = mc.player.inventory.currentItem;
        }

        if (newSlot == -1) {

            newSlot = 1;

            MessageBus.sendClientPrefixMessage(ModuleManager.getModule(ColorMain.class).getDisabledColor() + "Out of valid blocks. Disabling!");
            disable();

        }

            switch (towerMode.getValue()) {

                case "Jump": {

                    if (mc.player.onGround) {

                        oldTower = mc.player.posY;
                        mc.player.jump();

                    }

                    if (Math.floor(mc.player.posY) == oldTower + 1 && !mc.player.onGround) {

                        mc.player.motionY = -(mc.player.posY - Math.floor(mc.player.posY)); // go down faster whist looking smoothest

                    }

                    placeBlockPacket(towerPos, false);

                    break;

                }

                case "Motion": { // Best scaffold ever 100%

                    if (mc.player.onGround)
                        timer = 0;
                    else
                        timer++;

                    if (timer == 3 && mc.gameSettings.keyBindJump.isKeyDown()) {

                        mc.player.motionY = 0.42;
                        timer = 0;

                    }

                    placeBlockPacket(towerPos, false);
                    break;

                }
                case "FakeJump": {

                    if (mc.player.ticksExisted % delay.getValue() == 0 && mc.player.onGround) {

                        PlayerUtil.fakeJump(3);
                        mc.player.setPosition(mc.player.posX,mc.player.posY + 1.0013359791121,mc.player.posZ);

                        placeBlockPacket(towerPos, false);
                        break;
                    }

                }
            }


        if (mc.gameSettings.keyBindJump.isKeyDown())
            placeBlockPacket(towerPos, false);

        if (!mc.gameSettings.keyBindJump.isKeyDown() && !mc.gameSettings.keyBindSprint.isKeyDown()) {

            placeBlockPacket(scaffold, true);

        }

        double[] dir = MotionUtil.forward(downSpeed.getValue());
        if (mc.gameSettings.keyBindSprint.isKeyDown()) {

            placeBlockPacket(downPos, false);
            mc.player.motionX = dir[0];
            mc.player.motionZ = dir[1];

        }
    });

    boolean placeBlockPacket(BlockPos pos, boolean allowSupport) {

        mc.player.rotationYaw += mc.player.ticksExisted % 2 == 0 ? 0.00001 : -0.00001; // force rotation packet

        boolean shouldplace = mc.world.getBlockState(pos).getBlock().isReplaceable(mc.world,pos) && BlockUtil.getPlaceableSide(pos) != null;

        rotateTo = pos;

        if (shouldplace) {

            boolean swap = newSlot != mc.player.inventory.currentItem;

            if (swap) {
                mc.player.connection.sendPacket(new CPacketHeldItemChange(newSlot));
                mc.player.inventory.currentItem = newSlot;
            }


            boolean success = PlacementUtil.place(pos, EnumHand.MAIN_HAND, rotate.getValue(), false, false);

            if (swap) {
                mc.player.connection.sendPacket(new CPacketHeldItemChange(oldSlot));
                mc.player.inventory.currentItem = oldSlot;
            }

            return success;

        } else if (allowSupport && BlockUtil.getPlaceableSide(pos) == null)
            clutch();

        return false;
    }

    public void clutch() {

        BlockPos xpPos = new BlockPos(mc.player.posX + 1, mc.player.posY - 1, mc.player.posZ);
        BlockPos xmPos = new BlockPos(mc.player.posX - 1, mc.player.posY - 1, mc.player.posZ);
        BlockPos zpPos = new BlockPos(mc.player.posX, mc.player.posY - 1, mc.player.posZ + 1);
        BlockPos zmPos = new BlockPos(mc.player.posX, mc.player.posY - 1, mc.player.posZ - 1);


        if (!placeBlockPacket(xpPos, false))
            if (!placeBlockPacket(xmPos, false))
                if (!placeBlockPacket(zpPos, false))
                    placeBlockPacket(zmPos, false);
    }

    private final Listener<PlayerJumpEvent> jumpEventListener = new Listener<>(event -> {
        if (towerMode.getValue().equalsIgnoreCase("FakeJump"))
            event.cancel();
    });

}

