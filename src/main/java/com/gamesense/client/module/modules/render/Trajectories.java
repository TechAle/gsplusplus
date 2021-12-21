package com.gamesense.client.module.modules.render;

import com.gamesense.api.event.events.RenderEvent;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.ColorSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.util.render.GSColor;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.item.*;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Cylinder;
import org.lwjgl.util.glu.GLU;

import java.util.ArrayList;
import java.util.List;

@Module.Declaration(name = "Trajectories", category = Category.Render)
public class Trajectories extends Module {

    ColorSetting color = registerColor("Color", new GSColor(255, 255, 255, 255), () -> true, true);
    BooleanSetting landed = registerBoolean("Landed", true);
    BooleanSetting line = registerBoolean("Line", true);
    BooleanSetting rainbowLine = registerBoolean("Rainbow Line", false);
    IntegerSetting rainbowSpeed = registerInteger("Rainbow Speed", 1, 1, 100);
    IntegerSetting rainbowDesync = registerInteger("Rainbow Desync", 1, 1, 500);

    long count = 0;
    @Override
    public void onWorldRender(RenderEvent event) {
        count += rainbowSpeed.getValue();
        long start = count;
        if (mc.player == null || mc.world == null || mc.gameSettings.thirdPersonView != 0)
            return;
        if (!((mc.player.getHeldItemMainhand() != ItemStack.EMPTY && mc.player.getHeldItemMainhand().getItem() instanceof ItemBow) || (mc.player.getHeldItemMainhand() != ItemStack.EMPTY && isThrowable(mc.player.getHeldItemMainhand().getItem())) || (mc.player.getHeldItemOffhand() != ItemStack.EMPTY && isThrowable(mc.player.getHeldItemOffhand().getItem()))))
            return;
        final double renderPosX = mc.player.lastTickPosX + (mc.player.posX - mc.player.lastTickPosX) * event.getPartialTicks();
        final double renderPosY = mc.player.lastTickPosY + (mc.player.posY - mc.player.lastTickPosY) * event.getPartialTicks();
        final double renderPosZ = mc.player.lastTickPosZ + (mc.player.posZ - mc.player.lastTickPosZ) * event.getPartialTicks();
        mc.player.getHeldItem(EnumHand.MAIN_HAND);        
        Item item = null;
        if (mc.player.getHeldItemMainhand() != ItemStack.EMPTY && (mc.player.getHeldItemMainhand().getItem() instanceof ItemBow || isThrowable(mc.player.getHeldItemMainhand().getItem()))) {
            item = mc.player.getHeldItemMainhand().getItem();
        } else if (mc.player.getHeldItemOffhand() != ItemStack.EMPTY && isThrowable(mc.player.getHeldItemOffhand().getItem())) {
            item = mc.player.getHeldItemOffhand().getItem();
        }
        if (item == null) return;
        double posX = renderPosX - Math.cos(mc.player.rotationYaw / 180.0f * 3.1415927f) * 0.16f;
        double posY = renderPosY + mc.player.getEyeHeight() - 0.1000000014901161;
        double posZ = renderPosZ - Math.sin(mc.player.rotationYaw / 180.0f * 3.1415927f) * 0.16f;
        final float maxDist = getDistance(item);
        double motionX = -Math.sin(mc.player.rotationYaw / 180.0f * 3.1415927f) * Math.cos(mc.player.rotationPitch / 180.0f * 3.1415927f) * maxDist;
        double motionY = -Math.sin((mc.player.rotationPitch - getThrowPitch(item)) / 180.0f * 3.141593f) * maxDist;
        double motionZ = Math.cos(mc.player.rotationYaw / 180.0f * 3.1415927f) * Math.cos(mc.player.rotationPitch / 180.0f * 3.1415927f) * maxDist;
        int var6 = 72000 - mc.player.getItemInUseCount();
        float power = var6 / 20.0f;
        power = (power * power + power * 2.0f) / 3.0f;
        if (power > 1.0f) {
            power = 1.0f;
        }
        final float distance = (float) Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
        motionX /= distance;
        motionY /= distance;
        motionZ /= distance;

        final float pow = (item instanceof ItemBow ? (power * 2.0f) : 1.0f) * getThrowVelocity(item);
        motionX *= pow;
        motionY *= pow;
        motionZ *= pow;
        if (!mc.player.onGround)
            motionY += mc.player.motionY;
        GlStateManager.pushMatrix();
        GlStateManager.color(color.getValue().getRed() / 255.f, color.getValue().getGreen() / 255.f, color.getValue().getBlue() / 255.f, color.getValue().getAlpha() / 255.f);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        final float size = (float) ((item instanceof ItemBow) ? 0.3 : 0.25);
        boolean hasLanded = false;
        Entity landingOnEntity = null;
        RayTraceResult landingPosition = null;
        GL11.glBegin(GL11.GL_LINE_STRIP);
        while (!hasLanded && posY > 0.0) {
            Vec3d present = new Vec3d(posX, posY, posZ);
            Vec3d future = new Vec3d(posX + motionX, posY + motionY, posZ + motionZ);
            RayTraceResult possibleLandingStrip = mc.world.rayTraceBlocks(present, future, false, true, false);
            if (possibleLandingStrip != null && possibleLandingStrip.typeOfHit != RayTraceResult.Type.MISS) {
                landingPosition = possibleLandingStrip;
                hasLanded = true;
            }
            AxisAlignedBB arrowBox = new AxisAlignedBB(posX - size, posY - size, posZ - size, posX + size, posY + size, posZ + size);
            List<Entity> entities = getEntitiesWithinAABB(arrowBox.offset(motionX, motionY, motionZ).expand(1.0, 1.0, 1.0));
            for (Object entity : entities) {
                Entity boundingBox = (Entity) entity;
                if (boundingBox.canBeCollidedWith() && boundingBox != mc.player) {
                    float var7 = 0.3f;
                    AxisAlignedBB var8 = boundingBox.getEntityBoundingBox().expand(var7, var7, var7);
                    RayTraceResult possibleEntityLanding = var8.calculateIntercept(present, future);
                    if (possibleEntityLanding == null) {
                        continue;
                    }
                    hasLanded = true;
                    landingOnEntity = boundingBox;
                    landingPosition = possibleEntityLanding;
                }
            }
            if (landingOnEntity != null) {
                GlStateManager.color(1.0f, 0.0f, 0.0f, color.getValue().getAlpha() / 255.0f);
            }
            posX += motionX;
            posY += motionY;
            posZ += motionZ;
            final float motionAdjustment = 0.99f;
            motionX *= motionAdjustment;
            motionY *= motionAdjustment;
            motionZ *= motionAdjustment;
            motionY -= getGravity(item);
            if (rainbowLine.getValue())
                start += rainbowDesync.getValue();
            if (line.getValue())
                drawLine3D(posX - renderPosX, posY - renderPosY, posZ - renderPosZ, start);
        }
        GL11.glEnd();
        if (landed.getValue() &&  landingPosition != null && landingPosition.typeOfHit == RayTraceResult.Type.BLOCK) {
            GlStateManager.translate(posX - renderPosX, posY - renderPosY, posZ - renderPosZ);
            final int side = landingPosition.sideHit.getIndex();
            if (side == 2) {
                GlStateManager.rotate(90.0f, 1.0f, 0.0f, 0.0f);
            } else if (side == 3) {
                GlStateManager.rotate(90.0f, 1.0f, 0.0f, 0.0f);
            } else if (side == 4) {
                GlStateManager.rotate(90.0f, 0.0f, 0.0f, 1.0f);
            } else if (side == 5) {
                GlStateManager.rotate(90.0f, 0.0f, 0.0f, 1.0f);
            }
            final Cylinder c = new Cylinder();
            GlStateManager.rotate(-90.0f, 1.0f, 0.0f, 0.0f);
            c.setDrawStyle(GLU.GLU_SILHOUETTE);
            if (landingOnEntity != null) {
                GlStateManager.color(0.0f, 0.0f, 0.0f, color.getValue().getAlpha() / 255.0f);
                GL11.glLineWidth(2.5f);
                c.draw(0.5f, 0.15f, 0.0f, 8, 1);
                GL11.glLineWidth(0.1f);
                GlStateManager.color(1.0f, 0.0f, 0.0f, color.getValue().getAlpha() / 255.0f);
            }
            c.draw(0.5f, 0.15f, 0.0f, 8, 1);
        }
        GlStateManager.popMatrix();
    }

    protected boolean isThrowable(Item item) {
        return item instanceof ItemEnderPearl
                || item instanceof ItemExpBottle
                || item instanceof ItemSnowball
                || item instanceof ItemEgg
                || item instanceof ItemSplashPotion
                || item instanceof ItemLingeringPotion;
    }

    protected float getDistance(Item item) {
        return item instanceof ItemBow ? 1.0f : 0.4f;
    }

    protected float getThrowVelocity(Item item) {
        if (item instanceof ItemSplashPotion || item instanceof ItemLingeringPotion) {
            return 0.5f;
        }
        if (item instanceof ItemExpBottle) {
            return 0.59f;
        }
        return 1.5f;
    }

    protected int getThrowPitch(Item item) {
        if (item instanceof ItemSplashPotion || item instanceof ItemLingeringPotion || item instanceof ItemExpBottle) {
            return 20;
        }
        return 0;
    }

    protected float getGravity(Item item) {
        if (item instanceof ItemBow || item instanceof ItemSplashPotion || item instanceof ItemLingeringPotion || item instanceof ItemExpBottle) {
            return 0.05f;
        }
        return 0.03f;
    }

    protected List<Entity> getEntitiesWithinAABB(AxisAlignedBB bb) {
        final ArrayList<Entity> list = new ArrayList<>();
        final int chunkMinX = (int) Math.floor((bb.minX - 2.0) / 16.0);
        final int chunkMaxX = (int) Math.floor((bb.maxX + 2.0) / 16.0);
        final int chunkMinZ = (int) Math.floor((bb.minZ - 2.0) / 16.0);
        final int chunkMaxZ = (int) Math.floor((bb.maxZ + 2.0) / 16.0);
        for (int x = chunkMinX; x <= chunkMaxX; ++x) {
            for (int z = chunkMinZ; z <= chunkMaxZ; ++z) {
                if (mc.world.getChunkProvider().getLoadedChunk(x, z) != null) {
                    mc.world.getChunk(x, z).getEntitiesWithinAABBForEntity(mc.player, bb, list, EntitySelectors.NOT_SPECTATING);
                }
            }
        }
        return list;
    }

    public void drawLine3D(double var1, double var2, double var3, long start) {
        if (rainbowLine.getValue()) {
            (new GSColor(ColorSetting.getRainbowColor(start), color.getValue().getAlpha())).glColor();
        }
        GL11.glVertex3d(var1, var2, var3);
    }
}
