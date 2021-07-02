package com.gamesense.mixin.mixins.accessor;

import net.minecraft.network.play.server.SPacketMultiBlockChange;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SPacketMultiBlockChange.class)
public interface ISPacketMultiBlockChange {

    @Accessor("chunkPos")
    ChunkPos getChunkPos();

}
