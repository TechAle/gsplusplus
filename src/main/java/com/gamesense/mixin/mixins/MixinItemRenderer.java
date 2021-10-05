package com.gamesense.mixin.mixins;

import com.gamesense.api.event.events.TransformSideFirstPersonEvent;
import com.gamesense.client.GameSense;
import com.gamesense.client.module.ModuleManager;
import com.gamesense.client.module.modules.render.HandThing;
import com.gamesense.client.module.modules.render.NoRender;
import com.gamesense.client.module.modules.render.ViewModel;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Check ViewModel.class for further credits
 */

@Mixin(ItemRenderer.class)
public abstract class MixinItemRenderer {

    private boolean injection = true;

    @Shadow
    public abstract void renderItemInFirstPerson(AbstractClientPlayer var1, float var2, float var3, EnumHand var4, float var5, ItemStack var6, float var7);

    @Inject(method = "transformSideFirstPerson", at = @At("HEAD"))
    public void transformSideFirstPerson(EnumHandSide hand, float p_187459_2_, CallbackInfo callbackInfo) {
        TransformSideFirstPersonEvent event = new TransformSideFirstPersonEvent(hand);
        GameSense.EVENT_BUS.post(event);
    }

    @Inject(method = "transformEatFirstPerson", at = @At("HEAD"), cancellable = true)
    public void transformEatFirstPerson(float p_187454_1_, EnumHandSide hand, ItemStack stack, CallbackInfo callbackInfo) {
        TransformSideFirstPersonEvent event = new TransformSideFirstPersonEvent(hand);
        GameSense.EVENT_BUS.post(event);

        ViewModel viewModel = ModuleManager.getModule(ViewModel.class);

        if (viewModel.isEnabled() && viewModel.cancelEating.getValue()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method = "transformFirstPerson", at = @At("HEAD"))
    public void transformFirstPerson(EnumHandSide hand, float p_187453_2_, CallbackInfo callbackInfo) {
        TransformSideFirstPersonEvent event = new TransformSideFirstPersonEvent(hand);
        GameSense.EVENT_BUS.post(event);
    }

    @Redirect(method = "renderItemInFirstPerson(Lnet/minecraft/client/entity/AbstractClientPlayer;FFLnet/minecraft/util/EnumHand;FLnet/minecraft/item/ItemStack;F)V",
                at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;popMatrix()V"))
    public void removePopRight() {
        ViewModel viewModel = ModuleManager.getModule(ViewModel.class);
        if (!viewModel.isEnabled())
            GlStateManager.popMatrix();
    }

    @Redirect(method = "renderItemSide",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;popMatrix()V"))
    public void removePopLeft() {
        ViewModel viewModel = ModuleManager.getModule(ViewModel.class);
        if (!viewModel.isEnabled())
            GlStateManager.popMatrix();
    }

    @Inject(method = "renderOverlays", at = @At("HEAD"), cancellable = true)
    public void renderOverlays(float partialTicks, CallbackInfo callbackInfo) {
        NoRender noRender = ModuleManager.getModule(NoRender.class);

        if (noRender.isEnabled() && noRender.noOverlay.getValue()) {
            callbackInfo.cancel();
        }
    }
    @Inject(method = {"renderItemInFirstPerson(Lnet/minecraft/client/entity/AbstractClientPlayer;FFLnet/minecraft/util/EnumHand;FLnet/minecraft/item/ItemStack;F)V"}, at = {@At(value = "HEAD")}, cancellable = true)
    public void renderItemInFirstPersonHook(AbstractClientPlayer player, float p_187457_2_, float p_187457_3_, EnumHand hand, float p_187457_5_, ItemStack stack, float p_187457_7_, CallbackInfo info) {
        HandThing offset = ModuleManager.getModule(HandThing.class);
        if (this.injection && offset.isEnabled()) {
            info.cancel();
            float xOffset;
            float yOffset;
            this.injection = false;
            if (hand == EnumHand.MAIN_HAND) {
                xOffset = offset.mainX.getValue().floatValue();
                yOffset = offset.mainY.getValue().floatValue();
            } else {
                xOffset = offset.offX.getValue().floatValue();
                yOffset = offset.offY.getValue().floatValue();
            }
            if (!stack.isEmpty) {
                this.renderItemInFirstPerson(player, p_187457_2_, p_187457_3_, hand, p_187457_5_ + xOffset, stack, p_187457_7_ + yOffset);
            } else {
                this.renderItemInFirstPerson(player, p_187457_2_, p_187457_3_, hand, p_187457_5_, stack, p_187457_7_);
            }
            this.injection = true;
        }
    }

}