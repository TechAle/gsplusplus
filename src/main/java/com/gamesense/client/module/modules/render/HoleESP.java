package com.gamesense.client.module.modules.render;

import com.gamesense.api.event.events.RenderEvent;
import com.gamesense.api.setting.values.*;
import com.gamesense.api.util.player.PlayerUtil;
import com.gamesense.api.util.render.GSColor;
import com.gamesense.api.util.render.RenderUtil;
import com.gamesense.api.util.world.EntityUtil;
import com.gamesense.api.util.world.GeometryMasks;
import com.gamesense.api.util.world.HoleUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.google.common.collect.Sets;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @reworked by 0b00101010 on 14/01/2021
 * Added gradiant by TechAle
 */

@Module.Declaration(name = "HoleESP", category = Category.Render)
public class HoleESP extends Module {

    public IntegerSetting range = registerInteger("Range", 5, 1, 20);
    ModeSetting customHoles = registerMode("Show", Arrays.asList("Single", "Double", "Custom"), "Single");
    ModeSetting type = registerMode("Render", Arrays.asList("Outline", "Fill", "Both"), "Both");

    //region outline bedrock
    // Custom outline
    BooleanSetting bOutLineSection = registerBoolean("OutLine Section Bedrock", false,
            () ->  type.getValue().equals("Outline") || type.getValue().equals("Both"));
    // Bottom
    ModeSetting bNVerticesOutlineBot = registerMode("bN^ Vertices Outline Bot", Arrays.asList("1", "2", "4"), "4",
            () -> (type.getValue().equals("Outline") || type.getValue().equals("Both")) && bOutLineSection.getValue());
    ModeSetting bdirection2OutLineBot = registerMode("bDirection Outline Bot", Arrays.asList("X", "Z"), "X",
            () ->   (type.getValue().equals("Outline") || type.getValue().equals("Both")) &&
                    bOutLineSection.getValue() && bNVerticesOutlineBot.getValue().equals("2"));
    ColorSetting bfirstVerticeOutlineBot = registerColor("b1 Vert Out Bot", new GSColor(255, 16, 19, 255),
            () ->   (type.getValue().equals("Outline") || type.getValue().equals("Both")) &&
                    bOutLineSection.getValue()
                    , true);
    ColorSetting bsecondVerticeOutlineBot = registerColor("b2 Vert Out Bot", new GSColor(0, 0, 255, 255),
            () ->   (type.getValue().equals("Outline") || type.getValue().equals("Both")) &&
                    bOutLineSection.getValue()
                    && (bNVerticesOutlineBot.getValue().equals("2") || bNVerticesOutlineBot.getValue().equals("4")), true);
    ColorSetting bthirdVerticeOutlineBot = registerColor("b3 Vert Out Bot", new GSColor(0, 255, 128, 255),
            () ->   (type.getValue().equals("Outline") || type.getValue().equals("Both")) &&
                    bOutLineSection.getValue()
                    && bNVerticesOutlineBot.getValue().equals("4"), true);
    ColorSetting bfourVerticeOutlineBot = registerColor("b4 Vert Out Bot", new GSColor(255, 255, 2, 255),
            () ->   (type.getValue().equals("Outline") || type.getValue().equals("Both")) &&
                    bOutLineSection.getValue()
                    && bNVerticesOutlineBot.getValue().equals("4"), true);
    // Top
    ModeSetting bNVerticesOutlineTop = registerMode("bN^ Vertices Outline Top", Arrays.asList("1", "2", "4"), "4",
            () ->   (type.getValue().equals("Outline") || type.getValue().equals("Both")) &&
                    bOutLineSection.getValue());
    ModeSetting bdirection2OutLineTop = registerMode("bDirection Outline Top", Arrays.asList("X", "Z"), "X",
            () ->   (type.getValue().equals("Outline") || type.getValue().equals("Both")) &&
                    bOutLineSection.getValue() && bNVerticesOutlineTop.getValue().equals("2"));
    ColorSetting bfirstVerticeOutlineTop = registerColor("b1 Vert Out Top", new GSColor(255, 16, 19, 255),
            () ->   (type.getValue().equals("Outline") || type.getValue().equals("Both")) &&
                    bOutLineSection.getValue(), true);
    ColorSetting bsecondVerticeOutlineTop = registerColor("b2 Vert Out Top", new GSColor(0, 0, 255, 255),
            () ->   (type.getValue().equals("Outline") || type.getValue().equals("Both")) &&
                    bOutLineSection.getValue()
                    && (bNVerticesOutlineTop.getValue().equals("2") || bNVerticesOutlineTop.getValue().equals("4")), true);
    ColorSetting bthirdVerticeOutlineTop = registerColor("b3 Vert Out Top", new GSColor(0, 255, 128, 255),
            () ->   (type.getValue().equals("Outline") || type.getValue().equals("Both")) &&
                    bOutLineSection.getValue()
                    && bNVerticesOutlineTop.getValue().equals("4"), true);
    ColorSetting bfourVerticeOutlineTop = registerColor("b4 Vert Out Top", new GSColor(255, 255, 2, 255),
            () ->   (type.getValue().equals("Outline") || type.getValue().equals("Both")) &&
                    bOutLineSection.getValue()
                    && bNVerticesOutlineTop.getValue().equals("4"), true);
    //endregion
    // region fill bedrock
    BooleanSetting bFillSection = registerBoolean("Fill Section Bedrock", false,
            () ->  type.getValue().equals("Fill") || type.getValue().equals("Both"));
    // Bottom
    ModeSetting bNVerticesFillBot = registerMode("bN^ Vertices Fill Bot", Arrays.asList("1", "2", "4"), "4",
            () -> (type.getValue().equals("Fill") || type.getValue().equals("Both")) && bFillSection.getValue());
    ModeSetting bdirection2FillBot = registerMode("bDirection Fill Bot", Arrays.asList("X", "Z"), "X",
            () ->   (type.getValue().equals("Fill") || type.getValue().equals("Both")) &&
                    bFillSection.getValue() && bNVerticesFillBot.getValue().equals("2"));
    ColorSetting bfirstVerticeFillBot = registerColor("b1 Vert Fill Bot", new GSColor(255, 16, 19, 255),
            () ->   (type.getValue().equals("Fill") || type.getValue().equals("Both")) &&
                    bFillSection.getValue()
                    , true);
    ColorSetting bsecondVerticeFillBot = registerColor("b2 Vert Fill Bot", new GSColor(0, 0, 255, 255),
            () ->   (type.getValue().equals("Fill") || type.getValue().equals("Both")) &&
                    bFillSection.getValue()
                    && (bNVerticesFillBot.getValue().equals("2") || bNVerticesFillBot.getValue().equals("4")), true);
    ColorSetting bthirdVerticeFillBot = registerColor("b3 Vert Fill Bot", new GSColor(0, 255, 128, 255),
            () ->   (type.getValue().equals("Fill") || type.getValue().equals("Both")) &&
                    bFillSection.getValue()
                    && bNVerticesFillBot.getValue().equals("4"), true);
    ColorSetting bfourVerticeFillBot = registerColor("b4 Vert Fill Bot", new GSColor(255, 255, 2, 255),
            () ->   (type.getValue().equals("Fill") || type.getValue().equals("Both")) &&
                    bFillSection.getValue()
                    && bNVerticesFillBot.getValue().equals("4"), true);
    // Top
    ModeSetting bNVerticesFillTop = registerMode("N^ Vertices Fill Top", Arrays.asList("1", "2", "4"), "4",
            () ->   (type.getValue().equals("Fill") || type.getValue().equals("Both")) &&
                    bFillSection.getValue());
    ModeSetting bdirection2FillTop = registerMode("bDirection Fill Top", Arrays.asList("X", "Z"), "X",
            () ->   (type.getValue().equals("Fill") || type.getValue().equals("Both")) &&
                    bFillSection.getValue() && bNVerticesFillTop.getValue().equals("2"));
    ColorSetting bfirstVerticeFillTop = registerColor("b1 Vert Fill Top", new GSColor(255, 16, 19, 255),
            () ->   (type.getValue().equals("Fill") || type.getValue().equals("Both")) &&
                    bFillSection.getValue()
            , true);
    ColorSetting bsecondVerticeFillTop = registerColor("b2 Vert Fill Top", new GSColor(0, 0, 255, 255),
            () ->   (type.getValue().equals("Fill") || type.getValue().equals("Both")) &&
                    bFillSection.getValue()
                    && (bNVerticesFillTop.getValue().equals("2") || bNVerticesFillTop.getValue().equals("4")), true);
    ColorSetting bthirdVerticeFillTop = registerColor("b3 Vert Fill Top", new GSColor(0, 255, 128, 255),
            () ->   (type.getValue().equals("Fill") || type.getValue().equals("Both")) &&
                    bFillSection.getValue()
                    && bNVerticesFillTop.getValue().equals("4"), true);
    ColorSetting bfourVerticeFillTop = registerColor("b4 Vert Fill Top", new GSColor(255, 255, 2, 255),
            () ->   (type.getValue().equals("Fill") || type.getValue().equals("Both")) &&
                    bFillSection.getValue()
                    && bNVerticesFillTop.getValue().equals("4"), true);
    //endregion

    //region outline obby
    // Custom outline
    BooleanSetting oOutLineSection = registerBoolean("OutLine Section Obsidian", false,
            () ->  type.getValue().equals("Outline") || type.getValue().equals("Both"));
    // Bottom
    ModeSetting oNVerticesOutlineBot = registerMode("N^ Vertices Outline Bot", Arrays.asList("1", "2", "4"), "4",
            () -> (type.getValue().equals("Outline") || type.getValue().equals("Both")) && oOutLineSection.getValue());
    ModeSetting odirection2OutLineBot = registerMode("Direction Outline Bot", Arrays.asList("X", "Z"), "X",
            () ->   (type.getValue().equals("Outline") || type.getValue().equals("Both")) &&
                    oOutLineSection.getValue() && oNVerticesOutlineBot.getValue().equals("2"));
    ColorSetting ofirstVerticeOutlineBot = registerColor("1 Vert Out Bot", new GSColor(255, 16, 19, 255),
            () ->   (type.getValue().equals("Outline") || type.getValue().equals("Both")) &&
                    oOutLineSection.getValue()
                    , true);
    ColorSetting osecondVerticeOutlineBot = registerColor("2 Vert Out Bot", new GSColor(0, 0, 255, 255),
            () ->   (type.getValue().equals("Outline") || type.getValue().equals("Both")) &&
                    oOutLineSection.getValue()
                    && (oNVerticesOutlineBot.getValue().equals("2") || oNVerticesOutlineBot.getValue().equals("4")), true);
    ColorSetting othirdVerticeOutlineBot = registerColor("3 Vert Out Bot", new GSColor(0, 255, 128, 255),
            () ->   (type.getValue().equals("Outline") || type.getValue().equals("Both")) &&
                    oOutLineSection.getValue()
                    && oNVerticesOutlineBot.getValue().equals("4"), true);
    ColorSetting ofourVerticeOutlineBot = registerColor("4 Vert Out Bot", new GSColor(255, 255, 2, 255),
            () ->   (type.getValue().equals("Outline") || type.getValue().equals("Both")) &&
                    oOutLineSection.getValue()
                    && oNVerticesOutlineBot.getValue().equals("4"), true);
    // Top
    ModeSetting oNVerticesOutlineTop = registerMode("N^ Vertices Outline Top", Arrays.asList("1", "2", "4"), "4",
            () ->   (type.getValue().equals("Outline") || type.getValue().equals("Both")) &&
                    oOutLineSection.getValue());
    ModeSetting odirection2OutLineTop = registerMode("Direction Outline Top", Arrays.asList("X", "Z"), "X",
            () ->   (type.getValue().equals("Outline") || type.getValue().equals("Both")) &&
                    oOutLineSection.getValue() && oNVerticesOutlineTop.getValue().equals("2"));
    ColorSetting ofirstVerticeOutlineTop = registerColor("1 Vert Out Top", new GSColor(255, 16, 19, 255),
            () ->   (type.getValue().equals("Outline") || type.getValue().equals("Both")) &&
                    oOutLineSection.getValue(), true);
    ColorSetting osecondVerticeOutlineTop = registerColor("2 Vert Out Top", new GSColor(0, 0, 255, 255),
            () ->   (type.getValue().equals("Outline") || type.getValue().equals("Both")) &&
                    oOutLineSection.getValue()
                    && (oNVerticesOutlineTop.getValue().equals("2") || oNVerticesOutlineTop.getValue().equals("4")), true);
    ColorSetting othirdVerticeOutlineTop = registerColor("3 Vert Out Top", new GSColor(0, 255, 128, 255),
            () ->   (type.getValue().equals("Outline") || type.getValue().equals("Both")) &&
                    oOutLineSection.getValue()
                    && oNVerticesOutlineTop.getValue().equals("4"), true);
    ColorSetting ofourVerticeOutlineTop = registerColor("4 Vert Out Top", new GSColor(255, 255, 2, 255),
            () ->   (type.getValue().equals("Outline") || type.getValue().equals("Both")) &&
                    oOutLineSection.getValue()
                    && oNVerticesOutlineTop.getValue().equals("4"), true);
    //endregion
    // region fill obby
    BooleanSetting oFillSection = registerBoolean("Fill Section Obsidian", false,
            () ->  type.getValue().equals("Fill") || type.getValue().equals("Both"));
    // Bottom
    ModeSetting oNVerticesFillBot = registerMode("N^ Vertices Fill Bot", Arrays.asList("1", "2", "4"), "4",
            () -> (type.getValue().equals("Fill") || type.getValue().equals("Both")) && oFillSection.getValue());
    ModeSetting odirection2FillBot = registerMode("Direction Fill Bot", Arrays.asList("X", "Z"), "X",
            () ->   (type.getValue().equals("Fill") || type.getValue().equals("Both")) &&
                    oFillSection.getValue() && oNVerticesFillBot.getValue().equals("2"));
    ColorSetting ofirstVerticeFillBot = registerColor("1 Vert Fill Bot", new GSColor(255, 16, 19, 255),
            () ->   (type.getValue().equals("Fill") || type.getValue().equals("Both")) &&
                    oFillSection.getValue()
                    , true);
    ColorSetting osecondVerticeFillBot = registerColor("2 Vert Fill Bot", new GSColor(0, 0, 255, 255),
            () ->   (type.getValue().equals("Fill") || type.getValue().equals("Both")) &&
                    oFillSection.getValue()
                    && (oNVerticesFillBot.getValue().equals("2") || oNVerticesFillBot.getValue().equals("4")), true);
    ColorSetting othirdVerticeFillBot = registerColor("3 Vert Fill Bot", new GSColor(0, 255, 128, 255),
            () ->   (type.getValue().equals("Fill") || type.getValue().equals("Both")) &&
                    oFillSection.getValue()
                    && oNVerticesFillBot.getValue().equals("4"), true);
    ColorSetting ofourVerticeFillBot = registerColor("4 Vert Fill Bot", new GSColor(255, 255, 2, 255),
            () ->   (type.getValue().equals("Fill") || type.getValue().equals("Both")) &&
                    oFillSection.getValue()
                    && oNVerticesFillBot.getValue().equals("4"), true);
    // Top
    ModeSetting oNVerticesFillTop = registerMode("N^ Vertices Fill Top", Arrays.asList("1", "2", "4"), "4",
            () ->   (type.getValue().equals("Fill") || type.getValue().equals("Both")) &&
                    oFillSection.getValue());
    ModeSetting odirection2FillTop = registerMode("Direction Fill Top", Arrays.asList("X", "Z"), "X",
            () ->   (type.getValue().equals("Fill") || type.getValue().equals("Both")) &&
                    oFillSection.getValue() && oNVerticesFillTop.getValue().equals("2"));
    ColorSetting ofirstVerticeFillTop = registerColor("1 Vert Fill Top", new GSColor(255, 16, 19, 255),
            () ->   (type.getValue().equals("Fill") || type.getValue().equals("Both")) &&
                    oFillSection.getValue(), true);
    ColorSetting osecondVerticeFillTop = registerColor("2 Vert Fill Top", new GSColor(0, 0, 255, 255),
            () ->   (type.getValue().equals("Fill") || type.getValue().equals("Both")) &&
                    oFillSection.getValue()
                    && (oNVerticesFillTop.getValue().equals("2") || oNVerticesFillTop.getValue().equals("4")), true);
    ColorSetting othirdVerticeFillTop = registerColor("3 Vert Fill Top", new GSColor(0, 255, 128, 255),
            () ->   (type.getValue().equals("Fill") || type.getValue().equals("Both")) &&
                    oFillSection.getValue()
                    && oNVerticesFillTop.getValue().equals("4"), true);
    ColorSetting ofourVerticeFillTop = registerColor("4 Vert Fill Top", new GSColor(255, 255, 2, 255),
            () ->   (type.getValue().equals("Fill") || type.getValue().equals("Both")) &&
                    oFillSection.getValue()
                    && oNVerticesFillTop.getValue().equals("4"), true);
    //endregion

    //region outline custom
    // Custom outline
    BooleanSetting OutLineSection = registerBoolean("OutLine Section Custom", false,
            () ->  type.getValue().equals("Outline") || type.getValue().equals("Both"));
    // Bottom
    ModeSetting NVerticesOutlineBot = registerMode("N^ Vertices Outline Bot", Arrays.asList("1", "2", "4"), "4",
            () -> (type.getValue().equals("Outline") || type.getValue().equals("Both")) && OutLineSection.getValue());
    ModeSetting direction2OutLineBot = registerMode("Direction Outline Bot", Arrays.asList("X", "Z"), "X",
            () ->   (type.getValue().equals("Outline") || type.getValue().equals("Both")) &&
                    OutLineSection.getValue() && NVerticesOutlineBot.getValue().equals("2"));
    ColorSetting firstVerticeOutlineBot = registerColor("1 Vert Out Bot", new GSColor(255, 16, 19, 255),
            () ->   (type.getValue().equals("Outline") || type.getValue().equals("Both")) &&
                    OutLineSection.getValue()
                    , true);
    ColorSetting secondVerticeOutlineBot = registerColor("2 Vert Out Bot", new GSColor(0, 0, 255, 255),
            () ->   (type.getValue().equals("Outline") || type.getValue().equals("Both")) &&
                    OutLineSection.getValue()
                    && (NVerticesOutlineBot.getValue().equals("2") || NVerticesOutlineBot.getValue().equals("4")), true);
    ColorSetting thirdVerticeOutlineBot = registerColor("3 Vert Out Bot", new GSColor(0, 255, 128, 255),
            () ->   (type.getValue().equals("Outline") || type.getValue().equals("Both")) &&
                    OutLineSection.getValue()
                    && NVerticesOutlineBot.getValue().equals("4"), true);
    ColorSetting fourVerticeOutlineBot = registerColor("4 Vert Out Bot", new GSColor(255, 255, 2, 255),
            () ->   (type.getValue().equals("Outline") || type.getValue().equals("Both")) &&
                    OutLineSection.getValue()
                    && NVerticesOutlineBot.getValue().equals("4"), true);
    // Top
    ModeSetting NVerticesOutlineTop = registerMode("N^ Vertices Outline Top", Arrays.asList("1", "2", "4"), "4",
            () ->   (type.getValue().equals("Outline") || type.getValue().equals("Both")) &&
                    OutLineSection.getValue());
    ModeSetting direction2OutLineTop = registerMode("Direction Outline Top", Arrays.asList("X", "Z"), "X",
            () ->   (type.getValue().equals("Outline") || type.getValue().equals("Both")) &&
                    OutLineSection.getValue() && NVerticesOutlineTop.getValue().equals("2"));
    ColorSetting firstVerticeOutlineTop = registerColor("1 Vert Out Top", new GSColor(255, 16, 19, 255),
            () ->   (type.getValue().equals("Outline") || type.getValue().equals("Both")) &&
                    OutLineSection.getValue(), true);
    ColorSetting secondVerticeOutlineTop = registerColor("2 Vert Out Top", new GSColor(0, 0, 255, 255),
            () ->   (type.getValue().equals("Outline") || type.getValue().equals("Both")) &&
                    OutLineSection.getValue()
                    && (NVerticesOutlineTop.getValue().equals("2") || NVerticesOutlineTop.getValue().equals("4")), true);
    ColorSetting thirdVerticeOutlineTop = registerColor("3 Vert Out Top", new GSColor(0, 255, 128, 255),
            () ->   (type.getValue().equals("Outline") || type.getValue().equals("Both")) &&
                    OutLineSection.getValue()
                    && NVerticesOutlineTop.getValue().equals("4"), true);
    ColorSetting fourVerticeOutlineTop = registerColor("4 Vert Out Top", new GSColor(255, 255, 2, 255),
            () ->   (type.getValue().equals("Outline") || type.getValue().equals("Both")) &&
                    OutLineSection.getValue()
                    && NVerticesOutlineTop.getValue().equals("4"), true);
    //endregion
    // region fill custom
    BooleanSetting FillSection = registerBoolean("Fill Section Custom", false,
            () ->  type.getValue().equals("Fill") || type.getValue().equals("Both"));
    // Bottom
    ModeSetting NVerticesFillBot = registerMode("N^ Vertices Fill Bot", Arrays.asList("1", "2", "4"), "4",
            () -> (type.getValue().equals("Fill") || type.getValue().equals("Both")) && FillSection.getValue());
    ModeSetting direction2FillBot = registerMode("Direction Fill Bot", Arrays.asList("X", "Z"), "X",
            () ->   (type.getValue().equals("Fill") || type.getValue().equals("Both")) &&
                    FillSection.getValue() && NVerticesFillBot.getValue().equals("2"));
    ColorSetting firstVerticeFillBot = registerColor("1 Vert Fill Bot", new GSColor(255, 16, 19, 255),
            () ->   (type.getValue().equals("Fill") || type.getValue().equals("Both")) &&
                    FillSection.getValue()
                    , true);
    ColorSetting secondVerticeFillBot = registerColor("2 Vert Fill Bot", new GSColor(0, 0, 255, 255),
            () ->   (type.getValue().equals("Fill") || type.getValue().equals("Both")) &&
                    FillSection.getValue()
                    && (NVerticesFillBot.getValue().equals("2") || NVerticesFillBot.getValue().equals("4")), true);
    ColorSetting thirdVerticeFillBot = registerColor("3 Vert Fill Bot", new GSColor(0, 255, 128, 255),
            () ->   (type.getValue().equals("Fill") || type.getValue().equals("Both")) &&
                    FillSection.getValue()
                    && NVerticesFillBot.getValue().equals("4"), true);
    ColorSetting fourVerticeFillBot = registerColor("4 Vert Fill Bot", new GSColor(255, 255, 2, 255),
            () ->   (type.getValue().equals("Fill") || type.getValue().equals("Both")) &&
                    FillSection.getValue()
                    && NVerticesFillBot.getValue().equals("4"), true);
    // Top
    ModeSetting NVerticesFillTop = registerMode("N^ Vertices Fill Top", Arrays.asList("1", "2", "4"), "4",
            () ->   (type.getValue().equals("Fill") || type.getValue().equals("Both")) &&
                    FillSection.getValue());
    ModeSetting direction2FillTop = registerMode("Direction Fill Top", Arrays.asList("X", "Z"), "X",
            () ->   (type.getValue().equals("Fill") || type.getValue().equals("Both")) &&
                    FillSection.getValue() && NVerticesFillTop.getValue().equals("2"));
    ColorSetting firstVerticeFillTop = registerColor("1 Vert Fill Top", new GSColor(255, 16, 19, 255),
            () ->   (type.getValue().equals("Fill") || type.getValue().equals("Both")) &&
                    FillSection.getValue(), true);
    ColorSetting secondVerticeFillTop = registerColor("2 Vert Fill Top", new GSColor(0, 0, 255, 255),
            () ->   (type.getValue().equals("Fill") || type.getValue().equals("Both")) &&
                    FillSection.getValue()
                    && (NVerticesFillTop.getValue().equals("2") || NVerticesFillTop.getValue().equals("4")), true);
    ColorSetting thirdVerticeFillTop = registerColor("3 Vert Fill Top", new GSColor(0, 255, 128, 255),
            () ->   (type.getValue().equals("Fill") || type.getValue().equals("Both")) &&
                    FillSection.getValue()
                    && NVerticesFillTop.getValue().equals("4"), true);
    ColorSetting fourVerticeFillTop = registerColor("4 Vert Fill Top", new GSColor(255, 255, 2, 255),
            () ->   (type.getValue().equals("Fill") || type.getValue().equals("Both")) &&
                    FillSection.getValue()
                    && NVerticesFillTop.getValue().equals("4"), true);
    //endregion

    BooleanSetting fillRaytrace = registerBoolean("Fill raytrace", false);
    ModeSetting mode = registerMode("Mode", Arrays.asList("Air", "Ground", "Flat", "Slab", "Double"), "Air");
    BooleanSetting hideOwn = registerBoolean("Hide Own", false);
    BooleanSetting flatOwn = registerBoolean("Flat Own", false);
    DoubleSetting slabHeight = registerDouble("Slab Height", 0.5, 0.1, 1.5);
    IntegerSetting width = registerInteger("Width", 1, 1, 10);
    IntegerSetting ufoAlpha = registerInteger("UFOAlpha", 255, 0, 255);

    private ConcurrentHashMap<AxisAlignedBB, Integer> holes;

    public void onUpdate() {
        if (mc.player == null || mc.world == null) {
            return;
        }

        if (holes == null) {
            holes = new ConcurrentHashMap<>();
        } else {
            holes.clear();
        }

        int range = (int) Math.ceil(this.range.getValue());

        HashSet<BlockPos> possibleHoles = Sets.newHashSet();
        List<BlockPos> blockPosList = EntityUtil.getSphere(PlayerUtil.getPlayerPos(), range, range, false, true, 0);

        for (BlockPos pos : blockPosList) {

            if (!mc.world.getBlockState(pos).getBlock().equals(Blocks.AIR)) {
                continue;
            }

            if (mc.world.getBlockState(pos.add(0, -1, 0)).getBlock().equals(Blocks.AIR)) {
                continue;
            }
            if (!mc.world.getBlockState(pos.add(0, 1, 0)).getBlock().equals(Blocks.AIR)) {
                continue;
            }

            if (mc.world.getBlockState(pos.add(0, 2, 0)).getBlock().equals(Blocks.AIR)) {
                possibleHoles.add(pos);
            }
        }

        possibleHoles.forEach(pos -> {
            HoleUtil.HoleInfo holeInfo = HoleUtil.isHole(pos, false, false);
            HoleUtil.HoleType holeType = holeInfo.getType();
            if (holeType != HoleUtil.HoleType.NONE) {
                HoleUtil.BlockSafety holeSafety = holeInfo.getSafety();
                AxisAlignedBB centreBlocks = holeInfo.getCentre();

                if (centreBlocks == null)
                    return;

                int typeHole;
                if (holeSafety == HoleUtil.BlockSafety.UNBREAKABLE) {
                    typeHole = 0;
                } else {
                   typeHole = 1;
                }
                if (holeType == HoleUtil.HoleType.CUSTOM) {
                    typeHole = 2;
                }

                String mode = customHoles.getValue();
                if (mode.equalsIgnoreCase("Custom") && (holeType == HoleUtil.HoleType.CUSTOM || holeType == HoleUtil.HoleType.DOUBLE)) {
                    holes.put(centreBlocks, typeHole);
                } else if (mode.equalsIgnoreCase("Double") && holeType == HoleUtil.HoleType.DOUBLE) {
                    holes.put(centreBlocks, typeHole);
                } else if (holeType == HoleUtil.HoleType.SINGLE) {
                    holes.put(centreBlocks, typeHole);
                }
            }
        });
    }

    public void onWorldRender(RenderEvent event) {
        if (mc.player == null || mc.world == null || holes == null || holes.isEmpty()) {
            return;
        }

        holes.forEach(this::renderHoles);
    }

    boolean isOne(Integer typeHole, boolean outline) {
        return outline ?
            (
                typeHole == 0 ?
                (bNVerticesOutlineBot.getValue().equals("1") && bNVerticesOutlineTop.getValue().equals("1"))
            : typeHole == 1 ?
                (oNVerticesOutlineBot.getValue().equals("1") && oNVerticesOutlineTop.getValue().equals("1"))
            :   (NVerticesOutlineBot.getValue().equals("1") && NVerticesOutlineTop.getValue().equals("1"))
            ) : (
                typeHole == 0 ?
                (bNVerticesFillBot.getValue().equals("1") && bNVerticesFillTop.getValue().equals("1"))
                : typeHole == 1 ?
                (oNVerticesFillBot.getValue().equals("1") && oNVerticesFillTop.getValue().equals("1"))
                :   (NVerticesFillBot.getValue().equals("1") && NVerticesFillTop.getValue().equals("1"))
            );
    }

    private void renderHoles(AxisAlignedBB hole, Integer typeHole) {
        switch (type.getValue()) {
            case "Outline": {
                if (isOne(typeHole, true))
                    renderOutline(hole, typeHole == 0 ? bfirstVerticeOutlineBot.getColor()
                                        : typeHole == 1 ? ofirstVerticeOutlineBot.getColor()
                                        : firstVerticeOutlineBot.getColor());
                else renderCustomOutline(hole, typeHole);
                break;
            }
            case "Fill": {
                if (isOne(typeHole, false))
                    renderFill(hole, typeHole == 0 ? bfirstVerticeFillBot.getColor()
                                    : typeHole == 1 ? ofirstVerticeFillBot.getColor()
                                    : firstVerticeFillBot.getColor());
                else renderFillCustom(hole, typeHole);
                break;
            }
            case "Both": {
                if ( !fillRaytrace.getValue() || mc.world.rayTraceBlocks(hole.getCenter(), new Vec3d(mc.player.posX, mc.player.posY + (double) mc.player.getEyeHeight() + 1, mc.player.posZ)) == null)
                    if (isOne(typeHole, false))
                        renderFill(hole, typeHole == 0 ? bfirstVerticeFillBot.getColor()
                                        : typeHole == 1 ? ofirstVerticeFillBot.getColor()
                                        : firstVerticeFillBot.getColor());
                    else renderFillCustom(hole, typeHole);
                if (isOne(typeHole, true))
                    renderOutline(hole, typeHole == 0 ? bfirstVerticeOutlineBot.getColor()
                                        : typeHole == 1 ? ofirstVerticeOutlineBot.getColor()
                                        : firstVerticeOutlineBot.getColor());
                else renderCustomOutline(hole, typeHole);
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

    private void renderCustomOutline(AxisAlignedBB hole, int typeHole) {

        if (hideOwn.getValue() && hole.intersects(mc.player.getEntityBoundingBox())) return;

        ArrayList<GSColor> colors = new ArrayList<>();

        switch (typeHole) {
            case 0:
                switch (bNVerticesOutlineBot.getValue()) {
                    case "1":
                        colors.add(bfirstVerticeOutlineBot.getValue());
                        colors.add(bfirstVerticeOutlineBot.getValue());
                        colors.add(bfirstVerticeOutlineBot.getValue());
                        colors.add(bfirstVerticeOutlineBot.getValue());
                        break;
                    case "2":
                        if (bdirection2OutLineBot.getValue().equals("X")) {
                            colors.add(bfirstVerticeOutlineBot.getValue());
                            colors.add(bsecondVerticeOutlineBot.getValue());
                            colors.add(bfirstVerticeOutlineBot.getValue());
                            colors.add(bsecondVerticeOutlineBot.getValue());
                        } else {
                            colors.add(bfirstVerticeOutlineBot.getValue());
                            colors.add(bfirstVerticeOutlineBot.getValue());
                            colors.add(bsecondVerticeOutlineBot.getValue());
                            colors.add(bsecondVerticeOutlineBot.getValue());
                        }
                        break;
                    case "4":
                        colors.add(bfirstVerticeOutlineBot.getValue());
                        colors.add(bsecondVerticeOutlineBot.getValue());
                        colors.add(bthirdVerticeOutlineBot.getValue());
                        colors.add(bfourVerticeOutlineBot.getValue());
                        break;
                }
                switch (bNVerticesOutlineTop.getValue()) {
                    case "1":
                        colors.add(bfirstVerticeOutlineTop.getValue());
                        colors.add(bfirstVerticeOutlineTop.getValue());
                        colors.add(bfirstVerticeOutlineTop.getValue());
                        colors.add(bfirstVerticeOutlineTop.getValue());
                        break;
                    case "2":
                        if (bdirection2OutLineTop.getValue().equals("X")) {
                            colors.add(bfirstVerticeOutlineTop.getValue());
                            colors.add(bsecondVerticeOutlineTop.getValue());
                            colors.add(bfirstVerticeOutlineTop.getValue());
                            colors.add(bsecondVerticeOutlineTop.getValue());
                        } else {
                            colors.add(bfirstVerticeOutlineTop.getValue());
                            colors.add(bfirstVerticeOutlineTop.getValue());
                            colors.add(bsecondVerticeOutlineTop.getValue());
                            colors.add(bsecondVerticeOutlineTop.getValue());
                        }
                        break;
                    case "4":
                        colors.add(bfirstVerticeOutlineTop.getValue());
                        colors.add(bsecondVerticeOutlineTop.getValue());
                        colors.add(bthirdVerticeOutlineTop.getValue());
                        colors.add(bfourVerticeOutlineTop.getValue());
                        break;
                }
                break;
            case 1:
                switch (oNVerticesOutlineBot.getValue()) {
                    case "1":
                        colors.add(ofirstVerticeOutlineBot.getValue());
                        colors.add(ofirstVerticeOutlineBot.getValue());
                        colors.add(ofirstVerticeOutlineBot.getValue());
                        colors.add(ofirstVerticeOutlineBot.getValue());
                        break;
                    case "2":
                        if (odirection2OutLineBot.getValue().equals("X")) {
                            colors.add(ofirstVerticeOutlineBot.getValue());
                            colors.add(osecondVerticeOutlineBot.getValue());
                            colors.add(ofirstVerticeOutlineBot.getValue());
                            colors.add(osecondVerticeOutlineBot.getValue());
                        } else {
                            colors.add(ofirstVerticeOutlineBot.getValue());
                            colors.add(ofirstVerticeOutlineBot.getValue());
                            colors.add(osecondVerticeOutlineBot.getValue());
                            colors.add(osecondVerticeOutlineBot.getValue());
                        }
                        break;
                    case "4":
                        colors.add(ofirstVerticeOutlineBot.getValue());
                        colors.add(osecondVerticeOutlineBot.getValue());
                        colors.add(othirdVerticeOutlineBot.getValue());
                        colors.add(ofourVerticeOutlineBot.getValue());
                        break;
                }
                switch (oNVerticesOutlineTop.getValue()) {
                    case "1":
                        colors.add(ofirstVerticeOutlineTop.getValue());
                        colors.add(ofirstVerticeOutlineTop.getValue());
                        colors.add(ofirstVerticeOutlineTop.getValue());
                        colors.add(ofirstVerticeOutlineTop.getValue());
                        break;
                    case "2":
                        if (odirection2OutLineTop.getValue().equals("X")) {
                            colors.add(ofirstVerticeOutlineTop.getValue());
                            colors.add(osecondVerticeOutlineTop.getValue());
                            colors.add(ofirstVerticeOutlineTop.getValue());
                            colors.add(osecondVerticeOutlineTop.getValue());
                        } else {
                            colors.add(ofirstVerticeOutlineTop.getValue());
                            colors.add(ofirstVerticeOutlineTop.getValue());
                            colors.add(osecondVerticeOutlineTop.getValue());
                            colors.add(osecondVerticeOutlineTop.getValue());
                        }
                        break;
                    case "4":
                        colors.add(ofirstVerticeOutlineTop.getValue());
                        colors.add(osecondVerticeOutlineTop.getValue());
                        colors.add(othirdVerticeOutlineTop.getValue());
                        colors.add(ofourVerticeOutlineTop.getValue());
                        break;
                }
                break;
            case 2:
                switch (NVerticesOutlineBot.getValue()) {
                    case "1":
                        colors.add(firstVerticeOutlineBot.getValue());
                        colors.add(firstVerticeOutlineBot.getValue());
                        colors.add(firstVerticeOutlineBot.getValue());
                        colors.add(firstVerticeOutlineBot.getValue());
                        break;
                    case "2":
                        if (odirection2OutLineBot.getValue().equals("X")) {
                            colors.add(firstVerticeOutlineBot.getValue());
                            colors.add(secondVerticeOutlineBot.getValue());
                            colors.add(firstVerticeOutlineBot.getValue());
                            colors.add(secondVerticeOutlineBot.getValue());
                        } else {
                            colors.add(firstVerticeOutlineBot.getValue());
                            colors.add(firstVerticeOutlineBot.getValue());
                            colors.add(secondVerticeOutlineBot.getValue());
                            colors.add(secondVerticeOutlineBot.getValue());
                        }
                        break;
                    case "4":
                        colors.add(firstVerticeOutlineBot.getValue());
                        colors.add(secondVerticeOutlineBot.getValue());
                        colors.add(thirdVerticeOutlineBot.getValue());
                        colors.add(fourVerticeOutlineBot.getValue());
                        break;
                }
                switch (NVerticesOutlineTop.getValue()) {
                    case "1":
                        colors.add(firstVerticeOutlineTop.getValue());
                        colors.add(firstVerticeOutlineTop.getValue());
                        colors.add(firstVerticeOutlineTop.getValue());
                        colors.add(firstVerticeOutlineTop.getValue());
                        break;
                    case "2":
                        if (direction2OutLineTop.getValue().equals("X")) {
                            colors.add(firstVerticeOutlineTop.getValue());
                            colors.add(secondVerticeOutlineTop.getValue());
                            colors.add(firstVerticeOutlineTop.getValue());
                            colors.add(secondVerticeOutlineTop.getValue());
                        } else {
                            colors.add(firstVerticeOutlineTop.getValue());
                            colors.add(firstVerticeOutlineTop.getValue());
                            colors.add(secondVerticeOutlineTop.getValue());
                            colors.add(secondVerticeOutlineTop.getValue());
                        }
                        break;
                    case "4":
                        colors.add(firstVerticeOutlineTop.getValue());
                        colors.add(secondVerticeOutlineTop.getValue());
                        colors.add(thirdVerticeOutlineTop.getValue());
                        colors.add(fourVerticeOutlineTop.getValue());
                        break;
                }
                break;
        }

        switch (mode.getValue()) {
            case "Air":
                if (flatOwn.getValue() && hole.intersects(mc.player.getEntityBoundingBox())) {
                    hole = hole.setMaxY(hole.maxY - 1);
                }
                break;
            case "Ground":
                hole = hole.offset(0, -1, 0);
                break;
            case "Double":
                if (flatOwn.getValue() && hole.intersects(mc.player.getEntityBoundingBox())) {
                    hole = hole.setMaxY(hole.maxY - 1);
                } else hole = hole.setMaxY(hole.maxY + 1);
                break;
            case "Slab":
                if (flatOwn.getValue() && hole.intersects(mc.player.getEntityBoundingBox())) {
                    hole = hole.setMaxY(hole.maxY - 1);
                } else hole = hole.setMaxY(hole.minY + slabHeight.getValue());
                break;
            case "Flat":
                hole = hole.setMaxY(hole.maxY - 1);
                break;
        }

        RenderUtil.drawBoundingBox(hole, width.getValue(), ufoAlpha.getValue(), colors.toArray(new GSColor[7]));
    }

    void renderFillCustom(AxisAlignedBB hole, int typeHole) {

        int mask = GeometryMasks.Quad.ALL;

        if (hideOwn.getValue() && hole.intersects(mc.player.getEntityBoundingBox())) return;

        ArrayList<GSColor> colors = new ArrayList<>();
        switch (typeHole) {
            case 0:
                switch (bNVerticesFillBot.getValue()) {
                    case "1":
                        colors.add(bfirstVerticeFillBot.getValue());
                        colors.add(bfirstVerticeFillBot.getValue());
                        colors.add(bfirstVerticeFillBot.getValue());
                        colors.add(bfirstVerticeFillBot.getValue());
                        break;
                    case "2":
                        if (bdirection2FillBot.getValue().equals("X")) {
                            colors.add(bfirstVerticeFillBot.getValue());
                            colors.add(bsecondVerticeFillBot.getValue());
                            colors.add(bfirstVerticeFillBot.getValue());
                            colors.add(bsecondVerticeFillBot.getValue());
                        } else {
                            colors.add(bfirstVerticeFillBot.getValue());
                            colors.add(bfirstVerticeFillBot.getValue());
                            colors.add(bsecondVerticeFillBot.getValue());
                            colors.add(bsecondVerticeFillBot.getValue());
                        }
                        break;
                    case "4":
                        colors.add(bfirstVerticeFillBot.getValue());
                        colors.add(bsecondVerticeFillBot.getValue());
                        colors.add(bthirdVerticeFillBot.getValue());
                        colors.add(bfourVerticeFillBot.getValue());
                        break;
                }
                switch (bNVerticesFillTop.getValue()) {
                    case "1":
                        colors.add(bfirstVerticeFillTop.getValue());
                        colors.add(bfirstVerticeFillTop.getValue());
                        colors.add(bfirstVerticeFillTop.getValue());
                        colors.add(bfirstVerticeFillTop.getValue());
                        break;
                    case "2":
                        if (bdirection2FillTop.getValue().equals("X")) {
                            colors.add(bfirstVerticeFillTop.getValue());
                            colors.add(bsecondVerticeFillTop.getValue());
                            colors.add(bfirstVerticeFillTop.getValue());
                            colors.add(bsecondVerticeFillTop.getValue());
                        } else {
                            colors.add(bfirstVerticeFillTop.getValue());
                            colors.add(bfirstVerticeFillTop.getValue());
                            colors.add(bsecondVerticeFillTop.getValue());
                            colors.add(bsecondVerticeFillTop.getValue());
                        }
                        break;
                    case "4":
                        colors.add(bfirstVerticeFillTop.getValue());
                        colors.add(bsecondVerticeFillTop.getValue());
                        colors.add(bthirdVerticeFillTop.getValue());
                        colors.add(bfourVerticeFillTop.getValue());
                        break;
                }
                break;
            case 1:
                switch (oNVerticesFillBot.getValue()) {
                    case "1":
                        colors.add(ofirstVerticeFillBot.getValue());
                        colors.add(ofirstVerticeFillBot.getValue());
                        colors.add(ofirstVerticeFillBot.getValue());
                        colors.add(ofirstVerticeFillBot.getValue());
                        break;
                    case "2":
                        if (odirection2FillBot.getValue().equals("X")) {
                            colors.add(ofirstVerticeFillBot.getValue());
                            colors.add(osecondVerticeFillBot.getValue());
                            colors.add(ofirstVerticeFillBot.getValue());
                            colors.add(osecondVerticeFillBot.getValue());
                        } else {
                            colors.add(ofirstVerticeFillBot.getValue());
                            colors.add(ofirstVerticeFillBot.getValue());
                            colors.add(osecondVerticeFillBot.getValue());
                            colors.add(osecondVerticeFillBot.getValue());
                        }
                        break;
                    case "4":
                        colors.add(ofirstVerticeFillBot.getValue());
                        colors.add(osecondVerticeFillBot.getValue());
                        colors.add(othirdVerticeFillBot.getValue());
                        colors.add(ofourVerticeFillBot.getValue());
                        break;
                }
                switch (oNVerticesFillTop.getValue()) {
                    case "1":
                        colors.add(ofirstVerticeFillTop.getValue());
                        colors.add(ofirstVerticeFillTop.getValue());
                        colors.add(ofirstVerticeFillTop.getValue());
                        colors.add(ofirstVerticeFillTop.getValue());
                        break;
                    case "2":
                        if (odirection2FillTop.getValue().equals("X")) {
                            colors.add(ofirstVerticeFillTop.getValue());
                            colors.add(osecondVerticeFillTop.getValue());
                            colors.add(ofirstVerticeFillTop.getValue());
                            colors.add(osecondVerticeFillTop.getValue());
                        } else {
                            colors.add(ofirstVerticeFillTop.getValue());
                            colors.add(ofirstVerticeFillTop.getValue());
                            colors.add(osecondVerticeFillTop.getValue());
                            colors.add(osecondVerticeFillTop.getValue());
                        }
                        break;
                    case "4":
                        colors.add(ofirstVerticeFillTop.getValue());
                        colors.add(osecondVerticeFillTop.getValue());
                        colors.add(othirdVerticeFillTop.getValue());
                        colors.add(ofourVerticeFillTop.getValue());
                        break;
                }
                break;
            case 2:
                switch (NVerticesFillBot.getValue()) {
                    case "1":
                        colors.add(firstVerticeFillBot.getValue());
                        colors.add(firstVerticeFillBot.getValue());
                        colors.add(firstVerticeFillBot.getValue());
                        colors.add(firstVerticeFillBot.getValue());
                        break;
                    case "2":
                        if (odirection2FillBot.getValue().equals("X")) {
                            colors.add(firstVerticeFillBot.getValue());
                            colors.add(secondVerticeFillBot.getValue());
                            colors.add(firstVerticeFillBot.getValue());
                            colors.add(secondVerticeFillBot.getValue());
                        } else {
                            colors.add(firstVerticeFillBot.getValue());
                            colors.add(firstVerticeFillBot.getValue());
                            colors.add(secondVerticeFillBot.getValue());
                            colors.add(secondVerticeFillBot.getValue());
                        }
                        break;
                    case "4":
                        colors.add(firstVerticeFillBot.getValue());
                        colors.add(secondVerticeFillBot.getValue());
                        colors.add(thirdVerticeFillBot.getValue());
                        colors.add(fourVerticeFillBot.getValue());
                        break;
                }
                switch (NVerticesFillTop.getValue()) {
                    case "1":
                        colors.add(firstVerticeFillTop.getValue());
                        colors.add(firstVerticeFillTop.getValue());
                        colors.add(firstVerticeFillTop.getValue());
                        colors.add(firstVerticeFillTop.getValue());
                        break;
                    case "2":
                        if (direction2FillTop.getValue().equals("X")) {
                            colors.add(firstVerticeFillTop.getValue());
                            colors.add(secondVerticeFillTop.getValue());
                            colors.add(firstVerticeFillTop.getValue());
                            colors.add(secondVerticeFillTop.getValue());
                        } else {
                            colors.add(firstVerticeFillTop.getValue());
                            colors.add(firstVerticeFillTop.getValue());
                            colors.add(secondVerticeFillTop.getValue());
                            colors.add(secondVerticeFillTop.getValue());
                        }
                        break;
                    case "4":
                        colors.add(firstVerticeFillTop.getValue());
                        colors.add(secondVerticeFillTop.getValue());
                        colors.add(thirdVerticeFillTop.getValue());
                        colors.add(fourVerticeFillTop.getValue());
                        break;
                }
                break;
        }

        switch (mode.getValue()) {
            case "Air":
                if (flatOwn.getValue() && hole.intersects(mc.player.getEntityBoundingBox())) {
                    mask = GeometryMasks.Quad.DOWN;
                }
                break;
            case "Ground":
                hole = hole.offset(0, -1, 0);
                break;
            case "Double":
                if (flatOwn.getValue() && hole.intersects(mc.player.getEntityBoundingBox())) {
                    mask = GeometryMasks.Quad.DOWN;
                } else hole = hole.setMaxY(hole.maxY + 1);
                break;
            case "Slab":
                if (flatOwn.getValue() && hole.intersects(mc.player.getEntityBoundingBox())) {
                    mask = GeometryMasks.Quad.DOWN;
                } else hole = hole.setMaxY(hole.minY + slabHeight.getValue());
                break;
            case "Flat":
                mask = GeometryMasks.Quad.DOWN;
                break;
        }

        RenderUtil.drawBoxProva2(hole, true, 1, colors.toArray(new GSColor[7]), (this.ufoAlpha.getValue() * 50) / 255, mask);
    }
}