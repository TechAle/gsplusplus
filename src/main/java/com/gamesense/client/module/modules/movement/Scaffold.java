package com.gamesense.client.module.modules.movement;

import com.gamesense.api.event.events.PacketEvent;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.misc.MessageBus;
import com.gamesense.api.util.player.PlacementUtil;
import com.gamesense.api.util.player.PredictUtil;
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
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;

import java.util.Arrays;

@Module.Declaration(name = "Scaffold", category = Category.Movement)
public class Scaffold extends Module {

    ModeSetting logic = registerMode("Place Logic", Arrays.asList("Predict", "Player"), "Predict");
    IntegerSetting distance = registerInteger("Distance Predict", 2, 0, 20, () -> logic.getValue().equalsIgnoreCase("Predict"));
    IntegerSetting distanceP = registerInteger("Distance Player", 2, 0, 20, () -> logic.getValue().equalsIgnoreCase("Player"));
    ModeSetting towerMode = registerMode("Tower Mode", Arrays.asList("Jump", "Motion", "None"), "Motion");DoubleSetting downSpeed = registerDouble("DownSpeed", 0, 0, 0.2);
    BooleanSetting keepYOnSpeed = registerBoolean("Speed Keep Y", false);
    BooleanSetting rotate = registerBoolean("Rotate", false);
    BooleanSetting keepRot = registerBoolean("Keep rotated", false, () -> rotate.getValue());

    int timer;

    int oldSlot;
    int newSlot;

    double oldTower;

    EntityPlayer predPlayer;

    BlockPos scaffold;
    BlockPos towerPos;
    BlockPos downPos;
    BlockPos rotPos;

    Vec2f rot;
    @EventHandler
    private final Listener<PacketEvent.Send> sendListener = new Listener<>(event -> {

        try {
            if (rotate.getValue() && keepRot.getValue()) {

                if (event.getPacket() instanceof CPacketPlayer) {

                    ((CPacketPlayer) event.getPacket()).yaw = rot.x;
                    ((CPacketPlayer) event.getPacket()).pitch = rot.y;

                }

            }
        } catch (NullPointerException ignored) {
        }
    });

    @Override
    protected void onEnable() {
        timer = 0;
    }

    public void onUpdate() {

//      literally just get new packet that we cant even see for keepRotation to consistently have rotation packets to use
        if (mc.player.ticksExisted % 2 == 0)
            mc.player.rotationPitch += 0.00001;
        else
            mc.player.rotationPitch -= 0.00001;

        oldSlot = mc.player.inventory.currentItem;

        towerPos = new BlockPos(mc.player.posX, mc.player.posY - 1, mc.player.posZ);
        downPos = new BlockPos(mc.player.posX, mc.player.posY - 2, mc.player.posZ);


        if (logic.getValue().equalsIgnoreCase("Predict")) {

            PredictUtil.PredictSettings predset = new PredictUtil.PredictSettings((Integer) (distance.getValue()), false, 0, 0, 0, 0, 0, 0, false, 0, false, false, false, false, false, 0, 696969);

            predPlayer = PredictUtil.predictPlayer(mc.player, predset);

            scaffold = (new BlockPos(predPlayer.posX, predPlayer.posY - 1, predPlayer.posZ));

            if (keepYOnSpeed.getValue() && ModuleManager.getModule(Speed.class).isEnabled())
                scaffold.y = ModuleManager.getModule(Speed.class).yl;

        } else if (logic.getValue().equalsIgnoreCase("Player")) {

            scaffold = new BlockPos(mc.player.posX + (mc.player.motionX + distanceP.getValue()), mc.player.posY, mc.player.posZ + (mc.player.motionZ * distanceP.getValue())).down();

            if (keepYOnSpeed.getValue() && ModuleManager.getModule(Speed.class).isEnabled())
                scaffold.y = ModuleManager.getModule(Speed.class).yl;


        }

        // Courtesy of KAMI, this block finding algo
        newSlot = -1;
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

        if (newSlot == -1) {

            newSlot = 1;

            MessageBus.sendClientPrefixMessage(ModuleManager.getModule(ColorMain.class).getDisabledColor() + "Out of valid blocks. Disabling!");
            disable();

        }

        if (mc.gameSettings.keyBindJump.isKeyDown()) { // TOWER

            switch (towerMode.getValue()) {

                case "Jump": {

                    if (mc.player.onGround) {

                        oldTower = mc.player.posY;
                        mc.player.jump();

                    }

                    if (Math.floor(mc.player.posY) == oldTower + 1) {

                        mc.player.motionY = -69; // go down faster

                    }

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

                }
            }


            placeBlockPacket(towerPos, false);

        }

        if (!mc.gameSettings.keyBindJump.isKeyDown() && !mc.gameSettings.keyBindSprint.isKeyDown()) {

            placeBlockPacket(scaffold, true);

        }

        double[] dir = MotionUtil.forward(downSpeed.getValue());
        if (mc.gameSettings.keyBindSprint.isKeyDown()) {

            placeBlockPacket(downPos, false);
            mc.player.motionX = dir[0];
            mc.player.motionZ = dir[1];

        }
    }

    void placeBlockPacket(BlockPos pos, boolean allowSupport) {

        boolean shouldplace = mc.world.getBlockState(pos).getBlock().isReplaceable(mc.world,pos) && BlockUtil.getPlaceableSide(pos) != null;

        if (shouldplace) {

            mc.player.connection.sendPacket(new CPacketHeldItemChange(newSlot));
            mc.player.inventory.currentItem = newSlot;


            PlacementUtil.place(pos, EnumHand.MAIN_HAND, rotate.getValue(), false);

            mc.player.connection.sendPacket(new CPacketHeldItemChange(oldSlot));
            mc.player.inventory.currentItem = oldSlot;

        } else if (allowSupport && BlockUtil.getPlaceableSide(pos) == null)
            clutch();
    }

    public void clutch() {

        BlockPos xppos = new BlockPos(mc.player.posX + 1, mc.player.posY - 1, mc.player.posZ);
        BlockPos xmpos = new BlockPos(mc.player.posX - 1, mc.player.posY - 1, mc.player.posZ);
        BlockPos zppos = new BlockPos(mc.player.posX, mc.player.posY - 1, mc.player.posZ + 1);
        BlockPos zmpos = new BlockPos(mc.player.posX, mc.player.posY - 1, mc.player.posZ - 1);


        placeBlockPacket(xppos, false);
        if (mc.world.getBlockState(xppos).getMaterial().isReplaceable()) {
            placeBlockPacket(xmpos, false);
            if (mc.world.getBlockState(xmpos).getMaterial().isReplaceable()) {
                placeBlockPacket(zppos, false);
                if (mc.world.getBlockState(xppos).getMaterial().isReplaceable()) {
                    placeBlockPacket(zmpos, false);
                }
            }
        }
    }

}

