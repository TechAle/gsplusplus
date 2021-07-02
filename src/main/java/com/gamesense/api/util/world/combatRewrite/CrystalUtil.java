package com.gamesense.api.util.world.combatRewrite;

import com.gamesense.api.util.player.PlayerUtil;
import com.gamesense.api.util.world.EntityUtil;
import com.gamesense.api.util.world.combatRewrite.ac.entityData.EntityInfo;
import com.gamesense.client.manager.managers.WorldCopyManager;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.stream.Collectors;

public class CrystalUtil {

    private static final Minecraft mc = Minecraft.getMinecraft();

    public static boolean canPlaceCrystal(BlockPos blockPos, boolean newPlacement) {
        if (notValidBlock(mc.world.getBlockState(blockPos).getBlock())) return false;

        BlockPos posUp = blockPos.up();

        if (newPlacement) {
            if (!mc.world.isAirBlock(posUp)) return false;
        } else {
            if (notValidMaterial(mc.world.getBlockState(posUp).getMaterial())
                || notValidMaterial(mc.world.getBlockState(posUp.up()).getMaterial())) return false;
        }

        AxisAlignedBB box = new AxisAlignedBB(
            posUp.getX(), posUp.getY(), posUp.getZ(),
            posUp.getX() + 1.0, posUp.getY() + 2.0, posUp.getZ() + 1.0
        );

        return mc.world.getEntitiesWithinAABB(Entity.class, box, Entity::isEntityAlive).isEmpty();
    }

    public static boolean canPlaceCrystal(BlockPos blockPos, boolean newPlacement, List<EntityInfo> entities) {
        if (notValidBlock(WorldCopyManager.INSTANCE.getBlockState(blockPos).getBlock())) return false;

        BlockPos posUp = blockPos.up();

        if (newPlacement) {
            if (!WorldCopyManager.INSTANCE.isAirBlock(posUp)) return false;
        } else {
            if (notValidMaterial(WorldCopyManager.INSTANCE.getBlockState(posUp).getMaterial())
                    || notValidMaterial(WorldCopyManager.INSTANCE.getBlockState(posUp.up()).getMaterial())) return false;
        }

        AxisAlignedBB box = new AxisAlignedBB(
                posUp.getX(), posUp.getY(), posUp.getZ(),
                posUp.getX() + 1.0, posUp.getY() + 2.0, posUp.getZ() + 1.0
        );

        for (EntityInfo entity : entities) {
            if (box.intersects(entity.aabb)) {
                return false;
            }
        }
        return true;
    }

    public static boolean notValidBlock(Block block) {
        return block != Blocks.BEDROCK && block != Blocks.OBSIDIAN;
    }

    public static boolean notValidMaterial(Material material) {
        return !material.isReplaceable();
    }

    public static List<BlockPos> findCrystalBlocks(float placeRange, boolean mode, List<EntityInfo> entities) {
        return EntityUtil.getSphere(PlayerUtil.getPlayerPos(), placeRange, (int) placeRange, false, true, 0).stream().filter(pos -> CrystalUtil.canPlaceCrystal(pos, mode, entities)).collect(Collectors.toList());
    }

    public static void breakCrystal(Entity crystal) {
        mc.playerController.attackEntity(mc.player, crystal);
        mc.player.swingArm(EnumHand.MAIN_HAND);
    }

    public static void breakCrystalPacket(Entity crystal) {
        mc.player.connection.sendPacket(new CPacketUseEntity(crystal));
        mc.player.swingArm(EnumHand.MAIN_HAND);
    }

}