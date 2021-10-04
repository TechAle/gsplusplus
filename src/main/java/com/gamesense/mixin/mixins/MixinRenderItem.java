package com.gamesense.mixin.mixins;

import com.gamesense.client.module.ModuleManager;
import com.gamesense.client.module.modules.render.RainbowEnchant;
import net.minecraft.client.renderer.RenderItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.awt.*;

@Mixin(RenderItem.class)
public class MixinRenderItem {

    int a2;
    int a4;
    int a3;
    @ModifyArg(method = "renderEffect", at = @At(value = "INVOKE", target = "net/minecraft/client/renderer/RenderItem.renderModel(Lnet/minecraft/client/renderer/block/model/IBakedModel;I)V"), index = 1)
    private int renderEffect(int oldValue) {
        RainbowEnchant rainbowEnchant = ModuleManager.getModule(RainbowEnchant.class);
        if (rainbowEnchant.isEnabled()) {
            a2 = rainbowEnchant.color.getColor().getRed();
            a4 = rainbowEnchant.color.getColor().getGreen();
            a3 = rainbowEnchant.color.getColor().getBlue();
            return new Color(a2, a4, a3).getRGB();
        } return oldValue;
    }
}
