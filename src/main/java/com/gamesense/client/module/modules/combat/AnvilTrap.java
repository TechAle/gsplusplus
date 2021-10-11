package com.gamesense.client.module.modules.combat;

import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.misc.MessageBus;
import com.gamesense.api.util.misc.Timer;
import com.gamesense.api.util.player.InventoryUtil;
import com.gamesense.api.util.player.PlacementUtil;
import com.gamesense.api.util.player.PlayerUtil;
import com.gamesense.api.util.world.BlockUtil;
import com.gamesense.api.util.world.Offsets;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.ModuleManager;
import com.gamesense.client.module.modules.gui.ColorMain;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockAnvil;
import net.minecraft.block.BlockObsidian;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;

/**
 * @author Hoosiers
 * @author TechAle
 * @since 04/03/2021
 */

@Module.Declaration(name = "AnvilTrap", category = Category.Combat)
public class AnvilTrap extends Module {

    ModeSetting targetMode = registerMode("Target", Arrays.asList("Nearest", "Looking"), "Nearest");
    IntegerSetting enemyRange = registerInteger("Range", 4, 0, 6);
    IntegerSetting delayTicks = registerInteger("Tick Delay", 3, 0, 10);
    IntegerSetting blocksPerTick = registerInteger("Blocks Per Tick", 4, 1, 8);
    BooleanSetting rotate = registerBoolean("Rotate", true);
    BooleanSetting sneakOnly = registerBoolean("Sneak Only", false);
    BooleanSetting disableNoBlock = registerBoolean("Disable No Obby", true);
    BooleanSetting offhandObby = registerBoolean("Offhand Obby", false);
    BooleanSetting silentSwitch = registerBoolean("Silent Switch", false);
    BooleanSetting safeAnvil = registerBoolean("Safe Anvil", true);

    private final Timer delayTimer = new Timer();
    private EntityPlayer targetPlayer = null;

    private int oldSlot = -1;
    private int offsetSteps = 0;
    private boolean outOfTargetBlock = false;
    private boolean activedOff = false;
    int secureSilentSwitch = -1;
    boolean hasPlaced;

    public void onEnable() {
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

                offsetPattern = Offsets.TRAP_CRYSTAL_ANVIL;
                maxSteps = Offsets.TRAP_CRYSTAL_ANVIL.length;


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

            if ( (safeAnvil.getValue() && blocksPlaced <= blocksPerTick.getValue())|| blocksPlaced == 0) {

                if (!(BlockUtil.getBlock(new BlockPos(targetVec3d)) instanceof BlockAnvil)) {
                    boolean found = false;
                    for (Entity t : mc.world.loadedEntityList) {
                        // If it's a falling block
                        if (t instanceof EntityFallingBlock) {
                            Block ex = ((EntityFallingBlock) t).fallTile.getBlock();
                            // If it's anvil
                            if (ex instanceof BlockAnvil
                                    // If coords are the same as us
                                    && (int) t.posX == (int) targetPlayer.posX && (int) t.posZ == (int) targetPlayer.posZ) {
                                // Place the block
                                found = true;
                                break;
                            }
                        }
                    }

                    if (!found) {
                        int slotAnvil = InventoryUtil.findFirstBlockSlot(BlockAnvil.class, 0, 8);
                        if (slotAnvil != -1) {
                            mc.player.connection.sendPacket(new CPacketHeldItemChange(slotAnvil));
                            BlockPos targetPos = new BlockPos(targetVec3d).add(0, 2, 0);
                            PlacementUtil.place(targetPos, EnumHand.MAIN_HAND, rotate.getValue(), !silentSwitch.getValue());
                            if (!silentSwitch.getValue())
                                mc.player.connection.sendPacket(new CPacketHeldItemChange(mc.player.inventory.currentItem));
                        }
                    }
                }
            }

            if (hasPlaced)
                mc.player.connection.sendPacket(new CPacketHeldItemChange(oldSlot));
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