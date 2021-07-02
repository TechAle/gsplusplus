package com.gamesense.mixin.mixins.accessor;

import net.minecraft.entity.EntityAgeable;
import net.minecraft.network.datasync.DataParameter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityAgeable.class)
public interface IEntityAgeable {

    @Accessor("BABY")
    static DataParameter<Boolean> getBABY() {return null;}

}
