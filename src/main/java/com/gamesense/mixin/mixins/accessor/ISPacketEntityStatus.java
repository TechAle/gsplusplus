package com.gamesense.mixin.mixins.accessor;

import net.minecraft.network.play.server.SPacketEntityStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SPacketEntityStatus.class)
public interface ISPacketEntityStatus {

    @Accessor("entityId")
    int getEntityID();

}
