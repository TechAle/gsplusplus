package com.gamesense.mixin.mixins;

import com.gamesense.client.module.ModuleManager;
import com.gamesense.client.module.modules.render.RainbowEnchant;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LayerArmorBase.class)
public abstract class MixinLayerArmorBase {
    @Redirect(method = { "renderEnchantedGlint" }, at = @At(value = "INVOKE", target = "net/minecraft/client/renderer/GlStateManager.color(FFFF)V"))
    private static void renderEnchantedGlint(float a2, float a3, float a4, float v1) {
        RainbowEnchant rainbowEnchant = ModuleManager.getModule(RainbowEnchant.class);
        if (rainbowEnchant.isEnabled()) {
                a2 = rainbowEnchant.color.getColor().getRed() /255f;
                a4 = rainbowEnchant.color.getColor().getGreen() / 255f;
                a3 = rainbowEnchant.color.getColor().getBlue() / 255f;
                v1 = rainbowEnchant.color.getColor().getAlpha() / 255f;
            }
        GlStateManager.color(a2, a4, a3, v1);
    }
}