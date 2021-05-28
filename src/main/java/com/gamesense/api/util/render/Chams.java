package com.gamesense.api.util.render;

import net.minecraft.client.renderer.OpenGlHelper;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glHint;

public class Chams {

    public static void renderOne(final float lineWidth) {
        GL11.glPushAttrib(1048575);
        GL11.glDisable(3008);
        GL11.glDisable(3553);
        GL11.glDisable(2896);
        glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glLineWidth(lineWidth);
        glEnable(2848);
        glEnable(2960);
        GL11.glClear(1024);
        GL11.glClearStencil(15);
        GL11.glStencilFunc(512, 1, 15);
        GL11.glStencilOp(7681, 7681, 7681);
        GL11.glPolygonMode(1032, 6913);
    }

    public static void renderTwo() {
        GL11.glStencilFunc(512, 0, 15);
        GL11.glStencilOp(7681, 7681, 7681);
        GL11.glPolygonMode(1032, 6914);
    }

    public static void renderThree() {
        GL11.glStencilFunc(514, 1, 15);
        GL11.glStencilOp(7680, 7680, 7680);
        GL11.glPolygonMode(1032, 6913);
    }

    public static void renderFour(final GSColor color) {
        //color.glColor();
        GL11.glDepthMask(false);
        GL11.glDisable(2929);
        glEnable(10754);
        GL11.glPolygonOffset(1.0f, -2000000.0f);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0f, 240.0f);
    }

    public static void renderFive() {
        GL11.glPolygonOffset(1.0f, 2000000.0f);
        GL11.glDisable(10754);
        glEnable(2929);
        GL11.glDepthMask(true);
        GL11.glDisable(2960);
        GL11.glDisable(2848);
        glHint(3154, 4352);
        glEnable(3042);
        glEnable(2896);
        glEnable(3553);
        glEnable(3008);
        GL11.glPopAttrib();
    }

}
