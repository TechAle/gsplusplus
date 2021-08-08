package com.gamesense.client.module.modules.movement;

import com.gamesense.api.event.events.PlayerMoveEvent;
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
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.Sys;

import java.util.Arrays;

@Module.Declaration(name = "Scaffold", category = Category.Movement)
public class Scaffold extends Module {

    DoubleSetting upSpeed = registerDouble("Up Speed",  0.42, 0, 1);
    IntegerSetting upDelay = registerInteger("Up Delay", 0,0,10);
    DoubleSetting downSpeed = registerDouble("Down Speed", 0, 0, 0.25);
    IntegerSetting offset = registerInteger("Offset", 1,0,3);

    BooleanSetting showPredictSettings = registerBoolean("Predict Settings", false);

    IntegerSetting tickPredict = registerInteger("Tick Predict", 8, 0, 30, () -> showPredictSettings.getValue());
    BooleanSetting calculateYPredict = registerBoolean("Calculate Y Predict", true, () -> showPredictSettings.getValue());
    IntegerSetting startDecrease = registerInteger("Start Decrease", 39, 0, 200, () -> calculateYPredict.getValue() && showPredictSettings.getValue());
    IntegerSetting exponentStartDecrease = registerInteger("Exponent Start", 2, 1, 5, () -> calculateYPredict.getValue()&& showPredictSettings.getValue());
    IntegerSetting decreaseY = registerInteger("Decrease Y", 2, 1, 5, () -> calculateYPredict.getValue()&& showPredictSettings.getValue());
    IntegerSetting exponentDecreaseY = registerInteger("Exponent Decrease Y", 1, 1, 3, () -> calculateYPredict.getValue()&& showPredictSettings.getValue());
    IntegerSetting increaseY = registerInteger("Increase Y", 3, 1, 5, () -> calculateYPredict.getValue()&& showPredictSettings.getValue());
    IntegerSetting exponentIncreaseY = registerInteger("Exponent Increase Y", 2, 1, 3, () -> calculateYPredict.getValue()&& showPredictSettings.getValue());
    BooleanSetting splitXZ = registerBoolean("Split XZ", true, () -> showPredictSettings.getValue());
    IntegerSetting width = registerInteger("Line Width", 2, 1, 5, () -> showPredictSettings.getValue());
    BooleanSetting debug = registerBoolean("Debug", false, () -> showPredictSettings.getValue());
    BooleanSetting showPredictions = registerBoolean("Show Predictions", false, () -> showPredictSettings.getValue());
    BooleanSetting manualOutHole = registerBoolean("Manual Out Hole", false, () -> showPredictSettings.getValue());
    BooleanSetting aboveHoleManual = registerBoolean("Above Hole Manual", false, () -> manualOutHole.getValue()&& showPredictSettings.getValue());

    double jumpGround;

    int oldSlot;
    int direction;

    boolean doSupport;
    boolean doDown;
    boolean doTechaleVoodooMagic;
    boolean towering;
    boolean dontPlace;
    boolean cont;
    boolean wait;

    BlockPos belowPlayerBlock;
    BlockPos playerBlock;
    BlockPos supportBlock;

    PredictUtil.PredictSettings predictSettings;
    PredictUtil.PredictSettings predictSettingsSafeWalk;

    final Timer stuckTimer = new Timer();
    final Timer towerTimer = new Timer();
    final Timer switchTimer = new Timer();
    final Timer towerPlaceTimer = new Timer();

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

        direction = (MathHelper.floor((double) (mc.player.rotationYaw * 8.0F / 360.0F) + 0.5D) & 7);



//
//            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.42, mc.player.posZ, true));
//            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.75, mc.player.posZ, true));
//            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.01, mc.player.posZ, true));
//            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.16, mc.player.posZ, true));
//
//            }

        int targetBlockSlot = InventoryUtil.findObsidianSlot(false, false);

        if (targetBlockSlot > 9 || targetBlockSlot < 0) {
            MessageBus.sendClientPrefixMessage(ModuleManager.getModule(ColorMain.class).getDisabledColor() + "You dont have any obby lol");
            dontPlace = true;
        }


        for (int i = 1; i < 10; i++) {

            if (cont && mc.player.inventory.getStackInSlot(i) != ItemStack.EMPTY && mc.player.inventory.getStackInSlot(i).item instanceof ItemBlock) {

                targetBlockSlot = i;

                cont = false;

            }

        }


        if (targetBlockSlot == -1) {

            if (mc.player.posY > 0) {
                MessageBus.sendClientPrefixMessage(ModuleManager.getModule(ColorMain.class).getDisabledColor() + "No blocks in hotbar, disabling.");
                disable();
            }
        }


        if (!mc.world.getBlockState(belowPlayerBlock).getMaterial().isReplaceable()
                || mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(belowPlayerBlock)).stream().anyMatch(entity -> entity instanceof EntityPlayer && entity != mc.player)) {

            doTechaleVoodooMagic = !stuckTimer.hasReached(250, true) && !towering;

            return;
        }

        int newSlot;
        newSlot = InventoryUtil.findObsidianSlot(false, false);

        if (newSlot == -1)
            return;

        if (mc.player.inventory.currentItem != newSlot) {
            oldSlot = mc.player.inventory.currentItem;
            mc.player.connection.sendPacket(new CPacketHeldItemChange(newSlot));
            switchTimer.reset();
        }

        // place block

        if (!dontPlace) {
            if (!doDown && doTechaleVoodooMagic) {
                placeBlockPacket(null, belowPlayerBlock);

            } else {
                placeBlockPacket(null, supportBlock);
            }
        }

        if (mc.world.getBlockState(belowPlayerBlock).getMaterial().isReplaceable() && !mc.player.onGround && !towering && !doDown && !dontPlace && !mc.gameSettings.keyBindJump.isKeyDown()) {

            clutch();

        }
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

        //Switch back
        mc.player.connection.sendPacket(new CPacketHeldItemChange(oldSlot));
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

        BlockPos playerPos = new BlockPos(mc.player.posX, mc.player.posY - 1, mc.player.posZ);

        BlockPos xppos = new BlockPos(mc.player.posX + 1, mc.player.posY - 1, mc.player.posZ);
        BlockPos xmpos = new BlockPos(mc.player.posX - 1, mc.player.posY - 1, mc.player.posZ);
        BlockPos zppos = new BlockPos(mc.player.posX, mc.player.posY - 1, mc.player.posZ + 1);
        BlockPos zmpos = new BlockPos(mc.player.posX, mc.player.posY - 1, mc.player.posZ - 1);


        if (!dontPlace) {
            placeBlockPacket(null, xppos);
            if (mc.world.getBlockState(xppos).getMaterial().isReplaceable()) {
                placeBlockPacket(null, xmpos);
                if (mc.world.getBlockState(xmpos).getMaterial().isReplaceable()) {
                    placeBlockPacket(null, zppos);
                    if (mc.world.getBlockState(xppos).getMaterial().isReplaceable()) {
                        placeBlockPacket(null, zmpos);
                    }
                }
            }
        }
    }

    @EventHandler
    private final Listener<PlayerMoveEvent> playerMoveEventListener = new Listener<>(event -> {

        if (mc.gameSettings.keyBindJump.isKeyDown() && !mc.gameSettings.keyBindSprint.isKeyDown() && !MotionUtil.isMoving(mc.player)) {

//            mc.player.connection.sendPacket(new CPacketPlayer.Rotation(mc.player.rotationYaw,mc.player.rotationPitch,true));
//
//            mc.player.motionX *= 0.3;
//            mc.player.motionZ *= 0.3;
//            mc.player.motionY = 0.41;
//            if (towerTimer.hasReached(1500)) {
//                mc.player.motionY = -0.28;
//                towerTimer.reset();
//                towering = true;
//            }
//
//            placeBlockPacket(null, (new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ)));
//            placeBlockPacket(null, (new BlockPos(mc.player.posX, mc.player.posY - 1, mc.player.posZ)));



//            if (towerTimer.hasReached(upSpeed.getValue().longValue())){
//                mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.42, mc.player.posZ, true));
//                mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.75, mc.player.posZ, true));
//                mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.01, mc.player.posZ, true));
//                mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.15, mc.player.posZ, true));
//
//                mc.player.setPosition(mc.player.posX, mc.player.posY + 1.15, mc.player.posZ);
//
//                placeBlockPacket(null, (new BlockPos(mc.player.posX, mc.player.posY - 1, mc.player.posZ)));
//            }



            mc.player.motionX *= 0.3;
            mc.player.motionZ *= 0.3;
            mc.player.motionY = upSpeed.getValue();
            if (this.towerTimer.hasReached(1500L) || wait) {
                mc.player.motionY = -0.28;
                this.towerTimer.reset();
                wait = false;
            }
//            if (mc.player.posY > jumpGround + 0.79) {
//                mc.player.setPosition(
//                        mc.player.posX,
//                        Math.floor(mc.player.posY),
//                        mc.player.posZ
//                );
//                mc.player.motionY = 0.42;
//                jumpGround = mc.player.posY;
//            }
            if (mc.world.getBlockState(new BlockPos(mc.player.posX, mc.player.posY - offset.getValue(), mc.player.posZ)).getMaterial().isReplaceable() && towerPlaceTimer.hasReached(upDelay.getValue()*50, true)) {
                placeBlockPacket(null, new BlockPos(mc.player.posX, mc.player.posY - offset.getValue(), mc.player.posZ));
                wait = true;
            }

        } else {

            towerTimer.reset();
            towering = false;
            dontPlace = false;

        }
    });
}
