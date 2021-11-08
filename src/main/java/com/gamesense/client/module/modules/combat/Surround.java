package com.gamesense.client.module.modules.combat;

import com.gamesense.api.event.events.PlayerJumpEvent;
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
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Arrays;

import static java.lang.Math.*;

/**
 * @author Hoosiers
 * @author Doogie13
 * @since 03/29/2021
 * @since 2021 08 14
 */

@Module.Declaration(name = "Surround", category = Category.Combat)
public class Surround extends Module {

    BooleanSetting enableHole = registerBoolean("Enable in Hole", false);
    ModeSetting jumpMode = registerMode("Jump", Arrays.asList("Continue", "Pause", "Disable"), "Continue");
    ModeSetting offsetMode = registerMode("Pattern", Arrays.asList("Normal", "Minimum", "Anti City"), "Normal");
    IntegerSetting delayTicks = registerInteger("Tick Delay", 3, 0, 10);
    IntegerSetting blocksPerTick = registerInteger("Blocks Per Tick", 4, 1, 8);
    BooleanSetting rotate = registerBoolean("Rotate", true);
    BooleanSetting centre = registerBoolean("Centre", false);
    BooleanSetting sneakOnly = registerBoolean("Sneak Only", false);
    BooleanSetting disableNoBlock = registerBoolean("Disable No Obby", true);
    BooleanSetting offhandObby = registerBoolean("Offhand Obby", false);

    private final Timer delayTimer = new Timer();
    private final Timer enableTimer = new Timer();
    boolean hasPlaced;
    private Vec3d centeredBlock = Vec3d.ZERO;
    private int oldSlot = -1;
    private int offsetSteps = 0;
    private boolean outOfTargetBlock = false;
    private boolean activedOff = false;
    float y;

    @EventHandler
    private final Listener<PlayerJumpEvent> listener = new Listener<>(event -> disable());

    public void onEnable() {

        y = (float) mc.player.posY;

        PlacementUtil.onEnable();
        if (mc.player == null || mc.world == null) {
            disable();
            return;
        }

        centeredBlock = BlockUtil.getCenterOfBlock(mc.player.posX, mc.player.posY, mc.player.posZ);

        oldSlot = mc.player.inventory.currentItem;
    }

    public void onDisable() {
        PlacementUtil.onDisable();
        if (mc.player == null | mc.world == null) return;

        if (outOfTargetBlock) setDisabledMessage("No valid blocks detected... Surround turned OFF!");

        if (oldSlot != mc.player.inventory.currentItem && oldSlot != -1 && oldSlot != 9) {
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

        if (mc.player.posY > y + 0.4 /*slab is 0.5*/ && !(mc.player.isInWeb)) {
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

        y = (float) mc.player.posY;

        int targetBlockSlot = InventoryUtil.findCrystalBlockSlot(offhandObby.getValue(), activedOff);

        if ((outOfTargetBlock || targetBlockSlot == -1) && disableNoBlock.getValue()) {
            outOfTargetBlock = true;
            disable();
            return;
        }

        activedOff = true;

        if (HoleUtil.isHole((new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ)), true, true).getType().equals(HoleUtil.HoleType.NONE) && centre.getValue()) {
            PlayerUtil.centerPlayer(mc.player.getPositionVector());
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
                    offsetPattern = Offsets.SURROUND_MIN;
                    placeMinBlocks();
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
        }
    }

    private boolean placeBlock(BlockPos pos) {
        EnumHand handSwing = EnumHand.MAIN_HAND;

        int targetBlockSlot = InventoryUtil.findCrystalBlockSlot(offhandObby.getValue(), activedOff);

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
            if (!hasPlaced) {
                mc.player.connection.sendPacket(new CPacketHeldItemChange(targetBlockSlot));
                hasPlaced = true;
            }
        }

        boolean r = PlacementUtil.place(pos, handSwing, rotate.getValue(), false);

        mc.player.connection.sendPacket(new CPacketHeldItemChange(oldSlot));

        return r;
    }

    void placeMinBlocks() {

        Vec3d[] vec = Offsets.SURROUND_MIN;

        for (Vec3d vec3d : vec) {

            if (SurroundRewrite.getDown(new BlockPos(vec3d))) {

                placeBlock(new BlockPos(vec3d.add(0d, -1d, 0d)));

            }

        }
    }

    @Override
    public void onDisabledUpdate() {
        if (!HoleUtil.isHole(new BlockPos (mc.player.getPositionVector()), true, false).getType().equals(HoleUtil.HoleType.NONE) && enableHole.getValue()) {
            if (enableTimer.hasReached(150, true))
                enable();

        } else
            enableTimer.reset();
    }
}