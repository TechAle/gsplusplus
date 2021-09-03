package com.gamesense.api.util.render;

import com.gamesense.api.util.font.FontUtil;
import com.gamesense.api.util.world.EntityUtil;
import com.gamesense.api.util.world.GeometryMasks;
import com.gamesense.client.module.ModuleManager;
import com.gamesense.client.module.modules.gui.ColorMain;
import com.gamesense.client.module.modules.render.Nametags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL32;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.glu.Sphere;

import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glHint;

/**
 * @author 086
 * @author Hoosiers
 * @author lukflug
 * @author TechAle
 */

public class RenderUtil {

    private static final Minecraft mc = Minecraft.getMinecraft();

    public static void drawLine(double posx, double posy, double posz, double posx2, double posy2, double posz2, GSColor color) {
        drawLine(posx, posy, posz, posx2, posy2, posz2, color, 1);
    }

    public static void drawLine(double posx, double posy, double posz, double posx2, double posy2, double posz2, GSColor color, float width) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.glLineWidth(width);
        color.glColor();
        bufferbuilder.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
        vertex(posx, posy, posz, bufferbuilder);
        vertex(posx2, posy2, posz2, bufferbuilder);
        tessellator.draw();
    }

    public static void draw2DRect(int posX, int posY, int width, int height, int zHeight, GSColor color) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        color.glColor();
        bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        bufferbuilder.pos(posX, posY + height, zHeight).endVertex();
        bufferbuilder.pos(posX + width, posY + height, zHeight).endVertex();
        bufferbuilder.pos(posX + width, posY, zHeight).endVertex();
        bufferbuilder.pos(posX, posY, zHeight).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    private static void drawBorderedRect(double x, double y, double x1, double y1, float lineWidth, GSColor inside, GSColor border) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        inside.glColor();
        bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        bufferbuilder.pos(x, y1, 0).endVertex();
        bufferbuilder.pos(x1, y1, 0).endVertex();
        bufferbuilder.pos(x1, y, 0).endVertex();
        bufferbuilder.pos(x, y, 0).endVertex();
        tessellator.draw();
        border.glColor();
        GlStateManager.glLineWidth(lineWidth);
        bufferbuilder.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);
        bufferbuilder.pos(x, y, 0).endVertex();
        bufferbuilder.pos(x, y1, 0).endVertex();
        bufferbuilder.pos(x1, y1, 0).endVertex();
        bufferbuilder.pos(x1, y, 0).endVertex();
        bufferbuilder.pos(x, y, 0).endVertex();
        tessellator.draw();
    }

    public static void drawBox(BlockPos blockPos, double height, GSColor color, int sides) {
        drawBox(blockPos.getX(), blockPos.getY(), blockPos.getZ(), 1, height, 1, color, color.getAlpha(), sides);
    }

    public static void drawBox(AxisAlignedBB bb, boolean check, double height, GSColor color, int sides) {
        drawBox(bb, check, height, color, color.getAlpha(), sides);
    }

    public static void drawBox(AxisAlignedBB bb, boolean check, double height, GSColor color, int alpha, int sides) {
        if (check) {
            drawBox(bb.minX, bb.minY, bb.minZ, bb.maxX - bb.minX, bb.maxY - bb.minY, bb.maxZ - bb.minZ, color, alpha, sides);
        } else {
            drawBox(bb.minX, bb.minY, bb.minZ, bb.maxX - bb.minX, height, bb.maxZ - bb.minZ, color, alpha, sides);
        }
    }

    public static void drawBoxProva2(AxisAlignedBB bb, boolean check, double height, GSColor[] color, int sides, boolean five) {
        drawBoxProva(bb.minX, bb.minY, bb.minZ, bb.maxX - bb.minX, bb.maxY - bb.minY, bb.maxZ - bb.minZ, color, sides, five);
    }

    public static void drawBox(double x, double y, double z, double w, double h, double d, GSColor color, int alpha, int sides) {
        GlStateManager.disableAlpha();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        color.glColor();
        bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        doVerticies(new AxisAlignedBB(x, y, z, x + w, y + h, z + d), color, alpha, bufferbuilder, sides, false);
        tessellator.draw();
        GlStateManager.enableAlpha();
    }

    public static void drawBoxProva(double x, double y, double z, double w, double h, double d, GSColor[] color, int sides, boolean five) {
        GlStateManager.disableAlpha();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        doVerticiesProva(new AxisAlignedBB(x, y, z, x + w, y + h, z + d), color, bufferbuilder, sides, false);
        tessellator.draw();
        GlStateManager.enableAlpha();
    }

    private static void doVerticiesProva(AxisAlignedBB axisAlignedBB, GSColor[] color, BufferBuilder bufferbuilder, int sides, boolean five) {
        if ((sides & GeometryMasks.Quad.EAST) != 0) {
            colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ, color[2], color[2].getAlpha(), bufferbuilder);
            colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ, color[3], color[3].getAlpha(), bufferbuilder);
            colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ, color[7], color[7].getAlpha(), bufferbuilder);
            colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ, color[6], color[6].getAlpha(), bufferbuilder);
            if (five)
                colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ, color[2], color[2].getAlpha(), bufferbuilder);
        }

        if ((sides & GeometryMasks.Quad.WEST) != 0) {
            colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ, color[0], color[0].getAlpha(), bufferbuilder);
            colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ, color[1], color[1].getAlpha(), bufferbuilder);
            colorVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ, color[5], color[5].getAlpha(), bufferbuilder);
            colorVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ, color[4], color[4].getAlpha(), bufferbuilder);
            if (five)
                colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ, color[0], color[0].getAlpha(), bufferbuilder);
        }
        if ((sides & GeometryMasks.Quad.NORTH) != 0) {
            colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ, color[3], color[3].getAlpha(), bufferbuilder);
            colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ, color[0], color[0].getAlpha(), bufferbuilder);
            colorVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ, color[4], color[4].getAlpha(), bufferbuilder);
            colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ, color[7], color[7].getAlpha(), bufferbuilder);
            if (five)
                colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ, color[3], color[3].getAlpha(), bufferbuilder);
        }
        if ((sides & GeometryMasks.Quad.SOUTH) != 0) {
            colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ, color[1], color[1].getAlpha(), bufferbuilder);
            colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ, color[2], color[2].getAlpha(), bufferbuilder);
            colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ, color[6], color[6].getAlpha(), bufferbuilder);
            colorVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ, color[5], color[5].getAlpha(), bufferbuilder);
            if (five)
                colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ, color[1], color[1].getAlpha(), bufferbuilder);
        }
        if ((sides & GeometryMasks.Quad.UP) != 0) {
            colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ, color[7], color[7].getAlpha(), bufferbuilder);
            colorVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ, color[6], color[6].getAlpha(), bufferbuilder);
            colorVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ, color[5], color[5].getAlpha(), bufferbuilder);
            colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ, color[4], color[4].getAlpha(), bufferbuilder);
            if (five)
                colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ, color[7], color[7].getAlpha(), bufferbuilder);
        }
        if ((sides & GeometryMasks.Quad.DOWN) != 0) {
            colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ, color[3], color[3].getAlpha(), bufferbuilder);
            colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ, color[2], color[2].getAlpha(), bufferbuilder);
            colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ, color[1], color[1].getAlpha(), bufferbuilder);
            colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ, color[0], color[0].getAlpha(), bufferbuilder);
            if (five)
                colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ, color[3], color[3].getAlpha(), bufferbuilder);
        }
    }




    public static void drawBoundingBox(BlockPos bp, double height, float width, GSColor color) {
        drawBoundingBox(getBoundingBox(bp, 1, height, 1), width, color, color.getAlpha());
    }

    public static void drawBoundingBox(AxisAlignedBB bb, double width, GSColor color) {
        drawBoundingBox(bb, width, color, color.getAlpha());
    }

    public static void drawBoundingBox(AxisAlignedBB bb, double width, GSColor color, int alpha) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.glLineWidth((float) width);
        color.glColor();
        bufferbuilder.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        colorVertex(bb.minX, bb.minY, bb.minZ, color, color.getAlpha(), bufferbuilder);
        colorVertex(bb.minX, bb.minY, bb.maxZ, color, color.getAlpha(), bufferbuilder);
        colorVertex(bb.maxX, bb.minY, bb.maxZ, color, color.getAlpha(), bufferbuilder);
        colorVertex(bb.maxX, bb.minY, bb.minZ, color, color.getAlpha(), bufferbuilder);
        colorVertex(bb.minX, bb.minY, bb.minZ, color, color.getAlpha(), bufferbuilder);
        colorVertex(bb.minX, bb.maxY, bb.minZ, color, alpha, bufferbuilder);
        colorVertex(bb.minX, bb.maxY, bb.maxZ, color, alpha, bufferbuilder);
        colorVertex(bb.minX, bb.minY, bb.maxZ, color, color.getAlpha(), bufferbuilder);
        colorVertex(bb.maxX, bb.minY, bb.maxZ, color, color.getAlpha(), bufferbuilder);
        colorVertex(bb.maxX, bb.maxY, bb.maxZ, color, alpha, bufferbuilder);
        colorVertex(bb.minX, bb.maxY, bb.maxZ, color, alpha, bufferbuilder);
        colorVertex(bb.maxX, bb.maxY, bb.maxZ, color, alpha, bufferbuilder);
        colorVertex(bb.maxX, bb.maxY, bb.minZ, color, alpha, bufferbuilder);
        colorVertex(bb.maxX, bb.minY, bb.minZ, color, color.getAlpha(), bufferbuilder);
        colorVertex(bb.maxX, bb.maxY, bb.minZ, color, alpha, bufferbuilder);
        colorVertex(bb.minX, bb.maxY, bb.minZ, color, alpha, bufferbuilder);
        tessellator.draw();
    }

    public static void drawBoundingBox(AxisAlignedBB bb, double width,
                                       GSColor[] otherPos) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.glLineWidth((float) width);
        bufferbuilder.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        colorVertex(bb.minX, bb.minY, bb.minZ, otherPos[0], otherPos[0].getAlpha(), bufferbuilder);
        colorVertex(bb.minX, bb.minY, bb.maxZ, otherPos[1], otherPos[1].getAlpha(), bufferbuilder);
        colorVertex(bb.maxX, bb.minY, bb.maxZ, otherPos[2], otherPos[2].getAlpha(), bufferbuilder);
        colorVertex(bb.maxX, bb.minY, bb.minZ, otherPos[3], otherPos[3].getAlpha(), bufferbuilder);
        colorVertex(bb.minX, bb.minY, bb.minZ, otherPos[0], otherPos[0].getAlpha(), bufferbuilder);
        colorVertex(bb.minX, bb.maxY, bb.minZ, otherPos[4], otherPos[4].getAlpha(), bufferbuilder);
        colorVertex(bb.minX, bb.maxY, bb.maxZ, otherPos[5], otherPos[5].getAlpha(), bufferbuilder);
        colorVertex(bb.minX, bb.minY, bb.maxZ, otherPos[1], otherPos[1].getAlpha(), bufferbuilder);
        colorVertex(bb.maxX, bb.minY, bb.maxZ, otherPos[2], otherPos[2].getAlpha(), bufferbuilder);
        colorVertex(bb.maxX, bb.maxY, bb.maxZ, otherPos[6], otherPos[6].getAlpha(), bufferbuilder);
        colorVertex(bb.minX, bb.maxY, bb.maxZ, otherPos[5], otherPos[5].getAlpha(), bufferbuilder);
        colorVertex(bb.maxX, bb.maxY, bb.maxZ, otherPos[6], otherPos[6].getAlpha(), bufferbuilder);
        colorVertex(bb.maxX, bb.maxY, bb.minZ, otherPos[7], otherPos[7].getAlpha(), bufferbuilder);
        colorVertex(bb.maxX, bb.minY, bb.minZ, otherPos[3], otherPos[3].getAlpha(), bufferbuilder);
        colorVertex(bb.maxX, bb.maxY, bb.minZ, otherPos[7], otherPos[7].getAlpha(), bufferbuilder);
        colorVertex(bb.minX, bb.maxY, bb.minZ, otherPos[4], otherPos[4].getAlpha(), bufferbuilder);
        tessellator.draw();
    }

    public static void drawBoundingBox(AxisAlignedBB axisAlignedBB, double width,
                                       GSColor[] color, boolean five, int sides) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.glLineWidth((float) width);
        bufferbuilder.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);

        if ((sides & GeometryMasks.Quad.EAST) != 0) {
            colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ, color[2], color[2].getAlpha(), bufferbuilder);
            colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ, color[3], color[3].getAlpha(), bufferbuilder);
            colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ, color[7], color[7].getAlpha(), bufferbuilder);
            colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ, color[6], color[6].getAlpha(), bufferbuilder);
            if (five)
                colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ, color[2], color[2].getAlpha(), bufferbuilder);
        }

        if ((sides & GeometryMasks.Quad.WEST) != 0) {
            colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ, color[0], color[0].getAlpha(), bufferbuilder);
            colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ, color[1], color[1].getAlpha(), bufferbuilder);
            colorVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ, color[5], color[5].getAlpha(), bufferbuilder);
            colorVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ, color[4], color[4].getAlpha(), bufferbuilder);
            if (five)
                colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ, color[0], color[0].getAlpha(), bufferbuilder);
        }
        if ((sides & GeometryMasks.Quad.NORTH) != 0) {
            colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ, color[3], color[3].getAlpha(), bufferbuilder);
            colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ, color[0], color[0].getAlpha(), bufferbuilder);
            colorVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ, color[4], color[4].getAlpha(), bufferbuilder);
            colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ, color[7], color[7].getAlpha(), bufferbuilder);
            if (five)
                colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ, color[3], color[3].getAlpha(), bufferbuilder);
        }
        if ((sides & GeometryMasks.Quad.SOUTH) != 0) {
            colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ, color[1], color[1].getAlpha(), bufferbuilder);
            colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ, color[2], color[2].getAlpha(), bufferbuilder);
            colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ, color[6], color[6].getAlpha(), bufferbuilder);
            colorVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ, color[5], color[5].getAlpha(), bufferbuilder);
            if (five)
                colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ, color[1], color[1].getAlpha(), bufferbuilder);
        }
        if ((sides & GeometryMasks.Quad.UP) != 0) {
            colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ, color[7], color[7].getAlpha(), bufferbuilder);
            colorVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ, color[6], color[6].getAlpha(), bufferbuilder);
            colorVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ, color[5], color[5].getAlpha(), bufferbuilder);
            colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ, color[4], color[4].getAlpha(), bufferbuilder);
            if (five)
                colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ, color[7], color[7].getAlpha(), bufferbuilder);
        }
        if ((sides & GeometryMasks.Quad.DOWN) != 0) {
            colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ, color[3], color[3].getAlpha(), bufferbuilder);
            colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ, color[2], color[2].getAlpha(), bufferbuilder);
            colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ, color[1], color[1].getAlpha(), bufferbuilder);
            colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ, color[0], color[0].getAlpha(), bufferbuilder);
            if (five)
                colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ, color[3], color[3].getAlpha(), bufferbuilder);
        }

        /*
        colorVertex(bb.minX, bb.minY, bb.minZ, otherPos[0], otherPos[0].getAlpha(), bufferbuilder);
        colorVertex(bb.minX, bb.minY, bb.maxZ, otherPos[1], otherPos[1].getAlpha(), bufferbuilder);
        colorVertex(bb.maxX, bb.minY, bb.maxZ, otherPos[2], otherPos[2].getAlpha(), bufferbuilder);
        colorVertex(bb.maxX, bb.minY, bb.minZ, otherPos[3], otherPos[3].getAlpha(), bufferbuilder);
        colorVertex(bb.minX, bb.minY, bb.minZ, otherPos[0], otherPos[0].getAlpha(), bufferbuilder);
        colorVertex(bb.minX, bb.maxY, bb.minZ, otherPos[4], otherPos[4].getAlpha(), bufferbuilder);
        colorVertex(bb.minX, bb.maxY, bb.maxZ, otherPos[5], otherPos[5].getAlpha(), bufferbuilder);
        colorVertex(bb.minX, bb.minY, bb.maxZ, otherPos[1], otherPos[1].getAlpha(), bufferbuilder);
        colorVertex(bb.maxX, bb.minY, bb.maxZ, otherPos[2], otherPos[2].getAlpha(), bufferbuilder);
        colorVertex(bb.maxX, bb.maxY, bb.maxZ, otherPos[6], otherPos[6].getAlpha(), bufferbuilder);
        colorVertex(bb.minX, bb.maxY, bb.maxZ, otherPos[5], otherPos[5].getAlpha(), bufferbuilder);
        colorVertex(bb.maxX, bb.maxY, bb.maxZ, otherPos[6], otherPos[6].getAlpha(), bufferbuilder);
        colorVertex(bb.maxX, bb.maxY, bb.minZ, otherPos[7], otherPos[7].getAlpha(), bufferbuilder);
        colorVertex(bb.maxX, bb.minY, bb.minZ, otherPos[3], otherPos[3].getAlpha(), bufferbuilder);
        colorVertex(bb.maxX, bb.maxY, bb.minZ, otherPos[7], otherPos[7].getAlpha(), bufferbuilder);
        colorVertex(bb.minX, bb.maxY, bb.minZ, otherPos[4], otherPos[4].getAlpha(), bufferbuilder);*/
        tessellator.draw();
    }

    public static void drawBoundingBoxWithSides(BlockPos blockPos, int width, GSColor color, int sides) {
        drawBoundingBoxWithSides(getBoundingBox(blockPos, 1, 1, 1), width, color, color.getAlpha(), sides);
    }

    public static void drawBoundingBoxWithSides(BlockPos blockPos, int width, GSColor color, int alpha, int sides) {
        drawBoundingBoxWithSides(getBoundingBox(blockPos, 1, 1, 1), width, color, alpha, sides);
    }

    public static void drawBoundingBoxWithSides(AxisAlignedBB axisAlignedBB, int width, GSColor color, int sides) {
        drawBoundingBoxWithSides(axisAlignedBB, width, color, color.getAlpha(), sides);
    }

    public static void drawBoundingBoxWithSides(AxisAlignedBB axisAlignedBB, int width, GSColor color, int alpha, int sides) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.glLineWidth(width);
        bufferbuilder.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        doVerticies(axisAlignedBB, color, alpha, bufferbuilder, sides, true);
        tessellator.draw();
    }

    private static class Points {
        double[][] point = new double[10][2];
        private int count = 0;
        private final double xCenter;
        private final double zCenter;
        public final double yMin;
        public final double yMax;
        private final float rotation;

        public Points(double yMin, double yMax, double xCenter, double zCenter, float rotation) {
            this.yMin = yMin;
            this.yMax = yMax;
            this.xCenter = xCenter;
            this.zCenter = zCenter;
            this.rotation = rotation;
        }

        public void addPoints(double x, double z) {
            x -= xCenter;
            z -= zCenter;
            double rotateX = x * Math.cos(rotation) - z * Math.sin(rotation);
            double rotateZ = x * Math.sin(rotation) + z * Math.cos(rotation);
            rotateX += xCenter;
            rotateZ += zCenter;
            point[count++] = new double[]{rotateX, rotateZ};
        }

        public double[] getPoint(int index) {
            return point[index];
        }
    }

    public static void drawBoxWithDirection(AxisAlignedBB bb, GSColor color, float rotation, float width, int mode) {
        double xCenter = bb.minX + (bb.maxX - bb.minX) / 2;
        double zCenter = bb.minZ + (bb.maxZ - bb.minZ) / 2;

        Points square = new Points(bb.minY, bb.maxY, xCenter, zCenter, rotation);

        if (mode == 0) {
            square.addPoints(bb.minX, bb.minZ);
            square.addPoints(bb.minX, bb.maxZ);
            square.addPoints(bb.maxX, bb.maxZ);
            square.addPoints(bb.maxX, bb.minZ);
        }

        switch (mode) {
            case 0: {
                drawDirection(square, color, width);
                break;
            }
            //can add different modes in the future
        }
    }

    public static void drawDirection(Points square, GSColor color, float width) {
        for (int i = 0; i < 4; i++) {
            drawLine(square.getPoint(i)[0], square.yMin, square.getPoint(i)[1],
                square.getPoint((i + 1) % 4)[0], square.yMin, square.getPoint((i + 1) % 4)[1],
                color, width
            );
        }

        for (int i = 0; i < 4; i++) {
            drawLine(square.getPoint(i)[0], square.yMax, square.getPoint(i)[1],
                square.getPoint((i + 1) % 4)[0], square.yMax, square.getPoint((i + 1) % 4)[1],
                color, width
            );
        }

        for (int i = 0; i < 4; i++) {
            drawLine(square.getPoint(i)[0], square.yMin, square.getPoint(i)[1],
                square.getPoint(i)[0], square.yMax, square.getPoint(i)[1],
                color, width
            );
        }
    }

    public static void drawSphere(double x, double y, double z, float size, int slices, int stacks, float lineWidth, GSColor color) {
        Sphere sphere = new Sphere();
        GlStateManager.glLineWidth(lineWidth);
        color.glColor();
        sphere.setDrawStyle(GLU.GLU_SILHOUETTE);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x - mc.getRenderManager().viewerPosX, y - mc.getRenderManager().viewerPosY, z - mc.getRenderManager().viewerPosZ);
        sphere.draw(size, slices, stacks);
        GlStateManager.popMatrix();
    }

    public static void drawCircle(float x, float y, float z, float radius, GSColor colour) {
        //IBlockState iblockstate = RenderUtil.mc.world.getBlockState(new BlockPos(x, y, z));
        //Vec3d interpPos = EntityUtil.getInterpolatedPos(RenderUtil.mc.player, RenderUtil.mc.getRenderPartialTicks());
        BlockPos pos = new BlockPos(x, y, z);
        //AxisAlignedBB bb = iblockstate.getSelectedBoundingBox(RenderUtil.mc.world, new BlockPos(x, y, z)).offset(-interpPos.x, -interpPos.y, -interpPos.z);
        AxisAlignedBB bb = new AxisAlignedBB((double) pos.getX() - RenderUtil.mc.getRenderManager().viewerPosX, (double) pos.getY() - RenderUtil.mc.getRenderManager().viewerPosY,
                (double) pos.getZ() - RenderUtil.mc.getRenderManager().viewerPosZ,
                (double) (pos.getX() + 1) - RenderUtil.mc.getRenderManager().viewerPosX,
                (double) (pos.getY() + 1) - RenderUtil.mc.getRenderManager().viewerPosY, (double) (pos.getZ() + 1) - RenderUtil.mc.getRenderManager().viewerPosZ);
        drawCircleVertices(bb, radius, colour);

    }


    public static void drawCircleVertices(AxisAlignedBB bb, float radius, GSColor colour) {
        float r = (float) colour.getRed() / 255.0f;
        float g = (float) colour.getGreen() / 255.0f;
        float b = (float) colour.getBlue() / 255.0f;
        float a = (float) colour.getAlpha() / 255.0f;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        GL11.glEnable(2848);
        GL11.glHint(3154, 4354);
        GL11.glLineWidth(1f);
        for (int i = 0; i < 360; i++) {
            buffer.begin(3, DefaultVertexFormats.POSITION_COLOR);
            buffer.pos(bb.getCenter().x + (Math.sin((i * 3.1415926D / 180)) * radius), bb.minY, bb.getCenter().z + (Math.cos((i * 3.1415926D / 180)) * radius)).color(r, g, b, a).endVertex();
            buffer.pos(bb.getCenter().x + (Math.sin(((i + 1) * 3.1415926D / 180)) * radius), bb.minY, bb.getCenter().z + (Math.cos(((i + 1) * 3.1415926D / 180)) * radius)).color(r, g, b, a).endVertex();
            tessellator.draw();
        }
        GL11.glDisable(2848);
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public static void drawNametag(Entity entity, String[] text, GSColor color, int type) {
        Vec3d pos = EntityUtil.getInterpolatedPos(entity, mc.getRenderPartialTicks());
        drawNametag(pos.x, pos.y + entity.height, pos.z, text, color, type);
    }

    public static void drawNametag(double x, double y, double z, String[] text, GSColor color, int type) {
        ColorMain colorMain = ModuleManager.getModule(ColorMain.class);
        double dist = mc.player.getDistance(x, y, z);
        double scale = 1, offset = 0;
        int start = 0;
        switch (type) {
            case 0:
                scale = dist / 20 * Math.pow(1.2589254, 0.1 / (dist < 25 ? 0.5 : 2));
                scale = Math.min(Math.max(scale, .5), 5);
                offset = scale > 2 ? scale / 2 : scale;
                scale /= 40;
                start = 10;
                break;
            case 1:
                scale = -((int) dist) / 6.0;
                if (scale < 1) scale = 1;
                scale *= 2.0 / 75.0;
                break;
            case 2:
                scale = 0.0018 + 0.003 * dist;
                if (dist <= 8.0) scale = 0.0245;
                start = -8;
                break;
        }
        GlStateManager.pushMatrix();
        GlStateManager.translate(x - mc.getRenderManager().viewerPosX, y + offset - mc.getRenderManager().viewerPosY, z - mc.getRenderManager().viewerPosZ);
        GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0, 1, 0);
        GlStateManager.rotate(mc.getRenderManager().playerViewX, mc.gameSettings.thirdPersonView == 2 ? -1 : 1, 0, 0);
        GlStateManager.scale(-scale, -scale, scale);
        if (type == 2) {
            double width = 0;
            GSColor bcolor = new GSColor(0, 0, 0, 51);
            Nametags nametags = ModuleManager.getModule(Nametags.class);

            if (nametags.customColor.getValue()) {
                bcolor = nametags.borderColor.getValue();
            }
            for (int i = 0; i < text.length; i++) {
                double w = FontUtil.getStringWidth(colorMain.customFont.getValue(), text[i]) / 2;
                if (w > width) {
                    width = w;
                }
            }
            drawBorderedRect(-width - 1, -mc.fontRenderer.FONT_HEIGHT, width + 2, 1, 1.8f, new GSColor(0, 4, 0, 85), bcolor);
        }
        GlStateManager.enableTexture2D();
        for (int i = 0; i < text.length; i++) {
            FontUtil.drawStringWithShadow(colorMain.customFont.getValue(), text[i], -FontUtil.getStringWidth(colorMain.customFont.getValue(), text[i]) / 2, i * (mc.fontRenderer.FONT_HEIGHT + 1) + start, color);
        }
        GlStateManager.disableTexture2D();
        if (type != 2) {
            GlStateManager.popMatrix();
        }
    }

    private static void vertex(double x, double y, double z, BufferBuilder bufferbuilder) {
        bufferbuilder.pos(x - mc.getRenderManager().viewerPosX, y - mc.getRenderManager().viewerPosY, z - mc.getRenderManager().viewerPosZ).endVertex();
    }

    private static void colorVertex(double x, double y, double z, GSColor color, int alpha, BufferBuilder bufferbuilder) {
        bufferbuilder.pos(x - mc.getRenderManager().viewerPosX, y - mc.getRenderManager().viewerPosY, z - mc.getRenderManager().viewerPosZ).color(color.getRed(), color.getGreen(), color.getBlue(), alpha).endVertex();
    }

    private static AxisAlignedBB getBoundingBox(BlockPos bp, double width, double height, double depth) {
        double x = bp.getX();
        double y = bp.getY();
        double z = bp.getZ();
        return new AxisAlignedBB(x, y, z, x + width, y + height, z + depth);
    }

    private static void doVerticies(AxisAlignedBB axisAlignedBB, GSColor color, int alpha, BufferBuilder bufferbuilder, int sides, boolean five) {
        if ((sides & GeometryMasks.Quad.EAST) != 0) {
            colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ, color, color.getAlpha(), bufferbuilder);
            colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ, color, color.getAlpha(), bufferbuilder);
            colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ, color, alpha, bufferbuilder);
            colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ, color, alpha, bufferbuilder);
            if (five)
                colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ, color, color.getAlpha(), bufferbuilder);
        }
        if ((sides & GeometryMasks.Quad.WEST) != 0) {
            colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ, color, color.getAlpha(), bufferbuilder);
            colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ, color, color.getAlpha(), bufferbuilder);
            colorVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ, color, alpha, bufferbuilder);
            colorVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ, color, alpha, bufferbuilder);
            if (five)
                colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ, color, color.getAlpha(), bufferbuilder);
        }
        if ((sides & GeometryMasks.Quad.NORTH) != 0) {
            colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ, color, color.getAlpha(), bufferbuilder);
            colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ, color, color.getAlpha(), bufferbuilder);
            colorVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ, color, alpha, bufferbuilder);
            colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ, color, alpha, bufferbuilder);
            if (five)
                colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ, color, color.getAlpha(), bufferbuilder);
        }
        if ((sides & GeometryMasks.Quad.SOUTH) != 0) {
            colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ, color, color.getAlpha(), bufferbuilder);
            colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ, color, color.getAlpha(), bufferbuilder);
            colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ, color, alpha, bufferbuilder);
            colorVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ, color, alpha, bufferbuilder);
            if (five)
                colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ, color, color.getAlpha(), bufferbuilder);
        }
        if ((sides & GeometryMasks.Quad.UP) != 0) {
            colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ, color, alpha, bufferbuilder);
            colorVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ, color, alpha, bufferbuilder);
            colorVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ, color, alpha, bufferbuilder);
            colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ, color, alpha, bufferbuilder);
            if (five)
                colorVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ, color, alpha, bufferbuilder);
        }
        if ((sides & GeometryMasks.Quad.DOWN) != 0) {
            colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ, color, color.getAlpha(), bufferbuilder);
            colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ, color, color.getAlpha(), bufferbuilder);
            colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ, color, color.getAlpha(), bufferbuilder);
            colorVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ, color, color.getAlpha(), bufferbuilder);
            if (five)
                colorVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ, color, color.getAlpha(), bufferbuilder);
        }
    }

    public static void prepare() {
        glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ZERO, GL11.GL_ONE);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.enableAlpha();
        glEnable(GL11.GL_LINE_SMOOTH);
        glEnable(GL32.GL_DEPTH_CLAMP);
    }

    public static void release() {
        GL11.glDisable(GL32.GL_DEPTH_CLAMP);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GlStateManager.enableAlpha();
        GlStateManager.enableCull();
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
        GlStateManager.glLineWidth(1.0f);
        GlStateManager.shadeModel(GL11.GL_FLAT);
        glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_DONT_CARE);
    }
}