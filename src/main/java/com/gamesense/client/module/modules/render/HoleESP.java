package com.gamesense.client.module.modules.render;

import com.gamesense.api.event.events.RenderEvent;
import com.gamesense.api.setting.values.*;
import com.gamesense.api.util.player.PlayerUtil;
import com.gamesense.api.util.render.GSColor;
import com.gamesense.api.util.render.RenderUtil;
import com.gamesense.api.util.world.EntityUtil;
import com.gamesense.api.util.world.GeometryMasks;
import com.gamesense.api.util.world.HoleUtil;
import com.gamesense.client.manager.managers.WorldCopyManager;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.google.common.collect.Sets;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.*;

/**
 * @reworked by 0b00101010 on 14/01/2021
 */

@Module.Declaration(name = "HoleESP", category = Category.Render)
public class HoleESP extends Module {

    public IntegerSetting range = registerInteger("Range", 5, 1, 50);
    ModeSetting customHoles = registerMode("Show", Arrays.asList("Single", "Double", "Custom"), "Single");
    ModeSetting type = registerMode("Render", Arrays.asList("Outline", "Fill", "Both"), "Both");
    BooleanSetting fillRaytrace = registerBoolean("Fill raytrace", false);
    ModeSetting mode = registerMode("Mode", Arrays.asList("Air", "Ground", "Flat", "Slab", "Double"), "Air");
    BooleanSetting hideOwn = registerBoolean("Hide Own", false);
    BooleanSetting flatOwn = registerBoolean("Flat Own", false);
    DoubleSetting slabHeight = registerDouble("Slab Height", 0.5, 0.1, 1.5);
    IntegerSetting width = registerInteger("Width", 1, 1, 10);
    ColorSetting bedrockColor = registerColor("Bedrock Color", new GSColor(0, 255, 0));
    ColorSetting obsidianColor = registerColor("Obsidian Color", new GSColor(255, 0, 0));
    ColorSetting customColor = registerColor("Custom Color", new GSColor(0, 0, 255));
    IntegerSetting ufoAlpha = registerInteger("UFOAlpha", 255, 0, 255);

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private Future<HashMap<AxisAlignedBB, GSColor>> output;
    private HashMap<AxisAlignedBB, GSColor> current;

    public void onUpdate() {
        if (mc.player == null || mc.world == null) {
            return;
        }

        if (output == null) {
            output = executor.submit(new HoleESPExecutor(this, PlayerUtil.getPlayerPos()));
        }
    }

    public void onWorldRender(RenderEvent event) {
        if (mc.player == null || mc.world == null) {
            return;
        }

        if (output != null)
        {
            if (output.isCancelled()) {
                output = null;
            } else if (output.isDone()) {
                try {
                    current = output.get();
                } catch (InterruptedException | ExecutionException ignored) {
                }
                output = null;
            }
        }

        if (current != null) {
            current.forEach(this::renderHoles);
        }
    }

    private void renderHoles(AxisAlignedBB hole, GSColor color) {
        switch (type.getValue()) {
            case "Outline": {
                renderOutline(hole, color);
                break;
            }
            case "Fill": {
                renderFill(hole, color);
                break;
            }
            case "Both": {
                if ( fillRaytrace.getValue() && mc.world.rayTraceBlocks(hole.getCenter(), new Vec3d(mc.player.posX, mc.player.posY + (double) mc.player.getEyeHeight() + 1, mc.player.posZ)) == null)
                    renderFill(hole, color);
                renderOutline(hole, color);
                break;
            }
        }
    }

    private void renderFill(AxisAlignedBB hole, GSColor color) {
        GSColor fillColor = new GSColor(color, 50);
        int ufoAlpha = (this.ufoAlpha.getValue() * 50) / 255;

        if (hideOwn.getValue() && hole.intersects(mc.player.getEntityBoundingBox())) return;

        switch (mode.getValue()) {
            case "Air": {
                if (flatOwn.getValue() && hole.intersects(mc.player.getEntityBoundingBox())) {
                    RenderUtil.drawBox(hole, true, 1, fillColor, ufoAlpha, GeometryMasks.Quad.DOWN);
                } else {
                    RenderUtil.drawBox(hole, true, 1, fillColor, ufoAlpha, GeometryMasks.Quad.ALL);
                }
                break;
            }
            case "Ground": {
                RenderUtil.drawBox(hole.offset(0, -1, 0), true, 1, new GSColor(fillColor, ufoAlpha), fillColor.getAlpha(), GeometryMasks.Quad.ALL);
                break;
            }
            case "Flat": {
                RenderUtil.drawBox(hole, true, 1, fillColor, ufoAlpha, GeometryMasks.Quad.DOWN);
                break;
            }
            case "Slab": {
                if (flatOwn.getValue() && hole.intersects(mc.player.getEntityBoundingBox())) {
                    RenderUtil.drawBox(hole, true, 1, fillColor, ufoAlpha, GeometryMasks.Quad.DOWN);
                } else {
                    RenderUtil.drawBox(hole, false, slabHeight.getValue(), fillColor, ufoAlpha, GeometryMasks.Quad.ALL);
                }
                break;
            }
            case "Double": {
                if (flatOwn.getValue() && hole.intersects(mc.player.getEntityBoundingBox())) {
                    RenderUtil.drawBox(hole, true, 1, fillColor, ufoAlpha, GeometryMasks.Quad.DOWN);
                } else {
                    RenderUtil.drawBox(hole.setMaxY(hole.maxY + 1), true, 2, fillColor, ufoAlpha, GeometryMasks.Quad.ALL);
                }
                break;
            }
        }
    }

    private void renderOutline(AxisAlignedBB hole, GSColor color) {
        GSColor outlineColor = new GSColor(color, 255);

        if (hideOwn.getValue() && hole.intersects(mc.player.getEntityBoundingBox())) return;

        switch (mode.getValue()) {
            case "Air": {
                if (flatOwn.getValue() && hole.intersects(mc.player.getEntityBoundingBox())) {
                    RenderUtil.drawBoundingBoxWithSides(hole, width.getValue(), outlineColor, ufoAlpha.getValue(), GeometryMasks.Quad.DOWN);
                } else {
                    RenderUtil.drawBoundingBox(hole, width.getValue(), outlineColor, ufoAlpha.getValue());
                }
                break;
            }
            case "Ground": {
                RenderUtil.drawBoundingBox(hole.offset(0, -1, 0), width.getValue(), new GSColor(outlineColor, ufoAlpha.getValue()), outlineColor.getAlpha());
                break;
            }
            case "Flat": {
                RenderUtil.drawBoundingBoxWithSides(hole, width.getValue(), outlineColor, ufoAlpha.getValue(), GeometryMasks.Quad.DOWN);
                break;
            }
            case "Slab": {
                if (this.flatOwn.getValue() && hole.intersects(mc.player.getEntityBoundingBox())) {
                    RenderUtil.drawBoundingBoxWithSides(hole, width.getValue(), outlineColor, ufoAlpha.getValue(), GeometryMasks.Quad.DOWN);
                } else {
                    RenderUtil.drawBoundingBox(hole.setMaxY(hole.minY + slabHeight.getValue()), width.getValue(), outlineColor, ufoAlpha.getValue());
                }
                break;
            }
            case "Double": {
                if (this.flatOwn.getValue() && hole.intersects(mc.player.getEntityBoundingBox())) {
                    RenderUtil.drawBoundingBoxWithSides(hole, width.getValue(), outlineColor, ufoAlpha.getValue(), GeometryMasks.Quad.DOWN);
                } else {
                    RenderUtil.drawBoundingBox(hole.setMaxY(hole.maxY + 1), width.getValue(), outlineColor, ufoAlpha.getValue());
                }
                break;
            }
        }
    }

    private static class HoleESPExecutor implements Callable<HashMap<AxisAlignedBB, GSColor>> {
        private static final IBlockAccess world = WorldCopyManager.INSTANCE;

        private final HoleESP parent;
        private final BlockPos player;

        public HoleESPExecutor(HoleESP parent, BlockPos player) {
            this.parent = parent;
            this.player = player;
        }

        @Override
        public HashMap<AxisAlignedBB, GSColor> call() {
            HashMap<AxisAlignedBB, GSColor> holes = new HashMap<>();
            int range = (int) Math.ceil(parent.range.getValue());

            HashSet<BlockPos> possibleHoles = Sets.newHashSet();
            List<BlockPos> blockPosList = EntityUtil.getSphere(player, range, range, false, true, 0);

            WorldCopyManager.INSTANCE.lock.readLock().lock();
            try {
                for (BlockPos pos : blockPosList) {
                    if (!world.getBlockState(pos).getBlock().equals(Blocks.AIR)) {
                        continue;
                    }
                    if (world.getBlockState(pos.add(0, -1, 0)).getBlock().equals(Blocks.AIR)) {
                        continue;
                    }
                    if (!world.getBlockState(pos.add(0, 1, 0)).getBlock().equals(Blocks.AIR)) {
                        continue;
                    }

                    if (world.getBlockState(pos.add(0, 2, 0)).getBlock().equals(Blocks.AIR)) {
                        possibleHoles.add(pos);
                    }
                }

                possibleHoles.forEach(pos -> {
                    HoleUtil.HoleInfo holeInfo = HoleUtil.isHole(pos, false, false, world);
                    HoleUtil.HoleType holeType = holeInfo.getType();
                    if (holeType != HoleUtil.HoleType.NONE) {

                        HoleUtil.BlockSafety holeSafety = holeInfo.getSafety();
                        AxisAlignedBB centreBlocks = holeInfo.getCentre();

                        if (centreBlocks == null)
                            return;

                        GSColor colour;

                        if (holeSafety == HoleUtil.BlockSafety.UNBREAKABLE) {
                            colour = new GSColor(parent.bedrockColor.getValue(), 255);
                        } else {
                            colour = new GSColor(parent.obsidianColor.getValue(), 255);
                        }
                        if (holeType == HoleUtil.HoleType.CUSTOM) {
                            colour = new GSColor(parent.customColor.getValue(), 255);
                        }

                        String mode = parent.customHoles.getValue();
                        if (mode.equalsIgnoreCase("Custom") && (holeType == HoleUtil.HoleType.CUSTOM || holeType == HoleUtil.HoleType.DOUBLE)) {
                            holes.put(centreBlocks, colour);
                        } else if (mode.equalsIgnoreCase("Double") && holeType == HoleUtil.HoleType.DOUBLE) {
                            holes.put(centreBlocks, colour);
                        } else if (holeType == HoleUtil.HoleType.SINGLE) {
                            holes.put(centreBlocks, colour);
                        }
                    }
                });
            } finally {
                WorldCopyManager.INSTANCE.lock.readLock().unlock();
            }

            return holes;
        }
    }
}