package com.gamesense.mixin.mixins;

import com.gamesense.api.event.events.TransformSideFirstPersonEvent;
import com.gamesense.client.GameSense;
import com.gamesense.client.module.ModuleManager;
import com.gamesense.client.module.modules.render.HandThing;
import com.gamesense.client.module.modules.render.NoRender;
import com.gamesense.client.module.modules.render.ViewModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
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

    @Shadow
    protected abstract void renderArmFirstPerson(float p_187456_1_, float p_187456_2_, EnumHandSide p_187456_3_);

    @Shadow
    protected abstract void renderMapFirstPerson(float p_187463_1_, float p_187463_2_, float p_187463_3_);

    @Shadow
    protected abstract void renderMapFirstPersonSide(float p_187465_1_, EnumHandSide hand, float p_187465_3_, ItemStack stack);

    @Shadow
    protected abstract void transformSideFirstPerson(EnumHandSide hand, float p_187459_2_);

    @Shadow
    protected abstract void transformEatFirstPerson(float p_187454_1_, EnumHandSide hand, ItemStack stack);

    @Shadow
    protected abstract void transformFirstPerson(EnumHandSide hand, float p_187453_2_);

    @Shadow
    public abstract void renderItemSide(EntityLivingBase entitylivingbaseIn, ItemStack heldStack, ItemCameraTransforms.TransformType transform, boolean leftHanded);

    @Shadow
    public ItemStack itemStackOffHand;

    @Shadow @Final public Minecraft mc;



    @Inject(method = "renderItemInFirstPerson(Lnet/minecraft/client/entity/AbstractClientPlayer;FFLnet/minecraft/util/EnumHand;FLnet/minecraft/item/ItemStack;F)V", at = @At("HEAD"), cancellable = true)
    public void prova(AbstractClientPlayer player, float p_187457_2_, float p_187457_3_, EnumHand hand, float p_187457_5_, ItemStack stack, float p_187457_7_, CallbackInfo ci) {

        ViewModel viewModel = ModuleManager.getModule(ViewModel.class);
        if (viewModel.isEnabled()) {

            boolean flag = hand == EnumHand.MAIN_HAND;
            EnumHandSide enumhandside = flag ? player.getPrimaryHand() : player.getPrimaryHand().opposite();
            GlStateManager.pushMatrix();
            boolean popAfter = true;

            if (stack.isEmpty())
            {
                if (flag && !player.isInvisible())
                {
                    this.renderArmFirstPerson(p_187457_7_, p_187457_5_, enumhandside);
                }
            }
            else if (stack.getItem() instanceof net.minecraft.item.ItemMap)
            {
                if (flag && this.itemStackOffHand.isEmpty())
                {
                    this.renderMapFirstPerson(p_187457_3_, p_187457_7_, p_187457_5_);
                }
                else
                {
                    this.renderMapFirstPersonSide(p_187457_7_, enumhandside, p_187457_5_, stack);
                }
            }
            else
            {
                boolean flag1 = enumhandside == EnumHandSide.RIGHT;

                if (player.isHandActive() && player.getItemInUseCount() > 0 && player.getActiveHand() == hand)
                {
                    int j = flag1 ? 1 : -1;

                    switch (stack.getItemUseAction())
                    {
                        case NONE:
                            this.transformSideFirstPerson(enumhandside, p_187457_7_);
                            break;
                        case EAT:
                        case DRINK:
                            this.transformEatFirstPerson(p_187457_2_, enumhandside, stack);
                            this.transformSideFirstPerson(enumhandside, p_187457_7_);
                            break;
                        case BLOCK:
                            this.transformSideFirstPerson(enumhandside, p_187457_7_);
                            break;
                        case BOW:
                            this.transformSideFirstPerson(enumhandside, p_187457_7_);
                            // Standard bow animation setting
                            /*
                            GlStateManager.translate((float)j * -0.2785682F, 0.18344387F, 0.15731531F);
                            GlStateManager.rotate(-13.935F, 1.0F, 0.0F, 0.0F);
                            GlStateManager.rotate((float)j * 35.3F, 0.0F, 1.0F, 0.0F);
                            GlStateManager.rotate((float)j * -9.785F, 0.0F, 0.0F, 1.0F);
                            float f5 = (float)stack.getMaxItemUseDuration() - ((float)this.mc.player.getItemInUseCount() - p_187457_2_ + 1.0F);
                            float f6 = f5 / 20.0F;
                            f6 = (f6 * f6 + f6 * 2.0F) / 3.0F;

                            if (f6 > 1.0F)
                            {
                                f6 = 1.0F;
                            }

                            if (f6 > 0.1F)
                            {
                                float f7 = MathHelper.sin((f5 - 0.1F) * 1.3F);
                                float f3 = f6 - 0.1F;
                                float f4 = f7 * f3;
                                GlStateManager.translate(f4 * 0.0F, f4 * 0.004F, f4 * 0.0F);
                            }

                            GlStateManager.translate(f6 * 0.0F, f6 * 0.0F, f6 * 0.04F);
                            GlStateManager.scale(1.0F, 1.0F, 1.0F + f6 * 0.2F);
                            GlStateManager.rotate((float)j * 45.0F, 0.0F, -1.0F, 0.0F);*/
                    }


                }
                else
                {

                    TransformSideFirstPersonEvent event = new TransformSideFirstPersonEvent(enumhandside);
                    GameSense.EVENT_BUS.post(event);
                    popAfter = false;
                    float f = -0.4F * MathHelper.sin(MathHelper.sqrt(p_187457_5_) * (float)Math.PI);
                    float f1 = 0.2F * MathHelper.sin(MathHelper.sqrt(p_187457_5_) * ((float)Math.PI * 2F));
                    float f2 = -0.2F * MathHelper.sin(p_187457_5_ * (float)Math.PI);
                    int i = flag1 ? 1 : -1;
                    GlStateManager.translate((float)i * f, f1, f2);
                    this.transformSideFirstPerson(enumhandside, p_187457_7_);
                    this.transformFirstPerson(enumhandside, p_187457_5_);
                }

                this.renderItemSide(player, stack, flag1 ? ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND : ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND, !flag1);
            }

            if (popAfter)
                GlStateManager.popMatrix();


            ci.cancel();
        }
    }



    /*
    @Inject(method = "transformSideFirstPerson", at = @At("HEAD"))
    public void transformSideFirstPerson(EnumHandSide hand, float p_187459_2_, CallbackInfo callbackInfo) {
        TransformSideFirstPersonEvent event = new TransformSideFirstPersonEvent(hand);
        GameSense.EVENT_BUS.post(event);
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
        if (!viewModel.isEnabled() || mc.player.getHeldItemMainhand().isEmpty())
            GlStateManager.popMatrix();
    }*/
    /*
    @Redirect(method = "renderItemSide",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;popMatrix()V"))
    public void removePopLeft() {
        ViewModel viewModel = ModuleManager.getModule(ViewModel.class);
        if (!viewModel.isEnabled() || mc.player.getHeldItemOffhand().isEmpty())
            GlStateManager.popMatrix();
    }*/

    @Inject(method = "renderOverlays", at = @At("HEAD"), cancellable = true)
    public void renderOverlays(float partialTicks, CallbackInfo callbackInfo) {
        NoRender noRender = ModuleManager.getModule(NoRender.class);

        if (noRender.isEnabled() && noRender.noOverlay.getValue()) {
            callbackInfo.cancel();
        }
    }

    /*
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
    }*/

}