package com.gamesense.api.util.render.shaders.impl;

import com.gamesense.api.util.render.shaders.FramebufferShader;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL20;

public class TriangleShader extends FramebufferShader {

    public static final TriangleShader INSTANCE;
    public float time;

    public TriangleShader( ) {
        super( "triangle.frag" );
    }

    @Override public void setupUniforms ( ) {
        this.setupUniform( "resolution" );
        this.setupUniform( "time" );
    }

    @Override public void updateUniforms ( float duplicate ) {
        GL20.glUniform2f( getUniform( "resolution" ), new ScaledResolution( mc ).getScaledWidth( )/duplicate, new ScaledResolution( mc ).getScaledHeight( )/duplicate );
        GL20.glUniform1f( getUniform( "time" ), time );
    }
    static {
        INSTANCE = new TriangleShader();
    }

    public void update(double speed) {
        this.time += speed;
    }
}
