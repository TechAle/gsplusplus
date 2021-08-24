package com.gamesense.client.module.modules.movement;

import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.misc.MessageBus;
import com.gamesense.api.util.misc.Timer;
import com.gamesense.api.util.player.PlacementUtil;
import com.gamesense.api.util.player.PlayerUtil;
import com.gamesense.api.util.player.PredictUtil;
import com.gamesense.api.util.world.BlockUtil;
import com.gamesense.api.util.world.MotionUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.ModuleManager;
import com.gamesense.client.module.modules.gui.ColorMain;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;

@Module.Declaration(name = "ScaffoldRewrite", category = Category.Movement)
public class ScaffoldRewrite extends Module {

    IntegerSetting distance = registerInteger("Distance", 2, 0, 20);
    ModeSetting towerMode = registerMode("Tower Mode", Arrays.asList("Jump", "Motion", "Clip", "Burrow", "None"), "Motion");
    DoubleSetting jumpMotion = registerDouble("Jump Speed", -5, 0, -10, () -> towerMode.getValue().equalsIgnoreCase("Jump"));
    IntegerSetting clipSpeed = registerInteger("Clip Delay", 2, 1, 20, () -> towerMode.getValue().equalsIgnoreCase("Clip"));
    DoubleSetting downSpeed = registerDouble("DownSpeed", 0, 0, 0.2);
    BooleanSetting rotate = registerBoolean("Rotate", false);

    int oldSlot;
    int newSlot;

    double oldTower;

    EntityPlayer predPlayer;

    BlockPos scaffold;
    BlockPos scaffoldSupport;
    BlockPos towerPos;
    BlockPos downPos;

    Timer towerTimer = new Timer();

    public void onUpdate() {

        oldSlot = mc.player.inventory.currentItem;

        towerPos = new BlockPos(mc.player.posX, mc.player.posY - 1, mc.player.posZ);
        downPos = new BlockPos(mc.player.posX, mc.player.posY - 2, mc.player.posZ);

        if (mc.gameSettings.keyBindJump.isKeyDown()) {

            mc.player.motionX *= 0.3;
            mc.player.motionZ *= 0.3;


        }

        predPlayer = PredictUtil.predictPlayer(mc.player, new PredictUtil.PredictSettings(distance.getValue(), false, 0, 0, 0, 0, 0, 0, false, 0, false, false, false, false));

        scaffold = predPlayer.getPosition().add(0, -1, 0);

        if (mc.gameSettings.keyBindSprint.isKeyDown()) scaffold.add(0, -1, 0);

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
                    if (mc.player.collidedVertically) {
                        mc.player.isAirBorne = true;
                        mc.player.motionY = 0.4199382043D;
                        oldTower = mc.player.posY;
                    }

                    if (mc.player.posY > oldTower + 1.15) {
                        mc.player.setPosition(mc.player.posX, Math.floor(mc.player.posY), mc.player.posZ);
                        mc.player.motionY = 0.4199382043D;
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

                case "Clip": { //Doesnt work :/

                    if (towerTimer.hasReached(clipSpeed.getValue() * 50, true)) { // Delay so we don't spam packets and get kicked

                        PlayerUtil.fakeJump(); // Jump

                    }
                    break;

                }

                case "Burrow": {

                    if (towerTimer.hasReached(clipSpeed.getValue() * 50, true)) {
                        BlockPos burrowBlockPos = new BlockPos(Math.ceil(mc.player.posX) - 1, Math.ceil(mc.player.posY - 1) + 1.5, Math.ceil(mc.player.posZ) - 1);

                        mc.player.connection.sendPacket(new CPacketHeldItemChange(newSlot));

                        PlayerUtil.fakeJump();

                        PlacementUtil.place(burrowBlockPos, EnumHand.MAIN_HAND, (rotate.getValue()));

                        placeBlockPacket(towerPos, false); // Place Block

                        mc.player.connection.sendPacket(new CPacketHeldItemChange(oldSlot));
                    }

                    break;

                }
            }

            placeBlockPacket(towerPos, false);

        }

        if (!mc.gameSettings.keyBindJump.isKeyDown() && !mc.gameSettings.keyBindSprint.isKeyDown()) {

            placeBlockPacket(scaffold, true);

        }

        double[] dir = MotionUtil.forward(downSpeed.getValue());
        if (mc.gameSettings.keyBindSprint.isKeyDown()) {

            placeBlockPacket(downPos, true);
            mc.player.motionX = dir[0];
            mc.player.motionZ = dir[1];

        }
    }


    void placeBlockPacket(BlockPos pos, boolean allowSupport) {

        mc.player.connection.sendPacket(new CPacketHeldItemChange(newSlot));

        PlacementUtil.place(pos, EnumHand.MAIN_HAND, rotate.getValue());

        //Switch back
        mc.player.connection.sendPacket(new CPacketHeldItemChange(oldSlot));
    }

    void doSupport() {

        // Round player yaw to nearest 45

        float yawRounded;

        float yaw = Math.abs(Math.round(mc.player.rotationYaw) % 360);
        float division = (int) Math.floor(yaw / 45);
        float remainder = (int) (yaw % 45);
        if (remainder < 45f / 2f) {
            yawRounded = 45 * division;
        } else {
            yawRounded = 45 * (division + 1);
        }

        // TERRIBLE CODING AHEAD //

        if (mc.gameSettings.keyBindLeft.isKeyDown()) {

            yawRounded -= 45;

        }
        if (mc.gameSettings.keyBindRight.isKeyDown()) {

            yawRounded += 45;

        }

        if (yawRounded == 45) { //NE
            scaffoldSupport = scaffold;
            scaffoldSupport.add(-1, 0, 0);
        } else if (yawRounded == 45 + 90) { // SE
            scaffoldSupport = scaffold;
            scaffoldSupport.add(1, 0, 0);
        } else if (yawRounded == 45 + 90 + 90) { // SW
            scaffoldSupport = scaffold;
            scaffoldSupport.add(1, 0, 0);
        } else if (yawRounded == 45 + 90 + 90 + 90) { // NW
            scaffoldSupport = scaffold;
            scaffoldSupport.add(-1, 0, 0);
        }

        placeBlockPacket(scaffoldSupport, false);

    }
}

