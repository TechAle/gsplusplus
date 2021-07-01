package com.gamesense.client.module.modules.combat;

import com.gamesense.api.event.Phase;
import com.gamesense.api.event.events.OnUpdateWalkingPlayerEvent;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.player.*;
import com.gamesense.api.util.world.BlockUtil;
import com.gamesense.api.util.world.EntityUtil;
import com.gamesense.api.util.world.HoleUtil;
import com.gamesense.client.manager.managers.PlayerPacketManager;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockSkull;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSkull;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Arrays;

import static com.gamesense.api.util.player.SpoofRotationUtil.ROTATION_UTIL;

/**
 * @author TechAle last edit 06/04/21
 * Ported and modified from Blocker.java
 */

@Module.Declaration(name = "AutoSkull", category = Category.Combat)
public class AutoSkull extends Module {

    BooleanSetting placementSection = registerBoolean("Placement Section", true);
    BooleanSetting offHandSkull = registerBoolean("OffHand Skull", false, () -> placementSection.getValue());
    DoubleSetting playerDistance = registerDouble("Player Distance", 0, 0, 6, () -> placementSection.getValue());
    IntegerSetting BlocksPerTick = registerInteger("Blocks Per Tick", 4, 0, 10, () -> placementSection.getValue());
    BooleanSetting autoTrap = registerBoolean("AutoTrap", false, () -> placementSection.getValue());
    BooleanSetting noUp = registerBoolean("No Up", false, () -> placementSection.getValue());
    BooleanSetting onlyHoles = registerBoolean("Only Holes", false, () -> placementSection.getValue());
    BooleanSetting centerPlayer = registerBoolean("Center Player", false, () -> placementSection.getValue());
    BooleanSetting delaySection = registerBoolean("Delay Section", true);
    BooleanSetting onShift = registerBoolean("On Shift", false, () -> delaySection.getValue());
    BooleanSetting instaActive = registerBoolean("Insta Active", true, () -> delaySection.getValue());
    BooleanSetting disableAfter = registerBoolean("Disable After", true, () -> delaySection.getValue());
    IntegerSetting tickDelay = registerInteger("Tick Delay", 5, 0, 10, () -> delaySection.getValue());
    IntegerSetting preSwitch = registerInteger("Pre Switch", 0, 0, 20, () -> delaySection.getValue());
    IntegerSetting afterSwitch = registerInteger("After Switch", 0, 0, 20, () -> delaySection.getValue());
    BooleanSetting rotationSection = registerBoolean("Rotation Section", true);
    BooleanSetting forceRotation = registerBoolean("Force Rotation", false, () -> rotationSection.getValue());
    BooleanSetting rotate = registerBoolean("Rotate", true, () -> rotationSection.getValue());
    BooleanSetting phaseSection = registerBoolean("Phase Section", true);
    BooleanSetting phase = registerBoolean("Phase", true, () -> phaseSection.getValue());
    BooleanSetting ServerRespond = registerBoolean("Server Respond", true, () -> phaseSection.getValue());
    BooleanSetting predictPhase = registerBoolean("Predict Phase", true, () -> phaseSection.getValue());
    IntegerSetting maxTickTries = registerInteger("Max Tick Try", 100, 1, 200, () -> phaseSection.getValue());

    private static final Vec3d[] AIR = {
        // Supports
        new Vec3d(-1, -1, -1),
        new Vec3d(-1, 0, -1),
        new Vec3d(-1, 1, -1),
        // Start circle
        new Vec3d(-1, 2, -1),
        new Vec3d(-1, 2, 0),
        new Vec3d(0, 2, -1),
        new Vec3d(1, 2, -1),
        new Vec3d(1, 2, 0),
        new Vec3d(1, 2, 1),
        new Vec3d(0, 2, 1),
    };

    private int delayTimeTicks = 0;
    private boolean noObby;
    private boolean activedBefore;
    private int oldSlot;
    private Vec3d lastHitVec = new Vec3d(-1, -1, -1);
    private int preRotationTick;
    private int afterRotationTick;
    private int stage;
    private boolean toPhase;
    private boolean alrPlaced;
    private int tickTry;
    private Vec3d centeredBlock = Vec3d.ZERO;

    public void onEnable() {
        ROTATION_UTIL.onEnable();
        PlacementUtil.onEnable();
        if (mc.player == null) {
            disable();
            return;
        }
        noObby = firstShift = alrPlaced = activedBefore = toPhase = false;
        lastHitVec = null;
        preRotationTick = afterRotationTick = stage = resetPhase = tickTry = 0;
        if (centerPlayer.getValue() && mc.player.onGround) {
            mc.player.motionX = 0;
            mc.player.motionZ = 0;
        }
        centeredBlock = BlockUtil.getCenterOfBlock(mc.player.posX, mc.player.posY, mc.player.posY);
    }

    // Rotation
    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<OnUpdateWalkingPlayerEvent> onUpdateWalkingPlayerEventListener = new Listener<>(event -> {
        if (event.getPhase() != Phase.PRE || !rotate.getValue() || lastHitVec == null || !forceRotation.getValue())
            return;
        Vec2f rotation = RotationUtil.getRotationTo(lastHitVec);
        PlayerPacket packet = new PlayerPacket(this, rotation);
        PlayerPacketManager.INSTANCE.addPacket(packet);
    });

    public void onDisable() {
        ROTATION_UTIL.onDisable();
        PlacementUtil.onDisable();

        if (mc.player == null) {
            return;
        }

        if (noObby) setDisabledMessage("Skull not found... AutoSkull turned OFF!");
        if (offHandSkull.getValue()) OffHand.removeItem(1);
    }

    private boolean firstShift;
    private int resetPhase;

    public void onUpdate() {
        if (mc.player == null) {
            disable();
            return;
        }

        if (noObby) {
            disable();
            return;
        }

        if (delayTimeTicks < tickDelay.getValue()) {
            delayTimeTicks++;
        } else {
            delayTimeTicks = 0;

            if (centerPlayer.getValue() && centeredBlock != Vec3d.ZERO && mc.player.onGround) {
                PlayerUtil.centerPlayer(centeredBlock);
            }

            // If we are at the stage of phasing
            if (toPhase) {
                // tickTry for rubberbanding shit
                if (++tickTry == maxTickTries.getValue()) {
                    mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, (int)mc.player.posY + 1, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, mc.player.onGround));
                    mc.player.posY = (int) mc.player.posY + 1;
                    disable();
                }
                // If we have the skull on us
                if (BlockUtil.getBlock(mc.player.posX, mc.player.posY, mc.player.posZ) instanceof BlockSkull) {
                    // Go down
                    if (!mc.player.onGround) {
                        mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY - (mc.player.posY - (int) mc.player.posY), mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, mc.player.onGround));
                        return;
                    }
                    // For stages
                    switch (stage) {
                        case 0:
                            // PacketFly
                            mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY - 0.001, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, mc.player.onGround));
                            mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY + 1000, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, mc.player.onGround));
                            break;
                    }
                } else {
                    // If we are done
                    if (BlockUtil.getBlock(mc.player.posX, mc.player.posY, mc.player.posZ).fullBlock && mc.player.posY - (int) mc.player.posY <= 0.5) {
                        disable();
                    }
                    // If not
                    if ((mc.player.posY - (int) mc.player.posY) > .5) {
                        // Place
                        placeBlock(false);
                    } else if (++resetPhase == 50)
                        // Disable
                        disable();
                }
                return;
            }

            if (onlyHoles.getValue() && HoleUtil.isHole(EntityUtil.getPosition(mc.player), true, true).getType() == HoleUtil.HoleType.NONE)
                return;

            ROTATION_UTIL.shouldSpoofAngles(true);

            // AutoTrap thing
            if (autoTrap.getValue() && BlockUtil.getBlock(new BlockPos(mc.player.getPosition().add(0, .4, 0))) instanceof BlockSkull) {
                // Get closest
                EntityPlayer closest = PlayerUtil.findClosestTarget(2, null);
                if (closest != null && (int) closest.posX == (int) mc.player.posX && (int) closest.posZ == (int) mc.player.posZ && closest.posY > mc.player.posY && closest.posY < mc.player.posY + 2) {
                    int blocksPlaced = 0;
                    int offsetSteps = 0;
                    // Start placing
                    while (blocksPlaced <= BlocksPerTick.getValue() && offsetSteps < 10) {
                        BlockPos offsetPos = new BlockPos(AIR[offsetSteps]);
                        BlockPos targetPos = mc.player.getPosition().add(offsetPos.getX(), offsetPos.getY(), offsetPos.getZ());
                        if (placeBlock(targetPos))
                            blocksPlaced++;
                        offsetSteps++;
                    }
                }
            }

            // Place if insta active
            if (instaActive.getValue()) {
                placeBlock(true);
                return;
            }

            // Only on shift
            if (onShift.getValue() && mc.gameSettings.keyBindSneak.isKeyDown()) {
                if (!firstShift)
                    placeBlock(true);
                return;
            // For not creating problems with shift
            } else if (firstShift && !mc.gameSettings.keyBindSneak.isKeyDown()) firstShift = false;

            // When we have to check for a player near to place
            if (playerDistance.getValue() != 0) {
                if (PlayerUtil.findClosestTarget(playerDistance.getValue(), null) != null) {
                    placeBlock(true);
                    return;
                }
            }
        }
    }

    private boolean placeBlock(BlockPos pos) {

        EnumHand handSwing = EnumHand.MAIN_HAND;

        int obsidianSlot = InventoryUtil.findObsidianSlot(false, false);

        if (mc.player.inventory.currentItem != obsidianSlot && obsidianSlot != 9) {
            mc.player.inventory.currentItem = obsidianSlot;
        }

        return PlacementUtil.place(pos, handSwing, rotate.getValue(), true);
    }

    private final ArrayList<EnumFacing> exd = new ArrayList<EnumFacing>() {
        {
            add(EnumFacing.DOWN);
            add(EnumFacing.UP);
        }
    };

    private void placeBlock(boolean changeStatus) {

        BlockPos pos = new BlockPos(mc.player.posX, mc.player.posY + .4, mc.player.posZ);
        // Check if is possible
        if (mc.world.getBlockState(pos).getMaterial().isReplaceable()) {
            EnumHand handSwing = EnumHand.MAIN_HAND;

            int skullSlot = InventoryUtil.findSkullSlot(offHandSkull.getValue(), activedBefore);

            if (skullSlot == -1) {
                noObby = true;
                return;
            }

            if (skullSlot == 9) {
                if (mc.player.getHeldItemOffhand().getItem() instanceof ItemSkull) {
                    // We can continue
                    handSwing = EnumHand.OFF_HAND;
                } else return;
            }

            if (mc.player.inventory.currentItem != skullSlot && skullSlot != 9) {
                oldSlot = mc.player.inventory.currentItem;
                mc.player.inventory.currentItem = skullSlot;
            }


            if (preSwitch.getValue() > 0 && preRotationTick++ == preSwitch.getValue()) {
                lastHitVec = new Vec3d(pos.x, pos.y, pos.z);
                return;
            }

            // Enter
            if ((alrPlaced && changeStatus) || (noUp.getValue() ? (PlacementUtil.place(pos, handSwing, rotate.getValue(), exd) || PlacementUtil.place(pos, handSwing, rotate.getValue()))
                : PlacementUtil.place(pos, handSwing, rotate.getValue()))) {
                // Set alrPlaced to true for entring insta after
                alrPlaced = true;
                // Set new lastHitVec
                if (afterSwitch.getValue() > 0 && afterRotationTick++ == afterSwitch.getValue()) {
                    lastHitVec = new Vec3d(pos.x, pos.y, pos.z);
                    return;
                }

                if (oldSlot != -1) {
                    mc.player.inventory.currentItem = oldSlot;
                    oldSlot = -1;
                }

                // At beginning
                if (changeStatus) {
                    // Set new values
                    firstShift = true;
                    activedBefore = alrPlaced = true;
                    // Remove offhand
                    if (offHandSkull.getValue())
                        OffHand.removeItem(1);

                    // Disables in these cases
                    if (disableAfter.getValue() && !phase.getValue()) {
                        disable();
                    }

                    // If phase
                    if (phase.getValue() && (mc.player.posY > 1)) {
                        // Active it
                        toPhase = true;
                        stage = 0;
                        // If we have to wait, just set that block in air
                        if (ServerRespond.getValue())
                            mc.world.setBlockToAir(new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ));
                        // Rubberband
                        if (predictPhase.getValue()) {
                            mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY - 1, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, mc.player.onGround));
                        }

                    }
                    preRotationTick = afterRotationTick = 0;
                    lastHitVec = null;
                    centeredBlock = Vec3d.ZERO;
                }
            } else lastHitVec = null;
        }
    }

}
