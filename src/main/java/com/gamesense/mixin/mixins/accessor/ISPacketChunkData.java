package com.gamesense.mixin.mixins.accessor;

import net.minecraft.network.play.server.SPacketChunkData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SPacketChunkData.class)
public interface ISPacketChunkData {

    @Accessor("buffer")
    byte[] getBuffer();

}
