package com.gamesense.mixin.mixins.accessor;

import net.minecraft.network.play.server.SPacketUseBed;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SPacketUseBed.class)
public interface ISPacketUseBed {

    @Accessor("playerID")
    int getPlayerID();

}
