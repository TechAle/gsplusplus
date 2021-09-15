package com.gamesense.client.module.modules.combat;

import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.misc.Timer;
import com.gamesense.api.util.player.InventoryUtil;
import com.gamesense.api.util.player.PlacementUtil;
import com.gamesense.api.util.player.PlayerUtil;
import com.gamesense.api.util.world.BlockUtil;
import com.gamesense.api.util.world.HoleUtil;
import com.gamesense.api.util.world.Offsets;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.ModuleManager;
import net.minecraft.block.BlockObsidian;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Hoosiers
 * @since 03/29/2021
 */

@Module.Declaration(name = "Surround", category = Category.Combat)
public class Surround extends Module {

    ModeSetting jumpMode = registerMode("Jump", Arrays.asList("Continue", "Pause", "Disable"), "Continue");
    ModeSetting offsetMode = registerMode("Pattern", Arrays.asList("Normal", "Minimum","Anti City"), "Normal");
    IntegerSetting delayTicks = registerInteger("Tick Delay", 3, 0, 10);
    IntegerSetting blocksPerTick = registerInteger("Blocks Per Tick", 4, 1, 8);
    BooleanSetting rotate = registerBoolean("Rotate", true);
    ModeSetting centreMode = registerMode("Center Mode", Arrays.asList("Snap", "Motion", "None"), "Snap");
    BooleanSetting sneakOnly = registerBoolean("Sneak Only", false);
    BooleanSetting disableNoBlock = registerBoolean("Disable No Obby", true);
    BooleanSetting offhandObby = registerBoolean("Offhand Obby", false);
    BooleanSetting silentSwitch = registerBoolean("Silent Switch", false);

    private final Timer delayTimer = new Timer();
    private Vec3d centeredBlock = Vec3d.ZERO;

    private int oldSlot = -1;
    private int offsetSteps = 0;
    private boolean outOfTargetBlock = false;
    private boolean activedOff = false;
    boolean hasPlaced;

    public void onEnable() {
        PlacementUtil.onEnable();
        if (mc.player == null || mc.world == null) {
            disable();
            return;
        }

        if (centreMode.getValue().equalsIgnoreCase("Motion")) {
            mc.player.motionX = 0;
            mc.player.motionZ = 0;
        }

        centeredBlock = BlockUtil.getCenterOfBlock(mc.player.posX, mc.player.posY, mc.player.posZ);

        oldSlot = mc.player.inventory.currentItem;
    }

    public void onDisable() {
        PlacementUtil.onDisable();
        if (mc.player == null | mc.world == null) return;

        if (outOfTargetBlock) setDisabledMessage("No valid blocks detected... Surround turned OFF!");

        if (oldSlot != mc.player.inventory.currentItem && oldSlot != -1 && oldSlot != 9) {
            mc.player.inventory.currentItem = oldSlot;
            oldSlot = -1;
        }

        AutoCrystalRewrite.stopAC = false;

        if (offhandObby.getValue() && ModuleManager.isModuleEnabled(OffHand.class)) {
            OffHand.removeItem(0);
            activedOff = false;
        }

        centeredBlock = Vec3d.ZERO;
        outOfTargetBlock = false;
    }

    public void onUpdate() {
        if (mc.player == null || mc.world == null) {
            disable();
            return;
        }

        if (sneakOnly.getValue() && !mc.player.isSneaking()) {
            return;
        }

        if (!(mc.player.onGround) && !(mc.player.isInWeb)) {
            switch (jumpMode.getValue()) {
                case "Pause": {
                    return;
                }
                case "Disable": {
                    disable();
                    return;
                }
                default: {
                    break;
                }
            }
        }

        int targetBlockSlot = InventoryUtil.findObsidianSlot(offhandObby.getValue(), activedOff);

        if ((outOfTargetBlock || targetBlockSlot == -1) && disableNoBlock.getValue()) {
            outOfTargetBlock = true;
            disable();
            return;
        }

        activedOff = true;

        if (HoleUtil.isHole((new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ)), true, true).getType().equals(HoleUtil.HoleType.NONE)) {

            // if statement to check if we need to center. needs work
            if (intersects(mc.player)) {

                if (centreMode.getValue().equalsIgnoreCase("Motion")) {
                    PlayerUtil.centerPlayer(centeredBlock);
                } else if (centreMode.getValue().equalsIgnoreCase("Snap")) {

                    mc.player.connection.sendPacket(new CPacketPlayer.Position(Math.floor(mc.player.posX) + 0.5, mc.player.posY, Math.floor(mc.player.posZ) + 0.5, true));
                    mc.player.setPositionAndUpdate(Math.floor(mc.player.posX) + 0.5, mc.player.posY, Math.floor(mc.player.posZ) + 0.5); // Updating makes it look different lol

                }
            }

        }

        if (delayTimer.getTimePassed() / 50L >= delayTicks.getValue()) {
            delayTimer.reset();

            int blocksPlaced = 0;

            hasPlaced = false;

            while (blocksPlaced <= blocksPerTick.getValue()) {
                int maxSteps;
                Object[] offsetPattern;

                if ("Anti City".equals(offsetMode.getValue())) {
                    offsetPattern = Offsets.SURROUND_CITY;
                    maxSteps = Offsets.SURROUND_CITY.length;
                } else if ("Normal".equals(offsetMode.getValue())) {
                    offsetPattern = Offsets.SURROUND;
                    maxSteps = Offsets.SURROUND.length;
                } else {
                    offsetPattern = getSurroundMinVec();
                    maxSteps = offsetPattern.length;
                }

                if (offsetSteps >= maxSteps) {
                    offsetSteps = 0;
                    break;
                }

                BlockPos offsetPos = new BlockPos((Vec3d) offsetPattern[offsetSteps]);
                BlockPos targetPos = new BlockPos(mc.player.getPositionVector()).add(offsetPos.getX(), offsetPos.getY(), offsetPos.getZ());

                boolean tryPlacing = true;

                if (mc.player.posY % 1 > 0.2) {
                    targetPos = new BlockPos(targetPos.getX(), targetPos.getY() + 1, targetPos.getZ());
                }

                if (!mc.world.getBlockState(targetPos).getMaterial().isReplaceable()) {
                    tryPlacing = false;
                }

                for (Entity entity : mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(targetPos))) {
                    if (entity instanceof EntityPlayer) {
                        tryPlacing = false;
                        break;
                    }
                }

                if (tryPlacing && placeBlock(targetPos)) {
                    blocksPlaced++;
                }

                offsetSteps++;
            }

            if (hasPlaced)
                mc.player.connection.sendPacket(new CPacketHeldItemChange(oldSlot));
        }
    }

    private boolean intersects(Entity pl) {

        return true; // will be made to check collision

    }

    private boolean placeBlock(BlockPos pos) {
        EnumHand handSwing = EnumHand.MAIN_HAND;

        int targetBlockSlot = InventoryUtil.findObsidianSlot(offhandObby.getValue(), activedOff);

        if (targetBlockSlot == -1) {
            outOfTargetBlock = true;
            return false;
        }

        if (targetBlockSlot == 9) {
            activedOff = true;
            if (mc.player.getHeldItemOffhand().getItem() instanceof ItemBlock && ((ItemBlock) mc.player.getHeldItemOffhand().getItem()).getBlock().blockHardness > 6) {
                handSwing = EnumHand.OFF_HAND;
            } else return false;
        }

        if (mc.player.inventory.currentItem != targetBlockSlot && targetBlockSlot != 9) {
            if (silentSwitch.getValue()) {
                if (!hasPlaced) {
                    mc.player.connection.sendPacket(new CPacketHeldItemChange(targetBlockSlot));
                    hasPlaced = true;
                }
            } else
                mc.player.inventory.currentItem = targetBlockSlot;
        }

        return PlacementUtil.place(pos, handSwing, rotate.getValue(), !silentSwitch.getValue());
    }

    Object[] getSurroundMinVec() {

        Vec3d[] vec = Offsets.SURROUND_MIN;

        ArrayList<Integer> list = new ArrayList<Integer>();
        ArrayList<Vec3d> vl = new ArrayList<Vec3d>();

        for (Vec3d vec3d : vec) {

            if (BlockUtil.getPlaceableSide(new BlockPos(vec3d)) == null)
                list.add(0);
            else
                list.add(1);

        }

        for (int i = 0; i < list.size(); i++) {

            if (list.get(i) != 0) {

                if (list.get(i) == 1) {
                    switch (i) {
                        case 0:
                            new Vec3d(-1, -1, 0);
                        case 1:
                            new Vec3d(1, -1, 0);
                        case 2:
                            new Vec3d(0, -1, -1);
                        case 3:
                            new Vec3d(0, -1, 1);
                    }
                }

            }

        }

        Collections.addAll(vl, vec);

        return vl.toArray();

    }

}