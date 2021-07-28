package com.gamesense.client.module.modules.movement;

import com.gamesense.api.setting.values.*;
import com.gamesense.api.util.misc.MessageBus;
import com.gamesense.api.util.misc.Timer;
import com.gamesense.api.util.player.InventoryUtil;
import com.gamesense.api.util.player.PredictUtil;
import com.gamesense.api.util.world.BlockUtil;
import com.gamesense.api.util.world.MotionUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.ModuleManager;
import com.gamesense.client.module.modules.gui.ColorMain;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;

@Module.Declaration(name = "Scaffold", category = Category.Movement)
public class Scaffold extends Module {

    ModeSetting upMode = registerMode("Up Mode", Arrays.asList("Pull", "Fly", "None"), "Pull");
    DoubleSetting upSpeed = registerDouble("Up Speed", 1, 0, 5);
    DoubleSetting downSpeed = registerDouble("Down Speed", 0.05, 0, 0.25);
    BooleanSetting render = registerBoolean("Render", false);
    ColorSetting mainColor = registerColor("Color");

    IntegerSetting tickPredict = registerInteger("Tick Predict", 8, 0, 30);
    BooleanSetting calculateYPredict = registerBoolean("Calculate Y Predict", true);
    IntegerSetting startDecrease = registerInteger("Start Decrease", 39, 0, 200, () -> calculateYPredict.getValue());
    IntegerSetting exponentStartDecrease = registerInteger("Exponent Start", 2, 1, 5, () -> calculateYPredict.getValue());
    IntegerSetting decreaseY = registerInteger("Decrease Y", 2, 1, 5, () -> calculateYPredict.getValue());
    IntegerSetting exponentDecreaseY = registerInteger("Exponent Decrease Y", 1, 1, 3, () -> calculateYPredict.getValue());
    IntegerSetting increaseY = registerInteger("Increase Y", 3, 1, 5, () -> calculateYPredict.getValue());
    IntegerSetting exponentIncreaseY = registerInteger("Exponent Increase Y", 2, 1, 3, () -> calculateYPredict.getValue());
    BooleanSetting splitXZ = registerBoolean("Split XZ", true);
    IntegerSetting width = registerInteger("Line Width", 2, 1, 5);
    BooleanSetting debug = registerBoolean("Debug", false);
    BooleanSetting showPredictions = registerBoolean("Show Predictions", false);
    BooleanSetting manualOutHole = registerBoolean("Manual Out Hole", false);
    BooleanSetting aboveHoleManual = registerBoolean("Above Hole Manual", false, () -> manualOutHole.getValue());

    int targetBlockSlot;
    int oldSlot;
    int direction;

    boolean doSupport;
    boolean doDown;
    boolean doTechaleVoodooMagic;
    boolean towering;

    BlockPos belowPlayerBlock;
    BlockPos playerBlock;
    BlockPos supportBlock;

    PredictUtil.PredictSettings predictSettings;
    PredictUtil.PredictSettings predictSettingsSafeWalk;

    final Timer stuckTimer = new Timer();
    final Timer towerTimer = new Timer();

    @Override
    public void onUpdate() {

        predictSettings = new PredictUtil.PredictSettings(tickPredict.getValue(), calculateYPredict.getValue(), startDecrease.getValue(), exponentStartDecrease.getValue(), decreaseY.getValue(), exponentDecreaseY.getValue(), increaseY.getValue(), exponentIncreaseY.getValue(), splitXZ.getValue(), width.getValue(), debug.getValue(), showPredictions.getValue(), manualOutHole.getValue(), aboveHoleManual.getValue());

        supportBlock = new BlockPos(mc.player.posX, mc.player.posY - 2, mc.player.posZ);

        playerBlock = new BlockPos(PredictUtil.predictPlayer(mc.player, predictSettings));

        //DOWN SHIT

        if (mc.gameSettings.keyBindSprint.isKeyDown()) {
            belowPlayerBlock = playerBlock.add(0, -2, 0);
            doDown = true;
            if (!mc.player.onGround) {
                final double[] dir = directionSpeed(downSpeed.getValue());
                mc.player.motionX = dir[0];
                mc.player.motionZ = dir[1];
            }

        } else {
            belowPlayerBlock = playerBlock.add(0, -1, 0);
            doDown = false;
        }

        if (mc.gameSettings.keyBindJump.isKeyDown() && !mc.gameSettings.keyBindSprint.isKeyDown() && !MotionUtil.isMoving(mc.player)) {

            switch (upMode.getValue()) {
                case "Pull": {
                    towering = false;
                    if (mc.player.onGround) {
                        mc.player.jump();
                    } else if (mc.player.motionY < 0) {

                        mc.player.motionY = -upSpeed.getValue();

                    }
                }
                break;
                case "Fly": {


                    mc.player.motionX *= 0.3;
                    mc.player.motionZ *= 0.3;
                    mc.player.jump();
                    if (towerTimer.hasReached(1500)) {
                        mc.player.motionY = -0.28;
                        towerTimer.reset();
                        towering = true;
                    }
                }
            }
            placeBlockPacket(null, playerBlock);
        } else {
            towerTimer.reset();
            towering = false;
        }

        direction = (MathHelper.floor((double) (mc.player.rotationYaw * 8.0F / 360.0F) + 0.5D) & 7);


            /*

            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.42, mc.player.posZ, true));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.75, mc.player.posZ, true));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.01, mc.player.posZ, true));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.16, mc.player.posZ, true));

            }*/

        targetBlockSlot =

                oldSlot = mc.player.inventory.currentItem;

        if (targetBlockSlot == -1) {
            MessageBus.sendClientPrefixMessage(ModuleManager.getModule(ColorMain.class).getDisabledColor() + "You dont have any obby lol");
            disable();
        }

        mc.player.connection.sendPacket(new CPacketHeldItemChange(targetBlockSlot));
        mc.player.connection.sendPacket(new CPacketHeldItemChange(oldSlot));

        if (!mc.world.getBlockState(belowPlayerBlock).getMaterial().isReplaceable()
                || mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(belowPlayerBlock)).stream().anyMatch(entity -> entity instanceof EntityPlayer && entity != mc.player)) {
            doTechaleVoodooMagic = !stuckTimer.hasReached(250, true) && !towering;
            return;
        }

        int newSlot;
        newSlot = InventoryUtil.findObsidianSlot(false, false);

        if (newSlot == -1)
            return;

        int oldSlot;
        oldSlot = mc.player.inventory.currentItem;
        mc.player.inventory.currentItem = newSlot;

        // place block

        if (!doDown && doTechaleVoodooMagic) {
            placeBlockPacket(null, belowPlayerBlock);

        } else {
            placeBlockPacket(null, supportBlock);
        }

        if (mc.world.getBlockState(belowPlayerBlock).getMaterial().isReplaceable() && !mc.player.onGround && !towering && !doDown) {

            clutch();

        }

        mc.player.inventory.currentItem = oldSlot;
    }


    void placeBlockPacket(EnumFacing side, BlockPos pos) {

        if (side == null) {
            side = BlockUtil.getPlaceableSide(pos);
        }
        if (side == null) {

            doSupport = true;
            return;
        }
        BlockPos neighbour = pos.offset(side);
        EnumFacing opposite = side.getOpposite();
        Vec3d vec = new Vec3d(neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));

        // idk why these but PlayerControllerMP use them
        float f = (float) (vec.x - (double) pos.getX());
        float f1 = (float) (vec.y - (double) pos.getY());
        float f2 = (float) (vec.z - (double) pos.getZ());

        // Place
        mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(
                neighbour
                , opposite, EnumHand.MAIN_HAND, f, f1, f2));

        // Swing
        mc.player.swingArm(EnumHand.MAIN_HAND);
    }

    public static double[] directionSpeed(double speed) {
        float forward = mc.player.movementInput.moveForward;
        float side = mc.player.movementInput.moveStrafe;
        float yaw = mc.player.prevRotationYaw + (mc.player.rotationYaw - mc.player.prevRotationYaw) * mc.getRenderPartialTicks();

        if (forward != 0) {
            if (side > 0) {
                yaw += (forward > 0 ? -45 : 45);
            } else if (side < 0) {
                yaw += (forward > 0 ? 45 : -45);
            }
            side = 0;

            //forward = clamp(forward, 0, 1);
            if (forward > 0) {
                forward = 1;
            } else if (forward < 0) {
                forward = -1;
            }
        }

        final double sin = Math.sin(Math.toRadians(yaw + 90));
        final double cos = Math.cos(Math.toRadians(yaw + 90));
        final double posX = (forward * speed * cos + side * speed * sin);
        final double posZ = (forward * speed * sin - side * speed * cos);
        return new double[]{posX, posZ};
    }
    public void clutch() {

        BlockPos playerPos = new BlockPos(mc.player.posX,mc.player.posY-1,mc.player.posZ);

        BlockPos xppos = new BlockPos(mc.player.posX+1,mc.player.posY-1,mc.player.posZ);
        BlockPos xmpos = new BlockPos(mc.player.posX-1,mc.player.posY-1,mc.player.posZ);
        BlockPos zppos = new BlockPos(mc.player.posX,mc.player.posY-1,mc.player.posZ +1);
        BlockPos zmpos = new BlockPos(mc.player.posX,mc.player.posY-1,mc.player.posZ -1);


        placeBlockPacket(null,xppos);
        if (mc.world.getBlockState(xppos).getMaterial().isReplaceable()){
            placeBlockPacket(null, xmpos);
            if (mc.world.getBlockState(xmpos).getMaterial().isReplaceable()){
                placeBlockPacket(null, zppos);
                if (mc.world.getBlockState(xppos).getMaterial().isReplaceable()){
                    placeBlockPacket(null, zmpos);
                }
            }
        }
    }
}

