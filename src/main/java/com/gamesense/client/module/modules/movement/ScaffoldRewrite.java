package com.gamesense.client.module.modules.movement;

import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.misc.MessageBus;
import com.gamesense.api.util.misc.Timer;
import com.gamesense.api.util.player.PlacementUtil;
import com.gamesense.api.util.player.PredictUtil;
import com.gamesense.api.util.world.EntityUtil;
import com.gamesense.api.util.world.MotionUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.ModuleManager;
import com.gamesense.client.module.modules.gui.ColorMain;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockFalling;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import java.util.Arrays;

@Module.Declaration(name = "ScaffoldRewrite", category = Category.Movement)
public class ScaffoldRewrite extends Module {

    ModeSetting logic = registerMode("Place Logic", Arrays.asList("Predict", "Player"), "Predict");
    IntegerSetting distance = registerInteger("Distance", 2, 0, 20);
    ModeSetting towerMode = registerMode("Tower Mode", Arrays.asList("Jump", "Motion", "AirJump", "None"), "Motion");
    IntegerSetting airJumpDelay = registerInteger("Air Jump Delay", 3, 0, 20, () -> towerMode.getValue().equals("AirJump"));
    DoubleSetting jumpHeight = registerDouble("Air Jump Height", 0.42, 0, 1, () -> towerMode.getValue().equals("AirJump"));
    DoubleSetting jumpMotion = registerDouble("Jump Speed", -5, 0, -10, () -> towerMode.getValue().equalsIgnoreCase("Jump"));
    DoubleSetting downSpeed = registerDouble("DownSpeed", 0, 0, 0.2);
    BooleanSetting rotate = registerBoolean("Rotate", false);
    BooleanSetting silent = registerBoolean("Silent Switch", true);

    int timer;

    int oldSlot;
    int newSlot;

    double oldTower;

    EntityPlayer predPlayer;

    BlockPos scaffold;
    BlockPos towerPos;
    BlockPos downPos;

    Vec3d vec;

    @Override
    protected void onEnable() {
        timer = 0;
    }

    public void onUpdate() {

        oldSlot = mc.player.inventory.currentItem;

        towerPos = new BlockPos(mc.player.posX, mc.player.posY - 1, mc.player.posZ);
        downPos = new BlockPos(mc.player.posX, mc.player.posY - 2, mc.player.posZ);


        if (logic.getValue().equalsIgnoreCase("Predict")) {
            predPlayer = PredictUtil.predictPlayer(mc.player, new PredictUtil.PredictSettings(distance.getValue(), false, 0, 0, 0, 0, 0, 0, false, 0, false, false, false, false));

            scaffold = (new BlockPos(predPlayer.posX,predPlayer.posY-1,predPlayer.posZ));

            if (mc.gameSettings.keyBindSprint.isKeyDown()) scaffold.add(0, -1, 0);
        } else if (logic.getValue().equalsIgnoreCase("Player")) {

            scaffold = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ).down();

            scaffold.add(mc.player.motionX * distance.getValue(), 0, mc.player.motionZ * distance.getValue());

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

                case "Motion": { // might be broken
                    if (mc.player.onGround) {
                        mc.player.isAirBorne = true;
                        mc.player.motionY = 0.41583072100313484;
                        oldTower = mc.player.posY;
                    }

                    if (mc.player.posY > oldTower + 0.42) {

                        mc.player.setPosition(mc.player.posX, Math.floor(mc.player.posY), mc.player.posZ);
                        mc.player.motionY = 0.42;
                        oldTower = mc.player.posY;
                    }

                    break;

                }
                case "Jump": { // Should work in mean time

                    if (mc.player.onGround) {

                        oldTower = mc.player.posY;
                        mc.player.jump();

                    }

                    if (mc.player.posY > oldTower + 1.15) /* peak of jump is ~ 1.17ish so we will reach 1.1 */ {

                        mc.player.motionY = jumpMotion.getValue(); // go down faster

                    }

                    break;

                }

                case "AirJump": { // Best scaffold ever 100%

                    if (mc.player.onGround)
                        timer = 0;
                    else
                        timer++;

                    if (timer == airJumpDelay.getValue() && mc.gameSettings.keyBindJump.isKeyDown()) {

                        mc.player.motionY = jumpHeight.getValue();
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

        if (silent.getValue()){
            mc.player.connection.sendPacket(new CPacketHeldItemChange(newSlot));
        } else {

            mc.player.inventory.currentItem = newSlot;

        }


        if (mc.world != null && pos != null) {
            PlacementUtil.place(pos, EnumHand.MAIN_HAND, rotate.getValue());
        }

        //Switch back
        if (silent.getValue()) {
            mc.player.connection.sendPacket(new CPacketHeldItemChange(oldSlot));
        }

        if (allowSupport) {
            assert pos != null;
            if (mc.world.getBlockState(pos).getBlock() instanceof BlockAir) {

                clutch();

            }
        }
    }

    public void clutch() {

        BlockPos xppos = new BlockPos(mc.player.posX + 1, mc.player.posY - 1, mc.player.posZ);
        BlockPos xmpos = new BlockPos(mc.player.posX - 1, mc.player.posY - 1, mc.player.posZ);
        BlockPos zppos = new BlockPos(mc.player.posX, mc.player.posY - 1, mc.player.posZ + 1);
        BlockPos zmpos = new BlockPos(mc.player.posX, mc.player.posY - 1, mc.player.posZ - 1);


        if (!mc.player.onGround){
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

}

