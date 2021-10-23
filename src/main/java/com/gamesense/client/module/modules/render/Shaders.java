package com.gamesense.client.module.modules.render;

import com.gamesense.api.event.events.RenderEvent;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.ColorSetting;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.util.render.GSColor;
import com.gamesense.api.util.render.shaders.impl.FlowShader;
import com.gamesense.api.util.render.shaders.impl.GlowShader;
import com.gamesense.api.util.render.shaders.impl.TestShader;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;

import java.awt.*;

@Module.Declaration(name = "Shaders", category = Category.Render)
public class Shaders extends Module {

    BooleanSetting playerShader = registerBoolean("PlayerShader", false);
    BooleanSetting glowESP = registerBoolean("Glow ESP", false);
    ColorSetting color = registerColor("Color", new GSColor(255, 255, 255));
    DoubleSetting radius = registerDouble("Radius", 1, 0, 5);
    DoubleSetting quality = registerDouble("Quality", 1, 0, 5);

    public void onWorldRender(RenderEvent event) {

        if (mc.world == null)
            return;


        GlStateManager.pushMatrix();
        if (playerShader.getValue()) {
            FlowShader.INSTANCE.startDraw(event.getPartialTicks());
            mc.world.loadedEntityList.stream().filter(e -> e instanceof EntityPlayer && e != mc.player).forEach(e -> mc.getRenderManager().renderEntityStatic(e, event.getPartialTicks(), true));
            FlowShader.INSTANCE.stopDraw(Color.WHITE, 1f, 1f);
        }
        if (glowESP.getValue()) {
            GlowShader.INSTANCE.startDraw(event.getPartialTicks());
            mc.world.loadedEntityList.stream().filter(e -> e instanceof EntityPlayer && e != mc.player).forEach(e -> mc.getRenderManager().renderEntityStatic(e, event.getPartialTicks(), true));
            GlowShader.INSTANCE.stopDraw(color.getValue(), radius.getValue().floatValue(), quality.getValue().floatValue());
        }
        /*
        if (test.getValue()) {
            TestShader.INSTANCE.startDraw(event.getPartialTicks());
            mc.world.loadedEntityList.stream().filter(e -> e instanceof EntityPlayer && e != mc.player).forEach(e -> mc.getRenderManager().renderEntityStatic(e, event.getPartialTicks(), true));
            TestShader.INSTANCE.stopDraw(color.getValue(), 1f, 1f);
        }*/
        GlStateManager.popMatrix();
    }

}