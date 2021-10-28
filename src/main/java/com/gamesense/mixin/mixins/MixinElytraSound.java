package com.gamesense.mixin.mixins;

import com.gamesense.client.module.ModuleManager;
import com.gamesense.client.module.modules.movement.ElytraFly;
import net.minecraft.client.audio.ElytraSound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ElytraSound.class)
public class MixinElytraSound {

        @Inject(method = "update", at = @At("HEAD"), cancellable = true)
        public void update(CallbackInfo ci) {
            if (ModuleManager.getModule(ElytraFly.class).isEnabled() && !ModuleManager.getModule(ElytraFly.class).sound.getValue()) {
                ci.cancel();
            }
        }

}
