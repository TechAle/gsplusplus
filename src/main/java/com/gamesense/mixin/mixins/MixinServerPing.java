package com.gamesense.mixin.mixins;

import com.gamesense.client.module.ModuleManager;
import com.gamesense.client.module.modules.misc.AntiPing;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.network.ServerPinger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPinger.class)
public class MixinServerPing {

    @Inject(method = "ping", at = @At("HEAD"), cancellable = true)
    public void pingHook(ServerData server, CallbackInfo ci) {
        if (ModuleManager.isModuleEnabled(AntiPing.class))
            ci.cancel();
    }

    @Inject(method = "tryCompatibilityPing", at = @At("HEAD"), cancellable = true)
    public void tryCompatibilityPingHook(ServerData server, CallbackInfo ci) {
        if (ModuleManager.isModuleEnabled(AntiPing.class))
            ci.cancel();
    }

}
