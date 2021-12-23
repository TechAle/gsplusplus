package com.gamesense.mixin.mixins;

import com.gamesense.api.util.render.CapeUtil;
import com.gamesense.client.module.ModuleManager;
import com.gamesense.client.module.modules.render.CapesModule;
import com.gamesense.client.module.modules.render.Shaders;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.UUID;

@Mixin(AbstractClientPlayer.class)
public abstract class MixinAbstractClientPlayer {

    @Shadow
    @Nullable
    protected abstract NetworkPlayerInfo getPlayerInfo();

    private String me = null;

    @Inject(method = "getLocationCape", at = @At("HEAD"), cancellable = true)
    public void getLocationCape(CallbackInfoReturnable<ResourceLocation> callbackInfoReturnable) {

        if (!ModuleManager.getModule(Shaders.class).renderCape) {
            callbackInfoReturnable.cancel();
            return;
        }

        UUID uuid = getPlayerInfo().getGameProfile().getId();
        CapesModule capesModule = ModuleManager.getModule(CapesModule.class);

        if (CapeUtil.hasCape(uuid)) {

            if (me == null) {
                me = CapesModule.getUsName();
            }

            if (getPlayerInfo().getGameProfile().getName().equals(me) && !capesModule.isEnabled())
                return;

            if (capesModule.capeMode.getValue().equalsIgnoreCase("Old")) {
                callbackInfoReturnable.setReturnValue(CapeUtil.capes.get(0));
            } else if (capesModule.capeMode.getValue().equalsIgnoreCase("New")) {
                callbackInfoReturnable.setReturnValue(CapeUtil.capes.get(1));
            } else {
                callbackInfoReturnable.setReturnValue(CapeUtil.capes.get(2));
            }
        }
    }
}