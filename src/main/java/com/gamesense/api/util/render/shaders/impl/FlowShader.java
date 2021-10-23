package com.gamesense.api.util.render.shaders.impl;

import com.gamesense.api.util.render.shaders.FramebufferShader;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL20;

public class FlowShader extends FramebufferShader {

    public static final FlowShader INSTANCE;
    public float time;

    public FlowShader ( ) {
        super( "flow.frag" );
    }

    @Override public void setupUniforms ( ) {
        this.setupUniform( "resolution" );
        this.setupUniform( "time" );
    }

    @Override public void updateUniforms ( ) {
        GL20.glUniform2f( getUniform( "resolution" ), new ScaledResolution( mc ).getScaledWidth( ), new ScaledResolution( mc ).getScaledHeight( ) );
        GL20.glUniform1f( getUniform( "time" ), 1f );
    }
    static {
        INSTANCE = new FlowShader();
    }
}
