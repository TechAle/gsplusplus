package com.gamesense.client.module.modules.render;

import com.gamesense.api.event.events.RenderEvent;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.ColorSetting;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.render.GSColor;
import com.gamesense.api.util.render.shaders.impl.*;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;

import java.awt.*;
import java.util.Arrays;

@Module.Declaration(name = "Shaders", category = Category.Render)
public class Shaders extends Module {

    BooleanSetting glowESP = registerBoolean("Glow ESP", false);
    ColorSetting color = registerColor("Color", new GSColor(255, 255, 255));
    DoubleSetting radius = registerDouble("Radius", 1, 0, 5);
    DoubleSetting quality = registerDouble("Quality", 1, 0, 5);
    ModeSetting fillShader = registerMode("Fill Shader", Arrays.asList("Astral", "Aqua", "Red", "Smoke", "Test", "None"), "Astral");

    public boolean renderTags = true;
    public void onWorldRender(RenderEvent event) {

        if (mc.world == null)
            return;


        GlStateManager.pushMatrix();
        renderTags = false;

        if (glowESP.getValue()) {
            GlowShader.INSTANCE.startDraw(event.getPartialTicks());
            mc.world.loadedEntityList.stream().filter(e -> e instanceof EntityPlayer && e != mc.player).forEach(e -> mc.getRenderManager().renderEntityStatic(e, event.getPartialTicks(), true));
            GlowShader.INSTANCE.stopDraw(color.getValue(), radius.getValue().floatValue(), quality.getValue().floatValue());
        }

        switch (fillShader.getValue()) {
            case "Astral":
                FlowShader.INSTANCE.startDraw(event.getPartialTicks());
                mc.world.loadedEntityList.stream().filter(e -> e instanceof EntityPlayer && e != mc.player).forEach(e -> mc.getRenderManager().renderEntityStatic(e, event.getPartialTicks(), true));
                renderTags = true;
                FlowShader.INSTANCE.stopDraw(Color.WHITE, 1f, 1f);
                break;
            case "Aqua":
                AquaShader.INSTANCE.startDraw(event.getPartialTicks());
                mc.world.loadedEntityList.stream().filter(e -> e instanceof EntityPlayer && e != mc.player).forEach(e -> mc.getRenderManager().renderEntityStatic(e, event.getPartialTicks(), true));
                renderTags = true;
                AquaShader.INSTANCE.stopDraw(Color.WHITE, 1f, 1f);
                break;
            case "Red":
                RedShader.INSTANCE.startDraw(event.getPartialTicks());
                mc.world.loadedEntityList.stream().filter(e -> e instanceof EntityPlayer && e != mc.player).forEach(e -> mc.getRenderManager().renderEntityStatic(e, event.getPartialTicks(), true));
                renderTags = true;
                RedShader.INSTANCE.stopDraw(Color.WHITE, 1f, 1f);
                break;
            case "Smoke":
                SmokeShader.INSTANCE.startDraw(event.getPartialTicks());
                mc.world.loadedEntityList.stream().filter(e -> e instanceof EntityPlayer && e != mc.player).forEach(e -> mc.getRenderManager().renderEntityStatic(e, event.getPartialTicks(), true));
                renderTags = true;
                SmokeShader.INSTANCE.stopDraw(Color.WHITE, 1f, 1f);
                break;
            case "Test":
                TestShader.INSTANCE.startDraw(event.getPartialTicks());
                mc.world.loadedEntityList.stream().filter(e -> e instanceof EntityPlayer && e != mc.player).forEach(e -> mc.getRenderManager().renderEntityStatic(e, event.getPartialTicks(), true));
                renderTags = true;
                TestShader.INSTANCE.stopDraw(color.getValue(), 1f, 1f);
                break;
        }

        GlStateManager.popMatrix();
    }




}