package com.gamesense.client.module.modules.combat;

import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.misc.Timer;
import com.gamesense.api.util.player.InventoryUtil;
import com.gamesense.api.util.player.PlacementUtil;
import com.gamesense.api.util.player.PlayerUtil;
import com.gamesense.api.util.world.Offsets;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.ModuleManager;
import net.minecraft.block.BlockObsidian;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;

@Module.Declaration(name = "ForceBurrow", category = Category.Combat)
public class ForceBurrow extends Module {

    private final Timer delayTimer = new Timer();

    ModeSetting targetMode = registerMode("Target", Arrays.asList("Nearest", "Looking"), "Nearest");
    IntegerSetting enemyRange = registerInteger("Range", 4, 0, 6);
    IntegerSetting delayTicks = registerInteger("Tick Delay", 3, 0, 10);
    IntegerSetting blocksPerTick = registerInteger("Blocks Per Tick", 4, 1, 8);
    BooleanSetting rotate = registerBoolean("Rotate", true);
    BooleanSetting sneakOnly = registerBoolean("Sneak Only", false);
    BooleanSetting disableNoBlock = registerBoolean("Disable No Obby", true);
    BooleanSetting offhandObby = registerBoolean("Offhand Obby", false);
    BooleanSetting silentSwitch = registerBoolean("Silent Switch", false);

    int secureSilentSwitch = -1;
    boolean hasPlaced;
    int phase;
    private EntityPlayer targetPlayer = null;
    private int oldSlot = -1;
    private int offsetSteps = 0;
    private boolean outOfTargetBlock = false;
    private boolean activedOff = false;

    @Override
    protected void onEnable() {

        phase = 0;

        PlacementUtil.onEnable();
        if (mc.player == null || mc.world == null) {
            disable();
            return;
        }

        oldSlot = mc.player.inventory.currentItem;
        secureSilentSwitch = -1;
    }

    public void onDisable() {
        PlacementUtil.onDisable();
        if (mc.player == null | mc.world == null) return;

        if (outOfTargetBlock) setDisabledMessage("No obsidian detected... AutoTrap turned OFF!");

        if (oldSlot != mc.player.inventory.currentItem && oldSlot != -1 && oldSlot != 9) {
            mc.player.inventory.currentItem = oldSlot;
            oldSlot = -1;
        }

        AutoCrystalRewrite.stopAC = false;

        if (offhandObby.getValue() && ModuleManager.isModuleEnabled(OffHand.class)) {
            OffHand.removeItem(0);
            activedOff = false;
        }

        outOfTargetBlock = false;
        targetPlayer = null;
    }

    public void onUpdate() {
        if (mc.player == null || mc.world == null) {
            disable();
            return;
        }

        switch (phase) {
            case 0: {
                if (sneakOnly.getValue() && !mc.player.isSneaking()) {
                    return;
                }

                int targetBlockSlot = InventoryUtil.findCrystalBlockSlot(offhandObby.getValue(), activedOff);

                if ((outOfTargetBlock || targetBlockSlot == -1) && disableNoBlock.getValue()) {
                    outOfTargetBlock = true;
                    disable();
                    return;
                }

                activedOff = true;

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

                    hasPlaced = false;

                    while (blocksPlaced <= blocksPerTick.getValue()) {
                        int maxSteps;
                        Vec3d[] offsetPattern;

                        offsetPattern = Offsets.TRAP_BURROW;
                        maxSteps = Offsets.TRAP_BURROW.length;


                        if (offsetSteps >= maxSteps) {
                            offsetSteps = 0;
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
                    if (hasPlaced) {
                        mc.player.connection.sendPacket(new CPacketHeldItemChange(oldSlot));
                        phase = 1;
                    }
                }
            }
            case 1: {

                int newSlot = InventoryUtil.findFirstBlockSlot(Blocks.ANVIL.getClass(), 0, 8);
                int oldSlot = mc.player.inventory.currentItem;

                mc.player.connection.sendPacket(new CPacketHeldItemChange(newSlot));
                mc.player.inventory.currentItem = newSlot;

                placeBlock(new BlockPos(targetPlayer.getPositionVector()).add(0,2,0));

                mc.player.connection.sendPacket(new CPacketHeldItemChange(oldSlot));
                mc.player.inventory.currentItem = oldSlot;

                disable();
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
            if (mc.player.getHeldItemOffhand().getItem() instanceof ItemBlock && ((ItemBlock) mc.player.getHeldItemOffhand().getItem()).getBlock() instanceof BlockObsidian) {
                handSwing = EnumHand.OFF_HAND;
            } else return false;
        }

        if (mc.player.inventory.currentItem != targetBlockSlot && targetBlockSlot != 9) {
            if (silentSwitch.getValue()) {
                if (!hasPlaced) {
                    secureSilentSwitch = mc.player.inventory.currentItem;
                    mc.player.connection.sendPacket(new CPacketHeldItemChange(targetBlockSlot));
                    hasPlaced = true;
                }
            } else
                mc.player.inventory.currentItem = targetBlockSlot;
        }

        return PlacementUtil.place(pos, handSwing, rotate.getValue(), !silentSwitch.getValue());
    }

}
