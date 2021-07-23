package com.gamesense.client.module.modules.combat;

import com.gamesense.api.event.Phase;
import com.gamesense.api.event.events.OnUpdateWalkingPlayerEvent;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.player.*;
import com.gamesense.api.util.world.Offsets;
import com.gamesense.api.util.misc.Timer;
import com.gamesense.client.manager.managers.PlayerPacketManager;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.ModuleManager;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.block.BlockWeb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;

/**
 * @author Hoosiers
 * @since 03/29/2021
 */

@Module.Declaration(name = "AutoWeb", category = Category.Combat)
public class AutoWeb extends Module {

    ModeSetting offsetMode = registerMode("Pattern", Arrays.asList("Single", "Double"), "Single");
    ModeSetting targetMode = registerMode("Target", Arrays.asList("Nearest", "Looking"), "Nearest");
    IntegerSetting enemyRange = registerInteger("Range", 4, 0, 6);
    IntegerSetting delayTicks = registerInteger("Tick Delay", 3, 0, 10);
    IntegerSetting blocksPerTick = registerInteger("Blocks Per Tick", 4, 1, 8);
    BooleanSetting rotate = registerBoolean("Rotate", true);
    BooleanSetting sneakOnly = registerBoolean("Sneak Only", false);
    BooleanSetting disableNoBlock = registerBoolean("Disable No Web", true);
    BooleanSetting disableOnCa = registerBoolean("Disable on CA", true);
    BooleanSetting silentSwitch = registerBoolean("Silent Switch", false);
    BooleanSetting strict = registerBoolean("Strict Section", false);
    BooleanSetting yawCheck = registerBoolean("Yaw Check", false,
            () -> strict.getValue() && !rotate.getValue());
    IntegerSetting yawStep = registerInteger("Yaw Step", 40, 0, 180,
            () -> strict.getValue() && yawCheck.getValue() && !rotate.getValue());
    BooleanSetting pitchCheck = registerBoolean("Pitch Check", false,
            () -> strict.getValue() && !rotate.getValue());
    IntegerSetting pitchStep = registerInteger("Pitch Step", 40, 0, 180,
            () -> strict.getValue() && pitchCheck.getValue() && !rotate.getValue());
    BooleanSetting placeStrictPredict = registerBoolean("Place Strict Predict", false,
            () -> strict.getValue() && (pitchCheck.getValue() || yawCheck.getValue()));
    IntegerSetting tickAfterRotation = registerInteger("Tick After Rotation", 0, 0, 10,
            () -> strict.getValue());

    private final Timer delayTimer = new Timer();
    private EntityPlayer targetPlayer = null;

    private int oldSlot = -1;
    private int offsetSteps = 0;
    private boolean outOfTargetBlock = false;

    public void onEnable() {
        PlacementUtil.onEnable();
        if (mc.player == null || mc.world == null) {
            disable();
            return;
        }
        yPlayer = -2000;
        tick = 100;

        oldSlot = mc.player.inventory.currentItem;
    }

    public void onDisable() {
        PlacementUtil.onDisable();
        if (mc.player == null | mc.world == null) return;

        if (outOfTargetBlock) setDisabledMessage("No web detected... AutoWeb turned OFF!");

        if (oldSlot != mc.player.inventory.currentItem && oldSlot != -1) {
            if (silentSwitch.getValue())
                mc.player.connection.sendPacket(new CPacketHeldItemChange(oldSlot));
            else
                mc.player.inventory.currentItem = oldSlot;
            oldSlot = -1;
        }

        AutoCrystal.stopAC = false;

        outOfTargetBlock = false;
        targetPlayer = null;
    }

    public void onUpdate() {
        if (mc.player == null || mc.world == null) {
            disable();
            return;
        }

        if (disableOnCa.getValue() && ModuleManager.isModuleEnabled(AutoCrystalRewrite.class))
            return;

        if (sneakOnly.getValue() && !mc.player.isSneaking()) {
            return;
        }

        int targetBlockSlot = InventoryUtil.findFirstBlockSlot(BlockWeb.class, 0, 8);

        if ((outOfTargetBlock || targetBlockSlot == -1) && disableNoBlock.getValue()) {
            outOfTargetBlock = true;
            disable();
            return;
        }

        switch (targetMode.getValue()) {
            case "Nearest": {
                targetPlayer = PlayerUtil.findClosestTarget(enemyRange.getValue(), targetPlayer);
                break;
            }
            case "Looking": {
                targetPlayer = PlayerUtil.findLookingPlayer(enemyRange.getValue());
                break;
            }
            default: {
                targetPlayer = null;
                break;
            }
        }

        if (targetPlayer == null) return;

        Vec3d targetVec3d = targetPlayer.getPositionVector();

        if (delayTimer.getTimePassed() / 50L >= delayTicks.getValue()) {
            delayTimer.reset();

            int blocksPlaced = 0;

            while (blocksPlaced <= blocksPerTick.getValue()) {
                int maxSteps;
                Vec3d[] offsetPattern;

                switch (offsetMode.getValue()) {
                    case "Double": {
                        offsetPattern = Offsets.BURROW_DOUBLE;
                        maxSteps = Offsets.BURROW_DOUBLE.length;
                        break;
                    }
                    default: {
                        offsetPattern = Offsets.BURROW;
                        maxSteps = Offsets.BURROW.length;
                        break;
                    }
                }

                if (offsetSteps >= maxSteps) {
                    offsetSteps = 0;
                    break;
                }

                BlockPos offsetPos = new BlockPos(offsetPattern[offsetSteps]);
                BlockPos targetPos = new BlockPos(targetVec3d).add(offsetPos.getX(), offsetPos.getY(), offsetPos.getZ());

                boolean tryPlacing = true;

                if (targetPlayer.posY % 1 > 0.2) {
                    targetPos = new BlockPos(targetPos.getX(), targetPos.getY() + 1, targetPos.getZ());
                }

                if (!mc.world.getBlockState(targetPos).getMaterial().isReplaceable()) {
                    tryPlacing = false;
                }

                if (tryPlacing && placeBlock(targetPos)) {
                    blocksPlaced++;
                }

                offsetSteps++;
            }
        }
    }

    private boolean placeBlock(BlockPos pos) {
        EnumHand handSwing = EnumHand.MAIN_HAND;
        if ((yawCheck.getValue() || pitchCheck.getValue()) && yPlayer == -2000) {
            lastHitVec = new Vec3d(pos).add(0.5, 1, 0.5);
            Vec2f rotationWanted = RotationUtil.getRotationTo(lastHitVec);
            yPlayer = pitchCheck.getValue()
                    ? mc.player.getPitchYaw().x
                    : Double.MIN_VALUE;
            xPlayer = yawCheck.getValue()
                    ? RotationUtil.normalizeAngle(mc.player.getPitchYaw().y)
                    : Double.MIN_VALUE;
            tick = 100;

            if (placeStrictPredict.getValue()) {

                if (yawCheck.getValue()) {
                    // Get first if + or -
                    double distanceDo = rotationWanted.x - xPlayer;
                    if (Math.abs(distanceDo) > 180) {
                        distanceDo = RotationUtil.normalizeAngle(distanceDo);
                    }
                    // Check if distance is > of what we want
                    if (Math.abs(distanceDo) > yawStep.getValue()) {
                        return false;
                    }
                }

                if (pitchCheck.getValue()) {
                    // Get first if + or -
                    double distanceDo = rotationWanted.y - yPlayer;
                    // Check if distance is > of what we want

                    if (Math.abs(distanceDo) > pitchStep.getValue()) {
                        return false;
                    }
                }

            } else if (!(xPlayer == rotationWanted.x && yPlayer == rotationWanted.y))
                return false;
        }

        int targetBlockSlot = InventoryUtil.findFirstBlockSlot(BlockWeb.class, 0, 8);

        if (targetBlockSlot == -1) {
            outOfTargetBlock = true;
            return false;
        }

        if (mc.player.inventory.currentItem != targetBlockSlot) {
            if (silentSwitch.getValue())
                mc.player.connection.sendPacket(new CPacketHeldItemChange(targetBlockSlot));
            else
                mc.player.inventory.currentItem = targetBlockSlot;
        }
        tick = 0;
        return PlacementUtil.place(pos, handSwing, rotate.getValue(), true);
    }

    Vec3d lastHitVec;
    double xPlayer, yPlayer;
    int tick;
    boolean isRotating;

    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<OnUpdateWalkingPlayerEvent> onUpdateWalkingPlayerEventListener = new Listener<>(event -> {
        // If we dont have to rotate
        if (event.getPhase() != Phase.PRE || rotate.getValue() || lastHitVec == null) return;

        // If we reached the last point (Delay)
        if ( tick != 100 && tick++ > tickAfterRotation.getValue()) {
            lastHitVec = null;
            tick = 0;
            xPlayer = -2000;
            yPlayer = -2000;
        } else {
            // If we have to rotate
            Vec2f rotationWanted = RotationUtil.getRotationTo(lastHitVec);
            Vec2f nowRotation;

            if (yawCheck.getValue() || pitchCheck.getValue()) {

                if (yPlayer == Double.MIN_VALUE)
                    yPlayer = rotationWanted.y;
                else {
                    // Get first if + or -
                    double distanceDo = rotationWanted.y - yPlayer;
                    int direction = distanceDo > 0 ? 1 : -1;
                    // Check if distance is > of what we want

                    if (Math.abs(distanceDo) > pitchStep.getValue()) {
                        yPlayer = RotationUtil.normalizeAngle(yPlayer + pitchStep.getValue() * direction);
                    } else {
                        yPlayer = rotationWanted.y;
                    }
                }
                if (xPlayer == Double.MIN_VALUE)
                    xPlayer = rotationWanted.x;
                else {
                    // Get first if + or -
                    double distanceDo = rotationWanted.x - xPlayer;
                    if (Math.abs(distanceDo) > 180) {
                        distanceDo = RotationUtil.normalizeAngle(distanceDo);
                    }
                    int direction = distanceDo > 0 ? 1 : -1;
                    // Check if distance is > of what we want

                    if (Math.abs(distanceDo) > yawStep.getValue()) {
                        xPlayer = RotationUtil.normalizeAngle(xPlayer + yawStep.getValue() * direction);
                    } else {
                        xPlayer = rotationWanted.x;
                    }
                }
                nowRotation = new Vec2f((float) xPlayer, (float) yPlayer);
            } else {
                nowRotation = rotationWanted;
            }

            PlayerPacket packet = new PlayerPacket(this, nowRotation);
            PlayerPacketManager.INSTANCE.addPacket(packet);
        }
    });
}