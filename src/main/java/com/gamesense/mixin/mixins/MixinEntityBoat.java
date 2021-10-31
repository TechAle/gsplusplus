package com.gamesense.mixin.mixins;

import com.gamesense.api.event.events.BoatMoveEvent;
import com.gamesense.client.GameSense;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.EntityBoat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityBoat.class)
public abstract class MixinEntityBoat {

    @Redirect(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/item/EntityBoat;move(Lnet/minecraft/entity/MoverType;DDD)V")) // TY AresClient
    public void onMove(EntityBoat entityBoat, MoverType type, double x, double y, double z) {
        BoatMoveEvent boatMoveEvent = new BoatMoveEvent(type,x,y,z);
        GameSense.EVENT_BUS.post(boatMoveEvent);
        if(!boatMoveEvent.isCancelled()) entityBoat.move(type, x, y, z);
    }

}