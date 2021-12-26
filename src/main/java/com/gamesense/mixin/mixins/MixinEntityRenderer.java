package com.gamesense.mixin.mixins;

import com.gamesense.api.event.events.AspectEvent;
import com.gamesense.api.event.events.RenderHand;
import com.gamesense.api.util.render.GSColor;
import com.gamesense.client.GameSense;
import com.gamesense.client.module.ModuleManager;
import com.gamesense.client.module.modules.misc.NoEntityTrace;
import com.gamesense.client.module.modules.render.Ambience;
import com.gamesense.client.module.modules.render.ItemShaders;
import com.gamesense.client.module.modules.render.NoRender;
import com.gamesense.client.module.modules.render.RenderTweaks;
import com.google.common.base.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.util.glu.Project;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.List;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {

    /*
        I wasnt able to make invoker work so, i had to do shitty things.
        It works, it's just really really bad code :P
        But ehy! It's been months that gs++ code is shit, so i find no problems on making it worst
     */
    @Inject(method = "renderHand", at = @At("HEAD"), cancellable = true)
    public void renderHandMain(float partialTicks, int pass, CallbackInfo ci) {
        ItemShaders module = ModuleManager.getModule(ItemShaders.class);
        if (module.isEnabled()) {
            Minecraft mc = Minecraft.getMinecraft();
            if (!module.cancelItem.getValue()) {
                doRenderHand(partialTicks, pass, mc);
            }

            if (!module.glowESP.getValue().equals("None") && !module.fillShader.getValue().equals("None")) {
                GlStateManager.pushMatrix();
                RenderHand.PreBoth hand = new RenderHand.PreBoth(partialTicks);
                GameSense.EVENT_BUS.post(hand);
                doRenderHand(partialTicks, pass, mc);
                RenderHand.PostBoth hand2 = new RenderHand.PostBoth(partialTicks);
                GameSense.EVENT_BUS.post(hand2);
                GlStateManager.popMatrix();
            }

            if (!module.glowESP.getValue().equals("None")) {
                GlStateManager.pushMatrix();
                RenderHand.PreOutline hand = new RenderHand.PreOutline(partialTicks);
                GameSense.EVENT_BUS.post(hand);
                doRenderHand(partialTicks, pass, mc);
                RenderHand.PostOutline hand2 = new RenderHand.PostOutline(partialTicks);
                GameSense.EVENT_BUS.post(hand2);
                GlStateManager.popMatrix();
            }

            if (!module.fillShader.getValue().equals("None")) {
                GlStateManager.pushMatrix();
                RenderHand.PreFill hand = new RenderHand.PreFill(partialTicks);
                GameSense.EVENT_BUS.post(hand);
                doRenderHand(partialTicks, pass, mc);
                RenderHand.PostFill hand2 = new RenderHand.PostFill(partialTicks);
                GameSense.EVENT_BUS.post(hand2);
                GlStateManager.popMatrix();
            }

            ci.cancel();
        }
    }

    @Shadow
    public
    boolean debugView;

    @Shadow
    public abstract float getFOVModifier(float partialTicks, boolean useFOVSetting);

    @Shadow
    public abstract void hurtCameraEffect(float partialTicks);

    @Shadow
    public abstract void applyBobbing(float partialTicks);

    @Shadow
    public abstract void enableLightmap();

    @Shadow
    public float farPlaneDistance;

    @Final
    @Shadow
    public ItemRenderer itemRenderer;

    @Shadow
    public abstract void disableLightmap();

    void doRenderHand(float partialTicks, int pass, Minecraft mc) {
        if (!this.debugView)
        {
            GlStateManager.matrixMode(5889);
            GlStateManager.loadIdentity();
            float f = 0.07F;

            if (mc.gameSettings.anaglyph)
            {
                GlStateManager.translate((float)(-(pass * 2 - 1)) * 0.07F, 0.0F, 0.0F);
            }

            Project.gluPerspective(this.getFOVModifier(partialTicks, false), (float)mc.displayWidth / (float)mc.displayHeight, 0.05F, this.farPlaneDistance * 2.0F);
            GlStateManager.matrixMode(5888);
            GlStateManager.loadIdentity();

            if (mc.gameSettings.anaglyph)
            {
                GlStateManager.translate((float)(pass * 2 - 1) * 0.1F, 0.0F, 0.0F);
            }

            GlStateManager.pushMatrix();
            this.hurtCameraEffect(partialTicks);

            if (mc.gameSettings.viewBobbing)
            {
                this.applyBobbing(partialTicks);
            }

            boolean flag = mc.getRenderViewEntity() instanceof EntityLivingBase && ((EntityLivingBase)mc.getRenderViewEntity()).isPlayerSleeping();

            if (!net.minecraftforge.client.ForgeHooksClient.renderFirstPersonHand(mc.renderGlobal, partialTicks, pass))
                if (mc.gameSettings.thirdPersonView == 0 && !flag && !mc.gameSettings.hideGUI && !mc.playerController.isSpectator())
                {
                    this.enableLightmap();
                    this.itemRenderer.renderItemInFirstPerson(partialTicks);
                    this.disableLightmap();
                }

            GlStateManager.popMatrix();

            if (mc.gameSettings.thirdPersonView == 0 && !flag)
            {
                this.itemRenderer.renderOverlays(partialTicks);
                this.hurtCameraEffect(partialTicks);
            }

            if (mc.gameSettings.viewBobbing)
            {
                this.applyBobbing(partialTicks);
            }
        }
    }


    /*
        RenderHandPre hand = new RenderHandPre(partialTicks);

        GameSense.EVENT_BUS.post(hand);
    */

    /*
        RenderHand hand = new RenderHand(partialTicks);

        GameSense.EVENT_BUS.post(hand);
    */


    @Shadow
    @Final
    private int[] lightmapColors;

    @Shadow public abstract void renderHand(float partialTicks, int pass);

    @Redirect(method = "orientCamera", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;rayTraceBlocks(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/RayTraceResult;"))
    public RayTraceResult rayTraceBlocks(WorldClient world, Vec3d start, Vec3d end) {
        RenderTweaks renderTweaks = ModuleManager.getModule(RenderTweaks.class);

        if (renderTweaks.isEnabled() && renderTweaks.viewClip.getValue()) {
            return null;
        } else {
            return world.rayTraceBlocks(start, end);
        }
    }

    @Redirect(method={"setupCameraTransform"}, at=@At(value="INVOKE", target="Lorg/lwjgl/util/glu/Project;gluPerspective(FFFF)V"))
    private void onSetupCameraTransform(float fovy, float aspect, float zNear, float zFar) {

        AspectEvent aspectEvent = new AspectEvent((float) Minecraft.getMinecraft().displayWidth / Minecraft.getMinecraft().displayHeight);

        GameSense.EVENT_BUS.post(aspectEvent);

        Project.gluPerspective(fovy, aspectEvent.getAspect(), zNear, zFar);
    }

    @Redirect(method={"renderWorldPass"}, at=@At(value="INVOKE", target="Lorg/lwjgl/util/glu/Project;gluPerspective(FFFF)V"))
    private void onRenderWorldPass(float fovy, float aspect, float zNear, float zFar) {
        AspectEvent aspectEvent = new AspectEvent((float) Minecraft.getMinecraft().displayWidth / Minecraft.getMinecraft().displayHeight);

        GameSense.EVENT_BUS.post(aspectEvent);

        Project.gluPerspective(fovy, aspectEvent.getAspect(), zNear, zFar);
    }

    @Redirect(method={"renderCloudsCheck"}, at=@At(value="INVOKE", target="Lorg/lwjgl/util/glu/Project;gluPerspective(FFFF)V"))
    private void onRenderCloudsCheck(float fovy, float aspect, float zNear, float zFar) {
        AspectEvent aspectEvent = new AspectEvent((float) Minecraft.getMinecraft().displayWidth / Minecraft.getMinecraft().displayHeight);

        GameSense.EVENT_BUS.post(aspectEvent);

        Project.gluPerspective(fovy, aspectEvent.getAspect(), zNear, zFar);
    }

    @Redirect(method = "getMouseOver", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;getEntitiesInAABBexcluding(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;Lcom/google/common/base/Predicate;)Ljava/util/List;"))
    public List<Entity> getEntitiesInAABBexcluding(WorldClient worldClient, Entity entityIn, AxisAlignedBB boundingBox, Predicate<? super Entity> predicate) {
        if (ModuleManager.getModule(NoEntityTrace.class).noTrace()) {
            return new ArrayList<>();
        } else {
            return worldClient.getEntitiesInAABBexcluding(entityIn, boundingBox, predicate);
        }
    }

    @Inject(method = "hurtCameraEffect", at = @At("HEAD"), cancellable = true)
    public void hurtCameraEffect(float ticks, CallbackInfo callbackInfo) {
        NoRender noRender = ModuleManager.getModule(NoRender.class);

        if (noRender.isEnabled() && noRender.hurtCam.getValue()) {
            callbackInfo.cancel();
        }
    }


    @Inject( method = "updateLightmap", at = @At( value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/DynamicTexture;updateDynamicTexture()V", shift = At.Shift.BEFORE ) )
    private void updateTextureHook(float partialTicks, CallbackInfo ci) {
        Ambience ambience = ModuleManager.getModule(Ambience.class);
        if (ambience.isEnabled()) {
            for (int i = 0; i < this.lightmapColors.length; ++i) {
                GSColor ambientColor = ambience.colorLight.getValue();
                int alpha = ambientColor.getAlpha();
                float modifier = ( float ) alpha / 255.0f;
                int color = this.lightmapColors[ i ];
                int[] bgr = toRGBAArray(color);
                Vector3f values = new Vector3f(( float ) bgr[ 2 ] / 255.0f, ( float ) bgr[ 1 ] / 255.0f, ( float ) bgr[ 0 ] / 255.0f);
                Vector3f newValues = new Vector3f(( float ) ambientColor.getRed() / 255.0f, ( float ) ambientColor.getGreen() / 255.0f, ( float ) ambientColor.getBlue() / 255.0f);
                Vector3f finalValues = mix(values, newValues, modifier);
                int red = ( int ) (finalValues.x * 255.0f);
                int green = ( int ) (finalValues.y * 255.0f);
                int blue = ( int ) (finalValues.z * 255.0f);
                this.lightmapColors[ i ] = 0xFF000000 | red << 16 | green << 8 | blue;
            }
        }
    }

    private int[] toRGBAArray(int colorBuffer) {
        return new int[] { colorBuffer >> 16 & 0xFF, colorBuffer >> 8 & 0xFF, colorBuffer & 0xFF };
    }

    private Vector3f mix(Vector3f first, Vector3f second, float factor) {
        return new Vector3f(first.x * (1.0f - factor) + second.x * factor, first.y * (1.0f - factor) + second.y * factor, first.z * (1.0f - factor) + first.z * factor);
    }
}