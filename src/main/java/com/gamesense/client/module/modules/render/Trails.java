package com.gamesense.client.module.modules.render;

import com.gamesense.api.event.events.RenderEvent;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.ColorSetting;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.util.render.GSColor;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL32;

import java.util.ArrayList;

@Module.Declaration(name = "Trails", category = Category.Render)
public class Trails extends Module {

    BooleanSetting self = registerBoolean("Self", true);
    BooleanSetting others = registerBoolean("Others", false);
    ColorSetting color = registerColor("Color", new GSColor(255, 255, 255, 255),
            () -> true, true);
    IntegerSetting life = registerInteger("Life", 300, 0, 1000);
    IntegerSetting lineWidth = registerInteger("Line Width", 1, 1, 5);
    DoubleSetting distance = registerDouble("Distance", 10, 0, 20);

    ArrayList<EntityPlayer> players = new ArrayList<>();
    ArrayList<ArrayList<Vec3d>> points = new ArrayList<>();
    ArrayList<ArrayList<GSColor>> colors = new ArrayList<>();
    ArrayList<ArrayList<Long>> lifeSpan = new ArrayList<>();


    @Override
    public void onUpdate() {
        if (mc.world == null || mc.player == null)
            return;
        for(int i = 0; i < points.size(); i++)
            if (points.get(i).size() > 0) {
                while (true) {
                    if (lifeSpan.get(i).size() > 0) {
                        if (System.currentTimeMillis() - lifeSpan.get(i).get(0) > life.getValue()) {
                            lifeSpan.get(i).remove(0);
                            colors.get(i).remove(0);
                            points.get(i).remove(0);
                        }
                        else break;
                    } else break;
                }

            }
            else {
                points.remove(i);
                colors.remove(i);
                players.remove(i);
                lifeSpan.remove(i);
                i--;
            }

        mc.world.playerEntities.forEach(e -> {
            boolean add = false;
            if (e == mc.player) {
                if (self.getValue())
                    add = true;
            } else if (others.getValue()) {
                add = true;
            }

            if (add) {
                if (mc.player.getDistanceSq(e) < distance.getValue() * distance.getValue()) {
                    int found = -1;
                    for (int i = 0; i < players.size(); i++)
                        if (players.get(i) == e) {
                            found = i;
                            break;
                        }
                    if (found == -1) {
                        players.add(e);
                        points.add(new ArrayList<>());
                        colors.add(new ArrayList<>());
                        lifeSpan.add(new ArrayList<>());
                        found = points.size() - 1;
                    }

                    points.get(found).add(new Vec3d(e.posX, e.posY, e.posZ));
                    colors.get(found).add(new GSColor(color.getValue(), color.getValue().getAlpha()));
                    lifeSpan.get(found).add(System.currentTimeMillis());
                }
            }
        });
    }

    @Override
    public void onWorldRender(RenderEvent event) {
        if (mc.world == null || mc.player == null)
            return;
        GlStateManager.pushMatrix();
        GL11.glLineWidth(1f);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL32.GL_DEPTH_CLAMP);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        GlStateManager.disableAlpha();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.depthMask(false);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();

        for(int i = 0; i < points.size(); i++) {
            ArrayList<Vec3d> externalVec3 = points.get(i);
            ArrayList<GSColor> externalColor = colors.get(i);
            GlStateManager.glLineWidth(lineWidth.getValue());
            GlStateManager.glBegin(GL11.GL_LINE_STRIP);
            for(int j = 0; j < points.get(i).size(); j++) {
                Vec3d pos = externalVec3.get(j);
                externalColor.get(j).glColor();
                GL11.glVertex3d(pos.x - mc.getRenderManager().viewerPosX, pos.y - mc.getRenderManager().viewerPosY, pos.z - mc.getRenderManager().viewerPosZ);
            }
            GlStateManager.glEnd();
        }
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableAlpha();
        GlStateManager.depthMask(true);
        GL11.glDisable(GL32.GL_DEPTH_CLAMP);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GlStateManager.color(1f, 1f, 1f);
        GL11.glLineWidth(1f);
        GlStateManager.popMatrix();
    }
}
