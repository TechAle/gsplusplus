package com.gamesense.client.module.modules.render;

import com.gamesense.api.event.events.RenderEvent;
import com.gamesense.api.event.events.ShaderColorEvent;
import com.gamesense.api.event.events.TransformSideFirstPersonEvent;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.ColorSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.player.social.SocialManager;
import com.gamesense.api.util.render.GSColor;
import com.gamesense.api.util.render.RenderUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.ModuleManager;
import com.gamesense.client.module.modules.gui.ColorMain;
import com.gamesense.mixin.mixins.accessor.IRenderGlobal;
import com.gamesense.mixin.mixins.accessor.IShaderGroup;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.client.shader.Shader;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.client.shader.ShaderUniform;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.item.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.*;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.Arrays;
import java.util.List;

/**
 * @author Hoosiers on 09/22/2020
 * @author Techale on 12/19/2020
 * Thanks to cosmos for glowing color
 */

@Module.Declaration(name = "ESP", category = Category.Render)
public class ESP extends Module {

    List<String> Modes = Arrays.asList("None", "Box", "Direction", "Glowing");
    IntegerSetting range = registerInteger("Range", 100, 10, 260);
    IntegerSetting width = registerInteger("Line Width", 2, 1, 5);
    ModeSetting playerESPMode = registerMode("Player Esp", Modes, "Box");
    ModeSetting itemEsp = registerMode("Item Esp", Modes, "None");
    ModeSetting mobEsp = registerMode("Entity Esp", Modes, "None");
    ModeSetting crystalEsp = registerMode("Crystal Esp", Modes, "None");
    ColorSetting playerColor = registerColor("Player Color", new GSColor(255, 255, 0));
    ColorSetting friendColor = registerColor("Friend Color", new GSColor(0, 255, 255));
    ColorSetting enemyColor = registerColor("Enemy Color", new GSColor(255, 0, 0));
    ColorSetting mobColor = registerColor("Mob Color", new GSColor(0, 255, 0));
    ColorSetting itemColor = registerColor("Item Color", new GSColor(255, 255, 255));
    ColorSetting crystalColor = registerColor("Crystal Color", new GSColor(255, 0, 255));

    int opacityGradient;

    public void onWorldRender(RenderEvent event) {
        mc.world.loadedEntityList.stream().filter(entity -> entity != mc.player).filter(this::rangeEntityCheck).forEach(entity -> {

            if (entity instanceof EntityPlayer)
                render(playerESPMode.getValue(), entity, playerColor.getValue());
            else if (entity instanceof EntityCreature )
                render(mobEsp.getValue(), entity, mobColor.getValue());
            else if (entity instanceof EntityItem)
                render(itemEsp.getValue(), entity, itemColor.getValue());
            else if (entity instanceof EntityEnderCrystal)
                render(crystalEsp.getValue(), entity, crystalColor.getValue());

        });

    }


    @EventHandler
    private final Listener<ShaderColorEvent> eventListener = new Listener<>(event -> {
        Entity e = event.getEntity();
        boolean cancel = false;
        GSColor color = null;
        if (e instanceof EntityPlayer && playerESPMode.getValue().equals("Shader")) {
            color = playerColor.getValue();
            cancel = true;
        } else if (e instanceof EntityCreature && mobEsp.getValue().equals("Shader")) {
            color = mobColor.getValue();
            cancel = true;
        } else if (e instanceof EntityItem && itemEsp.getValue().equals("Shader")) {
            color = itemColor.getValue();
            cancel = true;
        } else if (e instanceof EntityEnderCrystal && crystalEsp.getValue().equals("Shader")) {
            color = crystalColor.getValue();
            cancel = true;
        }

        if (cancel) {
            event.setColor(color);
            event.cancel();
        }
    });

    void render(String type, Entity e, GSColor color) {
        switch (type) {
            case "Box":
                e.setGlowing(false);
                RenderUtil.drawBoundingBox(e.getEntityBoundingBox(), width.getValue(), color);
                break;
            case "Direction":
                e.setGlowing(false);
                RenderUtil.drawBoxWithDirection(e.getEntityBoundingBox(), color, e.rotationYaw, width.getValue(), 0);
                break;
            case "Glowing":
                e.setGlowing(true);
                ShaderGroup outlineShaderGroup = ((IRenderGlobal) mc.renderGlobal).getEntityOutlineShader();
                List<Shader> shaders = ((IShaderGroup) outlineShaderGroup).getListShaders();

                shaders.forEach(shader -> {
                    ShaderUniform outlineRadius = shader.getShaderManager().getShaderUniform("Radius");

                    if (outlineRadius != null)
                        outlineRadius.set(width.getValue().floatValue());
                });
                break;
        }
    }

    public void onDisable() {
        mc.world.loadedEntityList.forEach(entity -> entity.setGlowing(false));
    }

    private boolean rangeEntityCheck(Entity entity) {
        if (entity.getDistance(mc.player) > range.getValue()) {
            return false;
        }

        if (entity.getDistance(mc.player) >= 180) {
            opacityGradient = 50;
        } else if (entity.getDistance(mc.player) >= 130 && entity.getDistance(mc.player) < 180) {
            opacityGradient = 100;
        } else if (entity.getDistance(mc.player) >= 80 && entity.getDistance(mc.player) < 130) {
            opacityGradient = 150;
        } else if (entity.getDistance(mc.player) >= 30 && entity.getDistance(mc.player) < 80) {
            opacityGradient = 200;
        } else {
            opacityGradient = 255;
        }

        return true;
    }

}