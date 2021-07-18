package com.gamesense.client.module.modules.render;

import com.gamesense.api.event.events.RenderEvent;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.ColorSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.render.GSColor;
import com.gamesense.api.util.render.RenderUtil;
import com.gamesense.api.util.world.GeometryMasks;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import net.minecraft.block.material.Material;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Hoosiers
 * @since 10/10/2020
 */

@Module.Declaration(name = "BlockHighlight", category = Category.Render)
public class BlockHighlight extends Module {

    ModeSetting renderLook = registerMode("Render", Arrays.asList("Block", "Side"), "Block");
    ModeSetting type = registerMode("Render", Arrays.asList("Outline", "Fill", "Both"), "Both");
    IntegerSetting lineWidth = registerInteger("Width", 1, 1, 5);
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

    public void onWorldRender(RenderEvent event) {
        RayTraceResult rayTraceResult = mc.objectMouseOver;

        if (rayTraceResult == null) return;

        EnumFacing enumFacing = mc.objectMouseOver.sideHit;

        if (enumFacing == null) return;

        if (rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK) {

            BlockPos blockPos = rayTraceResult.getBlockPos();
            AxisAlignedBB axisAlignedBB = mc.world.getBlockState(blockPos).getSelectedBoundingBox(mc.world, blockPos);
            int lookInt = renderLook.getValue().equalsIgnoreCase("Side") ? findRenderingSide(enumFacing) : GeometryMasks.Quad.ALL;

            if (mc.world.getBlockState(blockPos).getMaterial() != Material.AIR) {
                switch (type.getValue()) {
                    case "Outline": {
                        if (NVerticesOutlineBot.getValue().equals("1") && NVerticesOutlineTop.getValue().equals("1"))
                            renderOutline(axisAlignedBB, lineWidth.getValue(),
                                    new GSColor(firstVerticeOutlineBot.getValue(), firstVerticeOutlineBot.getColor().getAlpha()), lookInt);
                        else {
                            renderCustomOutline(axisAlignedBB, lookInt);
                        }
                        break;
                    }
                    case "Fill": {
                        if (NVerticesFillBot.getValue().equals("1") && NVerticesFillTop.getValue().equals("1")) {
                            RenderUtil.drawBox(axisAlignedBB, true, 1,
                                    new GSColor(firstVerticeFillBot.getValue(), firstVerticeFillBot.getColor().getAlpha()), lookInt);
                        }
                        else {
                            renderCustomFill(axisAlignedBB, lookInt);
                        }
                        break;
                    }
                    default: {
                        if (NVerticesOutlineBot.getValue().equals("1") && NVerticesOutlineTop.getValue().equals("1"))
                            renderOutline(axisAlignedBB, lineWidth.getValue(),
                                    new GSColor(firstVerticeOutlineBot.getValue(), firstVerticeOutlineBot.getColor().getAlpha()), lookInt);
                        else {
                            renderCustomOutline(axisAlignedBB, lookInt);
                        }
                        if (NVerticesFillBot.getValue().equals("1") && NVerticesFillTop.getValue().equals("1")) {
                            RenderUtil.drawBox(axisAlignedBB, true, 1,
                                    new GSColor(firstVerticeFillBot.getValue(), firstVerticeFillBot.getColor().getAlpha()), lookInt);
                        }
                        else {
                            renderCustomFill(axisAlignedBB, lookInt);
                        }
                        break;
                    }
                }
            }
        }
    }

    void renderCustomOutline(AxisAlignedBB box, int face) {
        ArrayList<GSColor> colors = new ArrayList<>();

        switch (NVerticesOutlineBot.getValue()) {
            case "1":
                colors.add(firstVerticeOutlineBot.getValue());
                colors.add(firstVerticeOutlineBot.getValue());
                colors.add(firstVerticeOutlineBot.getValue());
                colors.add(firstVerticeOutlineBot.getValue());
                break;
            case "2":
                if (direction2OutLineBot.getValue().equals("X")) {
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

        if (face == GeometryMasks.Quad.ALL)
            RenderUtil.drawBoundingBox(box, lineWidth.getValue(), colors.toArray(new GSColor[7]));
        else RenderUtil.drawBoundingBox(box, lineWidth.getValue(), colors.toArray(new GSColor[7]), true, face);
    }

    void renderCustomFill(AxisAlignedBB box, int face) {
        ArrayList<GSColor> colors = new ArrayList<>();

        switch (NVerticesFillBot.getValue()) {
            case "1":
                colors.add(firstVerticeFillBot.getValue());
                colors.add(firstVerticeFillBot.getValue());
                colors.add(firstVerticeFillBot.getValue());
                colors.add(firstVerticeFillBot.getValue());
                break;
            case "2":
                if (direction2FillBot.getValue().equals("X")) {
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

        RenderUtil.drawBoxProva2(box, true, 1, colors.toArray(new GSColor[7]), face, true);
    }

    private void renderOutline(AxisAlignedBB axisAlignedBB, int lineWidth, GSColor color, int lookInt) {
        if (lookInt == GeometryMasks.Quad.ALL) {
            RenderUtil.drawBoundingBox(axisAlignedBB, lineWidth, color);
        } else {
            RenderUtil.drawBoundingBoxWithSides(axisAlignedBB, lineWidth, color, lookInt);
        }
    }

    private int findRenderingSide(EnumFacing enumFacing) {

        switch (enumFacing) {
            case EAST: {
                return GeometryMasks.Quad.EAST;
            }
            case WEST: {
                return GeometryMasks.Quad.WEST;
            }
            case NORTH: {
                return GeometryMasks.Quad.NORTH;
            }
            case SOUTH: {
                return GeometryMasks.Quad.SOUTH;
            }
            case UP: {
                return GeometryMasks.Quad.UP;
            }
            default: {
                return GeometryMasks.Quad.DOWN;
            }
        }
    }
}