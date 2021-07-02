package com.gamesense.mixin.mixins.accessor;

import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.network.datasync.DataParameter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntitySlime.class)
public interface IEntitySlime {

    @Accessor("SLIME_SIZE")
    static DataParameter<Integer> getSLIME_SIZE() {
        return null;
    }

}
