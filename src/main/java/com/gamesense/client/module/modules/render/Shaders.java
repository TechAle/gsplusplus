package com.gamesense.client.module.modules.render;

import com.gamesense.api.setting.values.*;
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

import java.awt.*;
import java.util.Arrays;

@Module.Declaration(name = "Shaders", category = Category.Render)
public class Shaders extends Module {

    BooleanSetting glowESP = registerBoolean("Glow ESP", false);
    ColorSetting color = registerColor("Color ESP", new GSColor(255, 255, 255));
    DoubleSetting radius = registerDouble("Radius", 1, 0, 5);
    DoubleSetting quality = registerDouble("Quality", 1, 0, 5);
    ModeSetting fillShader = registerMode("Fill Shader", Arrays.asList("Astral", "Aqua", "Red", "Smoke", "Triangle", "RainbowCube", "Gradient", "None"), "Astral");
    DoubleSetting speed = registerDouble("Speed", 0.1, 0.001, 0.1);
    DoubleSetting duplicate = registerDouble("Duplicate", 1, 0, 5);
    ColorSetting colorImg = registerColor("Color Shader", new GSColor(0, 0, 0), () -> "Aqua".equals(fillShader.getValue()));
    IntegerSetting redAstralBack = registerInteger("Red Astral Back", 10, 10, 100);
    DoubleSetting greenAstralBack = registerDouble("Green Astral Back", 0, 0, 5);
    DoubleSetting blueAstralBack = registerDouble("Blue Astral Back", 0, 0, 5);
    DoubleSetting alphaAstralBack = registerDouble("Alpha Astral Back", 1, 0, 1);
    IntegerSetting iterationsAstral = registerInteger("Iteration Astral", 4, 3, 20);
    DoubleSetting zoom = registerDouble("Zoom", 3.9, 0, 20);
    DoubleSetting formuparam2 = registerDouble("formuparam2", 0.89, 0, 1.5);
    IntegerSetting volumSteps = registerInteger("Volum Steps", 10, 0, 10);
    DoubleSetting stepSize = registerDouble("Step Size", 0.180, 0.0, 0.7);
    DoubleSetting title = registerDouble("Title", 0.45, 0, 1.3);
    DoubleSetting distfading = registerDouble("distfading", 0.56, 0, 1);
    DoubleSetting saturation = registerDouble("saturation", 0.4, 0, 3);
    DoubleSetting cloud = registerDouble("Cloud", 0.4, 0, 1);
    BooleanSetting fade = registerBoolean("Fade", false);
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
                    FlowShader.INSTANCE.stopDraw(duplicate.getValue().floatValue(), redAstralBack.getValue().floatValue(), greenAstralBack.getValue().floatValue(), blueAstralBack.getValue().floatValue(),
                            alphaAstralBack.getValue().floatValue(), iterationsAstral.getValue(), formuparam2.getValue().floatValue(), volumSteps.getValue(),
                            stepSize.getValue().floatValue(), zoom.getValue().floatValue(), title.getValue().floatValue(), distfading.getValue().floatValue(), cloud.getValue().floatValue(),
                            fade.getValue() ? 1 : 0, saturation.getValue().floatValue());
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