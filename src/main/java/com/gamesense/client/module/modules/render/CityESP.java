package com.gamesense.client.module.modules.render;

import com.gamesense.api.event.events.RenderEvent;
import com.gamesense.api.setting.values.*;
import com.gamesense.api.util.player.social.SocialManager;
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
import com.gamesense.client.module.modules.combat.Friends;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author Hoosiers
 * @author 0b00101010
 * @since 10/20/2020
 * @since 01/30/2021
 */

@Module.Declaration(name = "CityESP", category = Category.Render)
public class CityESP extends Module {

    IntegerSetting range = registerInteger("Range", 20, 1, 30);
    ModeSetting selectMode = registerMode("Select", Arrays.asList("Closest", "All"), "Closest");
    ModeSetting renderMode = registerMode("Render", Arrays.asList("Outline", "Fill", "Both"), "Both");
    BooleanSetting self = registerBoolean("Self", false);
    IntegerSetting width = registerInteger("Width", 1, 1, 10);
    ColorSetting color = registerColor("Color", new GSColor(102, 51, 153));
    BooleanSetting newPlace = registerBoolean("New Place", false);
    DoubleSetting maxSelfDamage = registerDouble("Max Self Damage", 6, 0, 20);
    DoubleSetting minDamage = registerDouble("Min Damage", 6, 0, 20);

    public void onWorldRender(RenderEvent event) {
        if (mc.player != null && mc.world != null) {
            mc.world.playerEntities.stream()
                    .filter(entityPlayer -> entityPlayer.getDistance(mc.player) <= range.getValue())
                    .filter(entityPlayer ->  !SocialManager.isFriend(entityPlayer.getName()))
                    .filter(entityPlayer -> self.getValue() || entityPlayer != mc.player)
                    .forEach(entityPlayer -> {

                        for(int[] positions : new int[][] {
                                {1,0,0},
                                {-1,0,0},
                                {0,0,1},
                                {0,0,-1}
                        }) {
                            BlockPos blockPos = new BlockPos(entityPlayer.posX + positions[0], entityPlayer.posY + positions[1], entityPlayer.posZ + positions[2]);
                            if (BlockUtil.getBlock(blockPos) instanceof BlockAir)
                                continue;
                            // Best
                            BlockPos coords = null;
                            double damage = Double.MIN_VALUE;
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
                                    if (DamageUtil.calculateDamage(temp.getX() + .5D, temp.getY() + 1D, temp.getZ() + .5D, mc.player, false) >= maxSelfDamage.getValue())
                                        continue;


                                    // Calculate damage
                                    float damagePlayer = DamageUtil.calculateDamage(temp.getX() + .5D, temp.getY() + 1D, temp.getZ() + .5D,
                                            entityPlayer, false);

                                    if (damagePlayer < minDamage.getValue())
                                        continue;

                                    renderBox2(blockPos);
                                    break;
                                }
                            }

                            // Reset surround
                            mc.world.setBlockState(blockPos, toReplace.getDefaultState());
                        }
                    });
        }
    }


    //this doesn't check if there is a block below the target block, might add it later if people want it
    private List<BlockPos> getBlocksToRender(EntityPlayer entityPlayer) {
        NonNullList<BlockPos> blockPosList = NonNullList.create();
        BlockPos blockPos = new BlockPos(entityPlayer.posX, entityPlayer.posY, entityPlayer.posZ);

        if (mc.world.getBlockState(blockPos.east()).getBlock() != Blocks.BEDROCK) {
            blockPosList.add(blockPos.east());
        }
        if (mc.world.getBlockState(blockPos.west()).getBlock() != Blocks.BEDROCK) {
            blockPosList.add(blockPos.west());
        }
        if (mc.world.getBlockState(blockPos.north()).getBlock() != Blocks.BEDROCK) {
            blockPosList.add(blockPos.north());
        }
        if (mc.world.getBlockState(blockPos.south()).getBlock() != Blocks.BEDROCK) {
            blockPosList.add(blockPos.south());
        }

        return blockPosList;
    }

    private void renderBox(List<BlockPos> blockPosList) {
        switch (selectMode.getValue()) {
            case "Closest": {
                BlockPos renderPos = blockPosList.stream().sorted(Comparator.comparing(blockPos -> blockPos.getDistance((int) mc.player.posX, (int) mc.player.posY, (int) mc.player.posZ))).findFirst().orElse(null);

                if (renderPos != null) {
                    renderBox2(renderPos);
                }
                break;
            }
            case "All": {
                for (BlockPos blockPos : blockPosList) {
                    renderBox2(blockPos);
                }
                break;
            }
        }
    }

    private void renderBox2(BlockPos blockPos) {
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
        }
    }
}