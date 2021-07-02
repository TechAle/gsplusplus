package com.gamesense.mixin.mixins.accessor;

import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.network.datasync.DataParameter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityZombie.class)
public interface IEntityZombie {

    @Accessor("IS_CHILD")
    static DataParameter<Boolean> getIS_CHILD() {
        return null;
    }

}
