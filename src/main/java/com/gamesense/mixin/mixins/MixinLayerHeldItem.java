package com.gamesense.mixin.mixins;

import com.gamesense.api.event.events.RenderEntityEvent;
import com.gamesense.client.GameSense;
import com.gamesense.client.module.ModuleManager;
import com.gamesense.client.module.modules.render.NoRender;
import com.gamesense.client.module.modules.render.Shaders;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.util.text.TextFormatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(LayerHeldItem.class)
public abstract class MixinLayerHeldItem {

    Shaders t = null;
    @Inject(method = "doRenderLayer", at = @At("HEAD"), cancellable = true)
    public void a(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, CallbackInfo ci) {
        if (t == null) {
            t = ModuleManager.getModule(Shaders.class);
            return;
        }
        if (!t.renderCape)
            ci.cancel();
    }
}