package com.gamesense.mixin.mixins;

import com.gamesense.api.event.events.NewRenderEntityEvent;
import com.gamesense.api.event.events.RenderEntityEvent;
import com.gamesense.client.GameSense;
import com.gamesense.client.module.ModuleManager;
import com.gamesense.client.module.modules.render.NoRender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

/**
 * @author linustouchtips
 * @author Hoosiers
 * @since 12/14/2020
 * @since 12/31/2020
 */

@Mixin(value=RenderLivingBase.class)
public class MixinRenderLivingBase<T extends EntityLivingBase>
        extends Render<T> {
    @Shadow
    protected ModelBase mainModel;

    public MixinRenderLivingBase(RenderManager renderManagerIn, ModelBase modelBaseIn, float shadowSizeIn) {
        super(renderManagerIn);
        this.mainModel = modelBaseIn;
        this.shadowSize = shadowSizeIn;
    }

    protected final Minecraft mc = Minecraft.getMinecraft();

    private boolean isClustered;


    @Inject(method="renderModel", at=@At(value="HEAD"), cancellable=true)
    void doRender(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, CallbackInfo ci) {
        NewRenderEntityEvent event = new NewRenderEntityEvent(this.mainModel, entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
        if (!this.bindEntityTexture((T) entityIn)) {
            return;
        }

        NoRender noRender = ModuleManager.getModule(NoRender.class);

        if (noRender.isEnabled() && noRender.noCluster.getValue() && mc.player.getDistance(entityIn) < 1 && entityIn != mc.player) {
            GlStateManager.enableBlendProfile(GlStateManager.Profile.TRANSPARENT_MODEL);
            isClustered = true;
            if (!noRender.incrementNoClusterRender()) {
                ci.cancel();
            }
        } else {
            isClustered = false;
        }

        RenderEntityEvent.Head renderEntityHeadEvent = new RenderEntityEvent.Head(entityIn, RenderEntityEvent.Type.COLOR);

        GameSense.EVENT_BUS.post(renderEntityHeadEvent);


        GlStateManager.enableBlendProfile(GlStateManager.Profile.TRANSPARENT_MODEL);
        GameSense.EVENT_BUS.post(event);
        GlStateManager.disableBlendProfile(GlStateManager.Profile.TRANSPARENT_MODEL);

        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Nullable
    protected ResourceLocation getEntityTexture(@NotNull T entity) {
        return null;
    }



    @Inject(method = "renderModel", at = @At("RETURN"), cancellable = true)
    protected void renderModelReturn(T entitylivingbaseIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, CallbackInfo callbackInfo) {
        RenderEntityEvent.Return renderEntityReturnEvent = new RenderEntityEvent.Return(entitylivingbaseIn, RenderEntityEvent.Type.COLOR);

        GameSense.EVENT_BUS.post(renderEntityReturnEvent);

        if (!renderEntityReturnEvent.isCancelled()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method = "renderLayers", at = @At("HEAD"), cancellable = true)
    protected void renderLayers(T entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scaleIn, CallbackInfo callbackInfo) {
        if (isClustered) {
            if (!ModuleManager.getModule(NoRender.class).getNoClusterRender()) {
                callbackInfo.cancel();
            }
        }
    }

    /*
      """Done like this or with 9 mixins. You choose?"""
      Never mind!!
     */
    @Redirect(method = "setBrightness", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;glTexEnvi(III)V", ordinal = 6))
    protected void glTexEnvi0(int target, int parameterName, int parameter) {
        if (!isClustered) {
            GlStateManager.glTexEnvi(target, parameterName, parameter);
        }
    }

    @Redirect(method = "setBrightness", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;glTexEnvi(III)V", ordinal = 7))
    protected void glTexEnvi1(int target, int parameterName, int parameter) {
        if (!isClustered) {
            GlStateManager.glTexEnvi(target, parameterName, parameter);
        }
    }

    @Redirect(method = "setBrightness", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;glTexEnvi(III)V", ordinal = 8))
    protected void glTexEnvi2(int target, int parameterName, int parameter) {
        if (!isClustered) {
            GlStateManager.glTexEnvi(target, parameterName, parameter);
        }
    }
}