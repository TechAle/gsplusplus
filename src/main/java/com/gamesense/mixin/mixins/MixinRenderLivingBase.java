package com.gamesense.mixin.mixins;

import com.gamesense.api.event.events.NewRenderEntityEvent;
import com.gamesense.client.GameSense;
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

    @Inject(method="renderModel", at=@At(value="HEAD"), cancellable=true)
    void doRender(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, CallbackInfo ci) {
        NewRenderEntityEvent event = new NewRenderEntityEvent(this.mainModel, entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
        if (!this.bindEntityTexture((T) entityIn)) {
            return;
        }
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
}