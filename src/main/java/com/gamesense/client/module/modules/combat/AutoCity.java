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

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author TechAle on 12/16/20
 * Ported and modified from Surround.java
 */

@Module.Declaration(name = "AutoCity", category = Category.Combat)
public class AutoCity extends Module {


    DoubleSetting range = registerDouble("Range", 6, 0, 8);
    IntegerSetting down = registerInteger("Down", 1, 0, 3);
    IntegerSetting sides = registerInteger("Sides", 1, 0, 4);
    IntegerSetting depth = registerInteger("Depth", 3, 0, 10);
    DoubleSetting minDamage = registerDouble("Min Damage", 5, 0, 10);
    DoubleSetting maxDamage = registerDouble("Max Self Damage", 7, 0, 20);
    BooleanSetting ignoreCrystals = registerBoolean("Ignore Crystals", true);
    ModeSetting target = registerMode("Target", Arrays.asList("Nearest", "Looking"), "Nearest");
    BooleanSetting switchPick = registerBoolean("Switch Pick", true);
    ModeSetting mineMode = registerMode("Mine Mode", Arrays.asList("Packet", "Vanilla"), "Packet");
    ModeSetting renderMode = registerMode("Render", Arrays.asList("Outline", "Fill", "Both", "None"), "Both");
    IntegerSetting width = registerInteger("Width", 1, 1, 10, () -> !renderMode.getValue().equals("None"));
    ColorSetting color = registerColor("Color", new GSColor(102, 51, 153), () -> !renderMode.getValue().equals("None"));

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
            /*
            if (placeCrystal.getValue()) {
                int slot = -1;
                EnumHand hand = EnumHand.MAIN_HAND;
                if (mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL)
                    hand = EnumHand.OFF_HAND;
                else {

                    slot = InventoryUtil.findFirstItemSlot(ItemEndCrystal.class, 0, 8);
                    if (slot != -1) {
                        mc.player.connection.sendPacket(new CPacketHeldItemChange(slot));
                    }
                }
                if (hand == EnumHand.OFF_HAND || slot != -1) {
                    mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(blockCrystal, EnumFacing.UP, hand, 0, 0, 0));
                    mc.player.swingArm(hand);
                }
            }
             */

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
            if (done) {
                disable();
            } else
            if (!packet) {
                breakBlock();
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

        List<BlockPos> blocks = EntityUtil.getBlocksIn(aimTarget);
        if (blocks.size() == 0) {
            blockInside = true;
            disable();
            return;
        }

        // find lowest point of the player
        int minY = Integer.MAX_VALUE;
        for (BlockPos block : blocks) {
            int y = block.getY();
            if (y < minY) {
                minY = y;
            }
        }
        if (aimTarget.posY % 1 > .2) {
            minY++;
        }

        int finalMinY = minY;
        blocks = blocks.stream().filter(blockPos -> blockPos.getY() == finalMinY).collect(Collectors.toList());

        Optional<BlockPos> any = blocks.stream().findAny();
        if (!any.isPresent()) {
            finalY = true;
            disable();
            return;
        }

        HoleUtil.HoleInfo holeInfo = HoleUtil.isHole(any.get(), false, true);
        if (holeInfo.getType() == HoleUtil.HoleType.NONE || holeInfo.getSafety() == HoleUtil.BlockSafety.UNBREAKABLE) {
            noHole = true;
            disable();
            return;
        }

        List<BlockPos> sides = new ArrayList<>();
        for (BlockPos block : blocks) {
            sides.addAll(cityableSides(block, HoleUtil.getUnsafeSides(block).keySet(), aimTarget));
        }

        if (sides.size() > 0) {
            blockMine = sides.get(0);
            /*
            blockCrystal = new BlockPos(blockMine.getX() - (aimTarget.posX - blockMine.getX()),
                                           blockMine.getY(),
                                         blockMine.getZ() - (aimTarget.posZ - blockMine.getZ()));*/
            double distance = mc.player.getDistanceSq(blockMine);
            for(BlockPos poss : sides) {
                if (mc.player.getDistanceSq(blockMine) < distance) {
                    blockMine = poss;
                    distance = mc.player.getDistanceSq(blockMine);
                    /*
                    blockCrystal = new BlockPos(poss.getZ() - (aimTarget.posZ - blockMine.getZ()),
                            poss.getY(),
                            blockMine.getZ() - (aimTarget.posZ - blockMine.getZ()));*/
                }
            }
        } else {
            noPossible = true;
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

    private List<BlockPos> cityableSides(BlockPos centre, Set<HoleUtil.BlockOffset> weakSides, EntityPlayer player) {
        List<BlockPos> cityableSides = new ArrayList<>();
        HashMap<BlockPos, HoleUtil.BlockOffset> directions = new HashMap<>();
        for (HoleUtil.BlockOffset weakSide : weakSides) {
            BlockPos pos = weakSide.offset(centre);
            if (mc.world.getBlockState(pos).getBlock() != Blocks.AIR) {
                directions.put(pos, weakSide);
            }
        }

        directions.forEach(((blockPos, blockOffset) -> {
            if (blockOffset == HoleUtil.BlockOffset.DOWN) {
                return;
            }

            BlockPos pos1 = blockOffset.left(blockPos.down(down.getValue()), sides.getValue());
            BlockPos pos2 = blockOffset.forward(blockOffset.right(blockPos, sides.getValue()), depth.getValue());
            List<BlockPos> square = EntityUtil.getSquare(pos1, pos2);

            IBlockState holder = mc.world.getBlockState(blockPos);
            mc.world.setBlockToAir(blockPos);


            for (BlockPos pos : square) {
                if (this.canPlaceCrystal(pos.down(), ignoreCrystals.getValue())) {

                    if (DamageUtil.calculateDamage((double) pos.getX() + 0.5d, pos.getY(), (double) pos.getZ() + 0.5d, player) >= minDamage.getValue()) {
                        if (DamageUtil.calculateDamage((double) pos.getX() + 0.5d, pos.getY(), (double) pos.getZ() + 0.5d, mc.player) <= maxDamage.getValue()) {
                            cityableSides.add(blockPos);
                        }
                        break;
                    }
                }
            }

            mc.world.setBlockState(blockPos, holder);
        }));

        return cityableSides;
    }

    private boolean canPlaceCrystal(BlockPos blockPos, boolean ignoreCrystal) {
        BlockPos boost = blockPos.add(0, 1, 0);
        BlockPos boost2 = blockPos.add(0, 2, 0);
        AxisAlignedBB axisAlignedBB = new AxisAlignedBB(boost, boost2);

        if (!(mc.world.getBlockState(blockPos).getBlock() == Blocks.BEDROCK
                || mc.world.getBlockState(blockPos).getBlock() == Blocks.OBSIDIAN)) {
            return false;
        }

        if (!(mc.world.getBlockState(boost).getBlock() == Blocks.AIR)) {
            return false;
        }

        if (!(mc.world.getBlockState(boost2).getBlock() == Blocks.AIR)) {
            return false;
        }

        if (!ignoreCrystal)
            return mc.world.getEntitiesWithinAABB(Entity.class, axisAlignedBB).isEmpty();
        else {
            List<Entity> entityList = mc.world.getEntitiesWithinAABB(Entity.class, axisAlignedBB);
            entityList.removeIf(entity -> entity instanceof EntityEnderCrystal);
            return entityList.isEmpty();
        }

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