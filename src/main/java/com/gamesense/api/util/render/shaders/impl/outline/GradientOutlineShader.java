package com.gamesense.api.util.render.shaders.impl.outline;

import com.gamesense.api.util.render.shaders.FramebufferShader;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL20;

public final class GradientOutlineShader extends FramebufferShader
{
    public static final GradientOutlineShader INSTANCE;
    public float time = 0;

    public GradientOutlineShader() {
        super("outlineGradient.frag");
    }

    @Override public void setupUniforms() {
        this.setupUniform("texture");
        this.setupUniform("texelSize");
        this.setupUniform("color");
        this.setupUniform("divider");
        this.setupUniform("radius");
        this.setupUniform("maxSample");
        this.setupUniform("time");
        this.setupUniform("rainbowStrength");
        this.setupUniform("rainbowSpeed");
        this.setupUniform("saturation");
        this.setupUniform("resolution");
    }

    @Override public void updateUniforms(float duplicate) {
        GL20.glUniform1i(this.getUniform("texture"), 0);
        GL20.glUniform2f(this.getUniform("texelSize"), 1.0f / this.mc.displayWidth * (this.radius * this.quality), 1.0f / this.mc.displayHeight * (this.radius * this.quality));
        GL20.glUniform3f(this.getUniform("color"), this.red, this.green, this.blue);
        GL20.glUniform1f(this.getUniform("divider"), 140.0f);
        GL20.glUniform1f(this.getUniform("radius"), this.radius);
        GL20.glUniform1f(this.getUniform("maxSample"), 10.0f);
        GL20.glUniform1f(this.getUniform("time"), System.currentTimeMillis());
        GL20.glUniform2f(this.getUniform("rainbowStrength"), 1f, 1f);
        GL20.glUniform1f(this.getUniform("rainbowSpeed"), -1.1f);
        GL20.glUniform1f(this.getUniform("saturation"), 1f);
        GL20.glUniform2f( getUniform( "resolution" ), new ScaledResolution( mc ).getScaledWidth( ) / duplicate, new ScaledResolution( mc ).getScaledHeight( ) / duplicate );
    }

    static {
        INSTANCE = new GradientOutlineShader();
    }
}