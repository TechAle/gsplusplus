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
        this.setupUniform( "time" );/*
        this.setupUniform("red");
        this.setupUniform("green");
        this.setupUniform("blue");
        this.setupUniform("alpha");
        this.setupUniform("iterations");
        this.setupUniform("zoom");
        this.setupUniform("formuparam2");
        this.setupUniform("volsteps");
        this.setupUniform("stepsize");
        this.setupUniform("title");
        this.setupUniform("distfading");
        this.setupUniform("saturation");
        this.setupUniform("cloud");
        this.setupUniform("fadeBol");*/
    }

    public void updateUniforms (float duplicate, float red, float green, float blue, float alpha, int iterations, float formuparam2, int volsteps
    , float stepsize, float zoom, float tile, float distfading, float cloud, int fade, float saturation) {
        GL20.glUniform2f(getUniform( "resolution" ), new ScaledResolution( mc ).getScaledWidth( ) / duplicate, new ScaledResolution( mc ).getScaledHeight( ) / duplicate );
        GL20.glUniform1f(getUniform( "time" ), time );/*
        GL20.glUniform1f(getUniform("red"), red);
        GL20.glUniform1f(getUniform("green"), green);
        GL20.glUniform1f(getUniform("blue"), blue);
        GL20.glUniform1f(getUniform("alpha"), alpha);
        GL20.glUniform1i(getUniform("iterations"), iterations);
        GL20.glUniform1f(getUniform("formuparam2"), formuparam2);
        GL20.glUniform1i(getUniform("volsteps"), volsteps);
        GL20.glUniform1f(getUniform("stepsize"), stepsize);
        GL20.glUniform1f(getUniform("zoom"), zoom);
        GL20.glUniform1f(getUniform("title"), tile);
        GL20.glUniform1f(getUniform("stepsize"), stepsize);
        GL20.glUniform1f(getUniform("distfading"), distfading);
        GL20.glUniform1f(getUniform("cloud"), cloud);
        GL20.glUniform1i(getUniform("fadeBol"), fade);
        GL20.glUniform1f(getUniform("saturation"), saturation);*/
    }

    public void stopDraw(float duplicate, float red, float green, float blue, float alpha, int iterations, float formuparam2, int volsteps
            , float stepsize, float zoom, float tile, float distfading, float cloud, int fade, float saturation) {
        mc.gameSettings.entityShadows = entityShadows;
        framebuffer.unbindFramebuffer( );
        GL11.glEnable( 3042 );
        GL11.glBlendFunc( 770, 771 );
        mc.getFramebuffer( ).bindFramebuffer( true );
        mc.entityRenderer.disableLightmap( );
        RenderHelper.disableStandardItemLighting( );
        startShader(duplicate, red, green, blue, alpha, iterations, formuparam2, volsteps, stepsize, zoom, tile, distfading, cloud, fade, saturation);
        mc.entityRenderer.setupOverlayRendering( );
        drawFramebuffer( framebuffer );
        stopShader( );
        mc.entityRenderer.disableLightmap( );
        GlStateManager.popMatrix( );
        GlStateManager.popAttrib( );
    }

    public void startShader(float duplicate, float red, float green, float blue, float alpha, int iterations, float formuparam2, int volsteps
            , float stepsize, float zoom, float tile, float distfading, float cloud, int fade, float saturation) {
        GL11.glPushMatrix();
        GL20.glUseProgram(this.program);
        if (this.uniformsMap == null) {
            this.uniformsMap = new HashMap<String, Integer>();
            this.setupUniforms();
        }
        this.updateUniforms(duplicate, red, green, blue, alpha, iterations, formuparam2, volsteps, stepsize, zoom, tile, distfading, cloud, fade, saturation);
    }



    static {
        INSTANCE = new FlowShader();
    }

    public void update(double speed) {
        this.time += speed;
    }
}
