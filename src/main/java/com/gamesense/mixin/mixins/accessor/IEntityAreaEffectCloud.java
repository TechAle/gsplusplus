package com.gamesense.mixin.mixins.accessor;

import net.minecraft.entity.EntityAreaEffectCloud;
import net.minecraft.network.datasync.DataParameter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityAreaEffectCloud.class)
public interface IEntityAreaEffectCloud {

    @Accessor("RADIUS")
    static DataParameter<Float> getRADIUS() {
        return null;
    }

}
