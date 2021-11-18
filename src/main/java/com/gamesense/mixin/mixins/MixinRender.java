package com.gamesense.mixin.mixins;

import com.gamesense.api.event.events.ShaderColorEvent;
import com.gamesense.client.GameSense;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("unused")
@Mixin(Render.class)
public class MixinRender<T extends Entity> {

    @Inject(method = "getTeamColor", at = @At("HEAD"), cancellable = true)
    public void getTeamColor(T entity, CallbackInfoReturnable<Integer> info) {
        final ShaderColorEvent shaderColorEvent = new ShaderColorEvent(entity);
        GameSense.EVENT_BUS.post(shaderColorEvent);

        if (shaderColorEvent.isCancelled()) {
            info.cancel();
            info.setReturnValue(shaderColorEvent.getColor().getRGB());
        }
    }
}
