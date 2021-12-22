package com.gamesense.client.module.modules.render;

import com.gamesense.api.event.events.NewRenderEntityEvent;
import com.gamesense.api.setting.values.*;
import com.gamesense.api.util.player.social.SocialManager;
import com.gamesense.api.util.render.GSColor;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.*;

/**
 * @author Techale
 * @author Hoosiers
 */

@Module.Declaration(name = "Chams2", category = Category.Render)
public class Chams2 extends Module {
    final ResourceLocation RES_ITEM_GLINT = new ResourceLocation("textures/misc/enchanted_item_glint.png");
    BooleanSetting self = registerBoolean("Self", false);
    BooleanSetting crystals = registerBoolean("Crystal", false);
    BooleanSetting players = registerBoolean("Players", false);
    BooleanSetting customBlendFunc = registerBoolean("customBlendFunc", false);
    BooleanSetting playerImage = registerBoolean("playerImage", false);
    BooleanSetting playerCancel = registerBoolean("playerCancel", false);
    BooleanSetting crystalCancel = registerBoolean("crystalCancel", false);
    BooleanSetting playerTexture = registerBoolean("playerTexture", false);
    BooleanSetting crystalTexture = registerBoolean("crystalTexture", false);
    BooleanSetting playerSecondaryTexture = registerBoolean("playerSecondaryTexture", false);
    BooleanSetting crystalSecondaryTexture = registerBoolean("crystalSecondaryTexture", false);
    ColorSetting playerSecondaryTextureColor = registerColor("playerSecondaryTextureColor", new GSColor(255, 255, 255, 255), () -> true);
    ColorSetting crystalSecondaryTextureColor = registerColor("crystalSecondaryTextureColor", new GSColor(255, 255, 255, 255), () -> true);
    ColorSetting friendLine = registerColor("friendLine", new GSColor(255, 255, 255, 255), ()->true, true);
    ColorSetting playerLine = registerColor("playerLine", new GSColor(255, 255, 255, 255), ()->true, true);
    ColorSetting crystalLine1 = registerColor("crystalLine1", new GSColor(255, 255, 255, 255), ()->true, true);
    ColorSetting friendFill = registerColor("friendFill", new GSColor(255, 255, 255, 255), ()->true, true);
    ColorSetting playerFill = registerColor("playerFill", new GSColor(255, 255, 255, 255), ()->true, true);
    ColorSetting crystalFill1 = registerColor("crystalFill1", new GSColor(255, 255, 255, 255), ()->true, true);
    BooleanSetting playerGlint = registerBoolean("playerGlint", false);
    BooleanSetting crystalGlint = registerBoolean("crystalGlint", false);
    ColorSetting crystalGlint1 = registerColor("crystalGlint1", new GSColor(255, 255, 255, 255), ()->true, true);
    ColorSetting playerGlintColor = registerColor("playerGlintColor", new GSColor(255, 255, 255, 255), ()->true, true);
    ColorSetting friendGlintColor = registerColor("friendGlintColor", new GSColor(255, 255, 255, 255), ()->true, true);
    DoubleSetting crystalRotateSpeed = registerDouble("crystalRotateSpeed", 1, 0, 2);
    DoubleSetting crystalScale = registerDouble("crystalScale", 1, 0, 2);
    DoubleSetting lineWidth = registerDouble("lineWidth", 1, 0, 2);
    DoubleSetting lineWidthInterp = registerDouble("lineWidthInterp", 1, 0, 2);



    boolean cancel = false;
    Action currentAction = Action.NONE;

    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<NewRenderEntityEvent> renderEntityHeadEventListener = new Listener<>(event -> {
        boolean texture;
        Color secondaryTextureColor;
        if (mc.player == null || mc.world == null || event.entityIn == null) {
            return;
        }

        if (event.entityIn instanceof EntityPlayer) {
            if (event.entityIn == mc.player && !this.self.getValue())
                return;
            else if (!this.players.getValue())
                return;
        } else if (event.entityIn instanceof EntityEnderCrystal && !this.crystals.getValue()) {
            return;
        } else return;


        prepare();
        GL11.glPushAttrib(1048575);
        if (this.customBlendFunc.getValue()) {
            GL11.glBlendFunc(770, 32772);
        }
        GL11.glEnable(2881);
        GL11.glEnable(2848);
        boolean image = !(event.entityIn instanceof EntityEnderCrystal) && this.playerImage.getValue();
        boolean cancelRender = event.entityIn instanceof EntityLivingBase ? this.playerCancel.getValue() : this.crystalCancel.getValue();
        boolean texture2d = event.entityIn instanceof EntityLivingBase ? this.playerTexture.getValue() : this.crystalTexture.getValue();
        boolean secondaryTexture = event.entityIn instanceof EntityLivingBase ? this.playerSecondaryTexture.getValue() : this.crystalSecondaryTexture.getValue();
        Color color = secondaryTextureColor = event.entityIn instanceof EntityLivingBase ? this.playerSecondaryTextureColor.getValue() : this.crystalSecondaryTextureColor.getValue();
        Color line = event.entityIn instanceof EntityLivingBase ? (SocialManager.isFriend(event.entityIn.getName()) ? this.friendLine.getValue() : this.playerLine.getValue()) : (line = this.crystalLine1.getValue());
        Color fill = event.entityIn instanceof EntityLivingBase ? (SocialManager.isFriend(event.entityIn.getName()) ? this.friendFill.getValue() : this.playerFill.getValue()) : this.crystalFill1.getValue();
        boolean bl = texture = event.entityIn instanceof EntityLivingBase ? this.playerGlint.getValue() : this.crystalGlint.getValue();
        Color textureColor = event.entityIn instanceof EntityLivingBase ? (SocialManager.isFriend(event.entityIn.getName()) ? this.friendGlintColor.getValue() : this.playerGlintColor.getValue()) : this.crystalGlint1.getValue();
        float limbSwingAmt = event.entityIn instanceof EntityEnderCrystal ? event.limbSwingAmount * this.crystalRotateSpeed.getValue().floatValue() : event.limbSwingAmount;
        float scale = event.entityIn instanceof EntityEnderCrystal ? this.crystalScale.getValue().floatValue() : event.scale;
        GlStateManager.glLineWidth(getInterpolatedLinWid(mc.player.getDistance(event.entityIn) + 1.0f, this.lineWidth.getValue().floatValue(), this.lineWidthInterp.getValue().floatValue()));
        if (!image) {
            GlStateManager.disableAlpha();
            glColor(fill);
            if (texture2d) {
                GL11.glEnable(3553);
            } else {
                GL11.glDisable(3553);
            }
            this.currentAction = Action.FILL;
            event.modelBase.render(event.entityIn, event.limbSwing, limbSwingAmt, event.ageInTicks, event.netHeadYaw, event.headPitch, event.scale);
            GL11.glDisable(3553);
            if (secondaryTexture) {
                this.currentAction = Action.NONE;
                glColor(secondaryTextureColor);
                event.modelBase.render(event.entityIn, event.limbSwing, limbSwingAmt, event.ageInTicks, event.netHeadYaw, event.headPitch, event.scale);
            }
            GL11.glPolygonMode(1032, 6913);
            this.currentAction = Action.LINE;
            glColor(line);
            event.modelBase.render(event.entityIn, event.limbSwing, limbSwingAmt, event.ageInTicks, event.netHeadYaw, event.headPitch, event.scale);
            this.currentAction = Action.GLINT;
            GL11.glPolygonMode(1032, 6914);
            if (texture) {
                mc.getTextureManager().bindTexture(RES_ITEM_GLINT);
                GL11.glEnable(3553);
                GL11.glBlendFunc(768, 771);
                glColor(textureColor);
                event.modelBase.render(event.entityIn, event.limbSwing, limbSwingAmt, event.ageInTicks, event.netHeadYaw, event.headPitch, event.scale);
                if (this.customBlendFunc.getValue()) {
                    GL11.glBlendFunc(770, 32772);
                } else {
                    GL11.glBlendFunc(770, 771);
                }
            }
            if (event.entityIn instanceof EntityLivingBase) {
                // empty if block
            }
            event.limbSwingAmount = limbSwingAmt;
            this.currentAction = Action.NONE;
        }
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.popAttrib();
        release();
        if (cancelRender) {
            event.cancel();
        }
    });




    void glColor(Color color) {
        GL11.glColor4f((float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, (float)color.getAlpha() / 255.0f);
    }

    enum Action {
        FILL,
        LINE,
        GLINT,
        NONE;

    }

    void prepare() {
        GlStateManager.pushMatrix();
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        GlStateManager.depthMask((boolean)false);
        GlStateManager.disableAlpha();
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GL11.glDisable(3553);
        GL11.glEnable(2848);
        GL11.glBlendFunc(770, 771);
    }

    void release() {
        GlStateManager.depthMask((boolean)true);
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        GlStateManager.enableAlpha();
        GlStateManager.popMatrix();
        GL11.glEnable(3553);
        GL11.glPolygonMode(1032, 6914);
    }

    float getInterpolatedLinWid(float distance, float line, float lineFactor) {
        return line * lineFactor / distance;
    }
}