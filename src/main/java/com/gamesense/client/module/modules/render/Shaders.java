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
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

import java.awt.*;
import java.util.Arrays;

@Module.Declaration(name = "Shaders", category = Category.Render)
public class Shaders extends Module {

    BooleanSetting glowESP = registerBoolean("Glow ESP", false);
    ColorSetting colorESP = registerColor("Color ESP", new GSColor(255, 255, 255, 255));
    DoubleSetting radius = registerDouble("Radius ESP", 1, 0, 5);
    DoubleSetting quality = registerDouble("Quality ESP", 1, 0, 5);
    ModeSetting fillShader = registerMode("Fill Shader", Arrays.asList("Astral", "Aqua", "Red", "Smoke", "Triangle", "RainbowCube", "Gradient", "None"), "Astral");
    DoubleSetting speed = registerDouble("Speed", 0.1, 0.001, 0.1);
    DoubleSetting duplicate = registerDouble("Duplicate", 1, 0, 5);
    ColorSetting colorImg = registerColor("Color Img", new GSColor(0, 0, 0, 255), () -> fillShader.getValue().equals("Aqua"), true);
    IntegerSetting red = registerInteger("Red", 0, 0, 100, () -> fillShader.getValue().equals("Astral"));
    DoubleSetting green = registerDouble("Green", 0, 0, 5, () -> fillShader.getValue().equals("Astral"));
    DoubleSetting blue = registerDouble("Blue", 0, 0, 5, () -> fillShader.getValue().equals("Astral"));
    DoubleSetting alpha = registerDouble("Alpha", 1, 0, 1, () -> fillShader.getValue().equals("Astral"));
    IntegerSetting iterations = registerInteger("Iteration", 4, 3, 20, () -> fillShader.getValue().equals("Astral"));
    DoubleSetting formuparam2 = registerDouble("formuparam2", 0.89, 0, 1.5, () -> fillShader.getValue().equals("Astral"));
    DoubleSetting zoom = registerDouble("Zoom", 3.9, 0, 20, () -> fillShader.getValue().equals("Astral"));
    IntegerSetting volumSteps = registerInteger("Volum Steps", 10, 0, 10, () -> fillShader.getValue().equals("Astral"));
    DoubleSetting stepSize = registerDouble("Step Size", 0.190, 0.0, 0.7, () -> fillShader.getValue().equals("Astral"));
    DoubleSetting title = registerDouble("Tile", 0.45, 0, 1.3, () -> fillShader.getValue().equals("Astral"));
    DoubleSetting distfading = registerDouble("distfading", 0.56, 0, 1, () -> fillShader.getValue().equals("Astral"));
    DoubleSetting saturation = registerDouble("saturation", 0.4, 0, 3, () -> fillShader.getValue().equals("Astral"));
    BooleanSetting fade = registerBoolean("Fade", false, () -> fillShader.getValue().equals("Astral"));
    BooleanSetting items = registerBoolean("Items", false);
    BooleanSetting mobs = registerBoolean("Mobs", false);
    BooleanSetting players = registerBoolean("Players", false);
    BooleanSetting crystals = registerBoolean("Crystals", false);
    BooleanSetting xp = registerBoolean("XP", false);
    BooleanSetting rangeCheck = registerBoolean("Range Check", true);
    DoubleSetting minRange = registerDouble("Min range", 1, 0, 5, () -> rangeCheck.getValue());
    DoubleSetting maxRange = registerDouble("Max Range", 20, 10, 100, () -> rangeCheck.getValue());

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
                    FlowShader.INSTANCE.stopDraw(Color.WHITE, 1f, 1f, duplicate.getValue().floatValue(),
                            red.getValue().floatValue(), green.getValue().floatValue(), blue.getValue().floatValue(), alpha.getValue().floatValue(),
                            iterations.getValue(), formuparam2.getValue().floatValue(), zoom.getValue().floatValue(), volumSteps.getValue(), stepSize.getValue().floatValue(), title.getValue().floatValue(), distfading.getValue().floatValue(),
                            saturation.getValue().floatValue(), 0f, fade.getValue() ? 1 : 0);
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
                    TriangleShader.INSTANCE.stopDraw(colorESP.getValue(), 1f, 1f, duplicate.getValue().floatValue());
                    TriangleShader.INSTANCE.update(speed.getValue());
                    break;
                case "RainbowCube":
                    RainbowCubeShader.INSTANCE.startDraw(event.getPartialTicks());
                    renderPlayers(event.getPartialTicks());
                    RainbowCubeShader.INSTANCE.stopDraw(colorESP.getValue(), 1f, 1f, duplicate.getValue().floatValue());
                    RainbowCubeShader.INSTANCE.update(speed.getValue());
                    break;
                case "Gradient":
                    GradientShader.INSTANCE.startDraw(event.getPartialTicks());
                    renderPlayers(event.getPartialTicks());
                    GradientShader.INSTANCE.stopDraw(colorESP.getValue(), 1f, 1f, duplicate.getValue().floatValue());
                    GradientShader.INSTANCE.update(speed.getValue());
                    break;
            }


            if (glowESP.getValue()) {
                GlowShader.INSTANCE.startDraw(event.getPartialTicks());
                renderPlayers(event.getPartialTicks());
                GlowShader.INSTANCE.stopDraw(colorESP.getValue(), radius.getValue().floatValue(), quality.getValue().floatValue(), 0f);
            }

            renderTags = true;
            renderCape = true;

            GlStateManager.popMatrix();
        }
    });


    void renderPlayers(float tick) {
        boolean rangeCheck = this.rangeCheck.getValue();
        double minRange = this.minRange.getValue() * this.minRange.getValue();
        double maxRange = this.maxRange.getValue() * this.maxRange.getValue();
        double distance = 0;
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
            } else if (e instanceof EntityXPOrb) {
                if (xp.getValue())
                    return true;
            }
            return false;
                }
        ).filter(e -> {
            if (!rangeCheck)
                return true;
            else {
                double distancePl = mc.player.getDistanceSq(e);
                return distancePl > minRange && distancePl < maxRange;
            }
        }).forEach(e -> mc.getRenderManager().renderEntityStatic(e, tick, true));
    }




}