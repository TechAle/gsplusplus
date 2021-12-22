package com.gamesense.client.module.modules.combat;

import com.gamesense.api.event.events.BlockChangeEvent;
import com.gamesense.api.event.events.RenderEvent;
import com.gamesense.api.event.events.TotemPopEvent;
import com.gamesense.api.setting.values.*;
import com.gamesense.api.util.misc.MessageBus;
import com.gamesense.api.util.player.InventoryUtil;
import com.gamesense.api.util.player.PlayerUtil;
import com.gamesense.api.util.render.GSColor;
import com.gamesense.api.util.render.RenderUtil;
import com.gamesense.api.util.world.BlockUtil;
import com.gamesense.api.util.world.EntityUtil;
import com.gamesense.api.util.world.GeometryMasks;
import com.gamesense.api.util.world.HoleUtil;
import com.gamesense.api.util.world.combat.CrystalUtil;
import com.gamesense.api.util.world.combat.DamageUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.ModuleManager;
import com.gamesense.client.module.modules.misc.AutoGG;
import com.mojang.realmsclient.gui.ChatFormatting;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemEndCrystal;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author TechAle on 12/16/20
 * Ported and modified from Surround.java
 */

@Module.Declaration(name = "AutoCity", category = Category.Combat)
public class AutoCity extends Module {


    DoubleSetting range = registerDouble("Range", 6, 0, 8);
    DoubleSetting minDamage = registerDouble("Min Damage", 5, 0, 10);
    DoubleSetting maxDamage = registerDouble("Max Self Damage", 7, 0, 20);
    ModeSetting target = registerMode("Target", Arrays.asList("Nearest", "Looking"), "Nearest");
    BooleanSetting switchPick = registerBoolean("Switch Pick", true);
    ModeSetting mineMode = registerMode("Mine Mode", Arrays.asList("Packet", "Vanilla"), "Packet");
    ModeSetting renderMode = registerMode("Render", Arrays.asList("Outline", "Fill", "Both", "None"), "Both");
    IntegerSetting width = registerInteger("Width", 1, 1, 10, () -> !renderMode.getValue().equals("None"));
    ColorSetting color = registerColor("Color", new GSColor(102, 51, 153), () -> !renderMode.getValue().equals("None"));
    BooleanSetting newPlace = registerBoolean("New Place", false);
    BooleanSetting disableAfter = registerBoolean("Disable After", true);

    private BlockPos blockMine,
                     blockCrystal;
    private int oldSlot;
    private EntityPlayer aimTarget;
    private boolean isMining;
    private boolean packet;
    private boolean blockInside,
                    finalY,
                    noHole,
                    noPossible,
                    done;


    public void onEnable() {
        resetValues();
    }

    void resetValues() {
        aimTarget = null;
        blockMine = blockCrystal = null;
        isMining = packet = blockInside = finalY = noHole = noPossible = done = false;
    }

    public void onDisable() {
        if (mc.player == null) {
            return;
        }

        if (blockInside) setDisabledMessage("Detected block inside... AutoCity turned OFF!");
        else if (noHole) setDisabledMessage("Enemy is not in a hole... AutoCity turned OFF!");
        else if (finalY) setDisabledMessage("Not correct y... AutoCity turned OFF!");
        else if (noPossible) setDisabledMessage("Enemy moved away from the hole... AutoCity turned OFF!");
        else setDisabledMessage("AutoCity turned OFF!");

    }
    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<BlockChangeEvent> totemPopEventListener = new Listener<>(event -> {
        if (mc.player == null || mc.world == null) return;

        if (event.getBlock() == null || event.getPosition() == null || blockMine == null) return;

        if (event.getPosition() == blockMine && event.getBlock() instanceof BlockAir) {

            if (!packet)
                if (oldSlot != -1)
                    mc.player.inventory.currentItem = oldSlot;
            done = true;
        }

    });


    public void onUpdate() {
        if (mc.player == null || mc.world == null)
            return;

        if (isMining) {

            if (BlockUtil.getBlock(blockMine) instanceof BlockAir) {
               resetValues();
                if (disableAfter.getValue())
                    disable();
                if (!packet && oldSlot != -1)
                    mc.player.inventory.currentItem = oldSlot;
            } else {
                if (done) {
                    if (disableAfter.getValue())
                        disable();
                } else if (!packet) {
                    breakBlock();
                }
            }
            return;

        }

        // All the setup
        if (target.getValue().equals("Nearest"))
            aimTarget = PlayerUtil.findClosestTarget(range.getValue(), aimTarget);
        else if (target.getValue().equals("Looking"))
            aimTarget = PlayerUtil.findLookingPlayer(range.getValue());

        if (aimTarget == null) {
            return;
        }

        boolean found = false;
        for(int[] positions : new int[][] {
                {1,0,0},
                {-1,0,0},
                {0,0,1},
                {0,0,-1}
        }) {
            BlockPos blockPos = new BlockPos(aimTarget.posX + positions[0], aimTarget.posY + positions[1] + (aimTarget.posY % 1 > 0.2 ? .5 : 0), aimTarget.posZ + positions[2]);
            if (BlockUtil.getBlock(blockPos) instanceof BlockAir || BlockUtil.getBlock(blockPos).blockResistance > 6001)
                continue;
            // For calculating the damage, set to air
            Block toReplace = BlockUtil.getBlock(blockPos);
            mc.world.setBlockToAir(blockPos);
            // Check around
            for (Vec3i placement : new Vec3i[]{
                    new Vec3i(1, -1, 0),
                    new Vec3i(-1, -1, 0),
                    new Vec3i(0, -1, 1),
                    new Vec3i(0, -1, -1)
            }) {
                // If we can place the crystal
                BlockPos temp;
                if (CrystalUtil.canPlaceCrystal((temp = blockPos.add(placement)), newPlace.getValue())) {

                    // Check damage
                    if (DamageUtil.calculateDamage(temp.getX() + .5D, temp.getY() + 1D, temp.getZ() + .5D, mc.player, false) >= maxDamage.getValue())
                        continue;


                    // Calculate damage
                    float damagePlayer = DamageUtil.calculateDamage(temp.getX() + .5D, temp.getY() + 1D, temp.getZ() + .5D,
                            aimTarget, false);

                    if (damagePlayer < minDamage.getValue())
                        continue;

                    found = true;
                    blockMine = blockPos;
                    break;
                }
            }

            // Reset surround
            mc.world.setBlockState(blockPos, toReplace.getDefaultState());
            if (found)
                break;
        }

        if (!found) {
            noPossible = true;
            if (disableAfter.getValue())
                disable();
            return;
        }



        if (mc.player.getHeldItemMainhand().getItem() != Items.DIAMOND_PICKAXE && switchPick.getValue()) {
            oldSlot = mc.player.inventory.currentItem;
            int slot = InventoryUtil.findFirstItemSlot(ItemPickaxe.class, 0, 9);
            if (slot != 1)
                mc.player.inventory.currentItem = slot;
        }

        switch (mineMode.getValue()) {
            case "Packet": {
                mc.player.swingArm(EnumHand.MAIN_HAND);
                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, blockMine, EnumFacing.UP));
                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, blockMine, EnumFacing.UP));
                isMining = true;
                packet = true;
                mc.player.inventory.currentItem = oldSlot;
            }
            case "Vanilla": {
                breakBlock();
                isMining = true;
            }
            default: {
                breakBlock();
                isMining = true;
            }
        }

    }


    private void breakBlock() {
        mc.player.swingArm(EnumHand.MAIN_HAND);
        mc.playerController.onPlayerDamageBlock(blockMine, EnumFacing.UP);

    }

    public void onWorldRender(RenderEvent event) {
        if (blockMine == null)
            return;
        renderBox(blockMine);
    }

    private void renderBox(BlockPos blockPos) {
        GSColor gsColor1 = new GSColor(color.getValue(), 255);
        GSColor gsColor2 = new GSColor(color.getValue(), 50);

        switch (renderMode.getValue()) {
            case "Both": {
                RenderUtil.drawBox(blockPos, 1, gsColor2, GeometryMasks.Quad.ALL);
                RenderUtil.drawBoundingBox(blockPos, 1, width.getValue(), gsColor1);
                break;
            }
            case "Outline": {
                RenderUtil.drawBoundingBox(blockPos, 1, width.getValue(), gsColor1);
                break;
            }
            case "Fill": {
                RenderUtil.drawBox(blockPos, 1, gsColor2, GeometryMasks.Quad.ALL);
                break;
            }
            case "None": {

            }
        }
    }


}