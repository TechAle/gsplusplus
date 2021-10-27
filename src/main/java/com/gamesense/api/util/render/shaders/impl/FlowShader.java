package com.gamesense.api.util.render.shaders.impl;

import com.gamesense.api.util.render.shaders.FramebufferShader;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.awt.*;
import java.util.HashMap;

public class FlowShader extends FramebufferShader {

    public static final FlowShader INSTANCE;
    public float time;

    public FlowShader ( ) {
        super( "flow.frag" );
    }

    @Override public void setupUniforms ( ) {
        this.setupUniform( "resolution" );
        this.setupUniform( "time" );
        this.setupUniform("color");
        this.setupUniform("iterations");
        this.setupUniform("formuparam2");
    }

    public void updateUniforms (float duplicate, float red, float green, float blue, float alpha, int iteractions, float formuparam2) {
        GL20.glUniform2f( getUniform( "resolution" ), new ScaledResolution( mc ).getScaledWidth( ) / duplicate, new ScaledResolution( mc ).getScaledHeight( ) / duplicate );
        GL20.glUniform1f( getUniform( "time" ), time );
        GL20.glUniform4f(getUniform("color"), red, green, blue, alpha);
        GL20.glUniform1i(getUniform("iterations"), iteractions);
        GL20.glUniform1f(getUniform("formuparam2"), formuparam2);
    }

    public void stopDraw(final Color color, final float radius, final float quality, float duplicate, float red, float green, float blue, float alpha, int iteractions, float formuparam2) {
        mc.gameSettings.entityShadows = entityShadows;
        framebuffer.unbindFramebuffer( );
        GL11.glEnable( 3042 );
        GL11.glBlendFunc( 770, 771 );
        mc.getFramebuffer( ).bindFramebuffer( true );
        this.radius = radius;
        this.quality = quality;
        mc.entityRenderer.disableLightmap( );
        RenderHelper.disableStandardItemLighting( );
        startShader(duplicate, red, green, blue, alpha, iteractions, formuparam2);
        mc.entityRenderer.setupOverlayRendering( );
        drawFramebuffer( framebuffer );
        stopShader( );
        mc.entityRenderer.disableLightmap( );
        GlStateManager.popMatrix( );
        GlStateManager.popAttrib( );
    }

    public void startShader(float duplicate, float red, float green, float blue, float alpha, int iteractions, float formuparam2) {
        GL11.glPushMatrix();
        GL20.glUseProgram(this.program);
        if (this.uniformsMap == null) {
            this.uniformsMap = new HashMap<String, Integer>();
            this.setupUniforms();
        }
        this.updateUniforms(duplicate, red, green, blue, alpha, iteractions, formuparam2);
    }


    static {
        INSTANCE = new FlowShader();
    }

    public void update(double speed) {
        this.time += speed;
    }
}
