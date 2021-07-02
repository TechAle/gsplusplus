package com.gamesense.api.util.world.combatRewrite.ac;

import com.gamesense.api.util.world.combatRewrite.DamageUtil;
import com.gamesense.api.util.world.combatRewrite.ac.entityData.EntityInfo;
import com.gamesense.api.util.world.combatRewrite.ac.entityData.PlayerInfo;
import com.gamesense.client.manager.managers.WorldCopyManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class ACUtil {
    public static CrystalInfo.PlaceInfo calculateBestPlacement(ACSettings settings, PlayerInfo target, List<BlockPos> possibleLocations) {
        double x = settings.player.position.x;
        double y = settings.player.position.y;
        double z = settings.player.position.z;

        // get the best crystal for the player
        BlockPos best = null;
        float bestDamage = 0f;
        final double enemyRangeSq = settings.enemyRange * settings.enemyRange;
        final float fullHealth = target.getHealth() + target.getAbsorption();
        for (BlockPos crystal : possibleLocations) {
            if (!(canFakeCrystalBeSeen(crystal, settings.player) || (settings.player.position.squareDistanceTo(crystal.getX() + 0.5d, crystal.getY() + 1.0d, crystal.getZ() + 0.5d) < settings.wallsRange))) {
                continue;
            }
            // if player is out of range of this crystal, do nothing
            if (target.position.squareDistanceTo((double) crystal.getX() + 0.5d, (double) crystal.getY() + 1.0d, (double) crystal.getZ() + 0.5d) <= enemyRangeSq) {
                float currentDamage = DamageUtil.calculateDamageThreaded((double) crystal.getX() + 0.5d, (double) crystal.getY() + 1.0d, (double) crystal.getZ() + 0.5d, target);
                if (currentDamage == bestDamage) {
                    // this new crystal is closer
                    // higher chance of being able to break it
                    if (best == null || crystal.distanceSq(x, y, z) < best.distanceSq(x, y, z)) {
                        bestDamage = currentDamage;
                        best = crystal;
                    }
                } else if (currentDamage > bestDamage) {
                    bestDamage = currentDamage;
                    best = crystal;
                }
            }
        }

        if (best != null) {
            if (bestDamage >= settings.minDmg || ((fullHealth <= settings.facePlaceValue || target.lowArmour) && bestDamage >= settings.minFacePlaceDmg)) {
                return new CrystalInfo.PlaceInfo(bestDamage, target, best);
            }
        }

        return null;
    }

    public static CrystalInfo.BreakInfo calculateBestBreakable(ACSettings settings, PlayerInfo target, List<EntityInfo> crystals) {
        double x = settings.player.position.x;
        double y = settings.player.position.y;
        double z = settings.player.position.z;

        EntityInfo best = null;
        float bestDamage = 0f;
        final boolean smart = settings.breakMode.equalsIgnoreCase("Smart");
        final float fullHealth = target.getHealth() + target.getAbsorption();
        for (EntityInfo crystal : crystals) {
            if (!(canCrystalBeSeen(crystal, settings.player) || (settings.player.position.distanceTo(crystal.position) < settings.wallsRange))) {
                continue;
            }
            float currentDamage = DamageUtil.calculateDamageThreaded(crystal.position.x, crystal.position.y, crystal.position.z, target);
            if (currentDamage == bestDamage) {
                // this new crystal is closer
                // higher chance of being able to break it
                if (best == null || crystal.position.squareDistanceTo(x, y, z) < best.position.squareDistanceTo(x, y, z)) {
                    bestDamage = currentDamage;
                    best = crystal;
                }
            } else if (currentDamage > bestDamage) {
                bestDamage = currentDamage;
                best = crystal;
            }
        }

        if (best != null) {
            boolean shouldAdd = false;
            if (smart) {
                if ((double) bestDamage >= settings.minBreakDmg || ((fullHealth <= settings.facePlaceValue || target.lowArmour) && bestDamage > settings.minFacePlaceDmg)) {
                    shouldAdd = true;
                }
            } else {
                shouldAdd = true;
            }

            if (shouldAdd) {
                return new CrystalInfo.BreakInfo(bestDamage, target, best);
            }
        }

        return null;
    }

    public static boolean canCrystalBeSeen(EntityInfo entity, PlayerInfo player) {
        return DamageUtil.rayTraceBlocks(player.position.add(0, player.getEyeHeight(), 0), entity.position.add(0, entity.height * 0.85D, 0), WorldCopyManager.INSTANCE) == null;
    }

    public static boolean canFakeCrystalBeSeen(BlockPos pos, PlayerInfo player) {
        return DamageUtil.rayTraceBlocks(player.position.add(0, player.getEyeHeight(), 0), new Vec3d(pos.getX() + 0.5d, (double)pos.getY() + 2.7D, pos.getZ() + 0.5d), WorldCopyManager.INSTANCE) == null;
    }
}
