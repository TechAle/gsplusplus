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
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.Arrays;

@Module.Declaration(name = "Shaders", category = Category.Render)
public class Shaders extends Module {

    BooleanSetting glowESP = registerBoolean("Glow ESP", false);
    ColorSetting color = registerColor("Color", new GSColor(255, 255, 255));
    DoubleSetting radius = registerDouble("Radius", 1, 0, 5);
    DoubleSetting quality = registerDouble("Quality", 1, 0, 5);
    ModeSetting fillShader = registerMode("Fill Shader", Arrays.asList("Astral", "Aqua", "Red", "Smoke", "Triangle", "RainbowCube", "Gradient", "None"), "Astral");
    DoubleSetting speed = registerDouble("Speed", 0.1, 0.001, 0.1);
    DoubleSetting duplicate = registerDouble("Duplicate", 1, 0, 5);
    ColorSetting colorImg = registerColor("Color", new GSColor(0, 0, 0));
    BooleanSetting items = registerBoolean("Items", false);
    BooleanSetting mobs = registerBoolean("Mobs", false);
    BooleanSetting players = registerBoolean("Players", false);
    BooleanSetting crystals = registerBoolean("Crystals", false);

    public boolean renderTags = true,
                   renderCape = true;


    @EventHandler
    private final Listener<RenderGameOverlayEvent.Pre> renderGameOverlayEventListener = new Listener<>(event -> {

        if (event.getType() == RenderGameOverlayEvent.ElementType.HOTBAR) {
            if (mc.world == null)
                return;


            GlStateManager.pushMatrix();
            renderTags = false;
            renderCape = false;

            switch (fillShader.getValue()) {
                case "Astral":
                    FlowShader.INSTANCE.startDraw(event.getPartialTicks());
                    renderPlayers(event.getPartialTicks());
                    FlowShader.INSTANCE.stopDraw(Color.WHITE, 1f, 1f, duplicate.getValue().floatValue());
                    FlowShader.INSTANCE.update(speed.getValue());
                    break;
                case "Aqua":
                    AquaShader.INSTANCE.startDraw(event.getPartialTicks());
                    renderPlayers(event.getPartialTicks());
                    AquaShader.INSTANCE.stopDraw(colorImg.getColor(), 1f, 1f, duplicate.getValue().floatValue());
                    AquaShader.INSTANCE.update(speed.getValue());
                    break;
                case "Red":
                    RedShader.INSTANCE.startDraw(event.getPartialTicks());
                    renderPlayers(event.getPartialTicks());
                    RedShader.INSTANCE.stopDraw(Color.WHITE, 1f, 1f, duplicate.getValue().floatValue());
                    RedShader.INSTANCE.update(speed.getValue());
                    break;
                case "Smoke":
                    SmokeShader.INSTANCE.startDraw(event.getPartialTicks());
                    renderPlayers(event.getPartialTicks());
                    SmokeShader.INSTANCE.stopDraw(Color.WHITE, 1f, 1f, duplicate.getValue().floatValue());
                    SmokeShader.INSTANCE.update(speed.getValue());
                    break;
                case "Triangle":
                    TriangleShader.INSTANCE.startDraw(event.getPartialTicks());
                    renderPlayers(event.getPartialTicks());
                    TriangleShader.INSTANCE.stopDraw(color.getValue(), 1f, 1f, duplicate.getValue().floatValue());
                    TriangleShader.INSTANCE.update(speed.getValue());
                    break;
                case "RainbowCube":
                    RainbowCubeShader.INSTANCE.startDraw(event.getPartialTicks());
                    renderPlayers(event.getPartialTicks());
                    RainbowCubeShader.INSTANCE.stopDraw(color.getValue(), 1f, 1f, duplicate.getValue().floatValue());
                    RainbowCubeShader.INSTANCE.update(speed.getValue());
                    break;
                case "Gradient":
                    GradientShader.INSTANCE.startDraw(event.getPartialTicks());
                    renderPlayers(event.getPartialTicks());
                    GradientShader.INSTANCE.stopDraw(color.getValue(), 1f, 1f, duplicate.getValue().floatValue());
                    GradientShader.INSTANCE.update(speed.getValue());
                    break;
            }


            if (glowESP.getValue()) {
                GlowShader.INSTANCE.startDraw(event.getPartialTicks());
                renderPlayers(event.getPartialTicks());
                GlowShader.INSTANCE.stopDraw(color.getValue(), radius.getValue().floatValue(), quality.getValue().floatValue(), 0f);
            }

            renderTags = true;
            renderCape = true;

            GlStateManager.popMatrix();
        }
    });


    void renderPlayers(float tick) {
        mc.world.loadedEntityList.stream().filter(e -> {
            if (e instanceof EntityPlayer) {
                if (players.getValue())
                    if (e != mc.player || mc.gameSettings.thirdPersonView != 0)
                        return true;
            } else if (e instanceof EntityItem) {
                if (items.getValue())
                    return true;
            } else if (e instanceof EntityCreature) {
                if (mobs.getValue())
                    return true;
            } else if (e instanceof EntityEnderCrystal) {
                if (crystals.getValue())
                    return true;
            }
            return false;
                }
        ).forEach(e -> mc.getRenderManager().renderEntityStatic(e, tick, true));
    }




}