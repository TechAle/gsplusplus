package com.gamesense.mixin.mixins;

import com.gamesense.api.event.events.EntityCollisionEvent;
import com.gamesense.api.event.events.StepEvent;
import com.gamesense.client.GameSense;
import com.gamesense.client.module.ModuleManager;
import com.gamesense.client.module.modules.movement.SafeWalk;
import com.gamesense.client.module.modules.movement.Scaffold;
import com.gamesense.client.module.modules.movement.Step;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class MixinEntity {

    @Inject(method = "applyEntityCollision", at = @At("HEAD"), cancellable = true)
    public void velocity(Entity entityIn, CallbackInfo ci) {
        EntityCollisionEvent event = new EntityCollisionEvent();
        GameSense.EVENT_BUS.post(event);

        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Redirect(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isSneaking()Z"))
    public boolean isSneaking(Entity entity) {
        return (ModuleManager.isModuleEnabled(Scaffold.class)) && !Minecraft.getMinecraft().gameSettings.keyBindSprint.isKeyDown()|| ModuleManager.isModuleEnabled(SafeWalk.class) || entity.isSneaking();
    }

    @Shadow public float stepHeight;
    @Inject(method = "move", at = @At(value = "HEAD"))
    public void prova(MoverType type, double x, double y, double z, CallbackInfo ci) {
        StepEvent bleach = new StepEvent(y);
        GameSense.EVENT_BUS.post(bleach);
    }
}