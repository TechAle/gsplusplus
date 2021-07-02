package com.gamesense.mixin.mixins.accessor;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.datasync.DataParameter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityLivingBase.class)
public interface IEntityLivingBase {

    @Accessor("HEALTH")
    static DataParameter<Float> getHEALTH() {
        return null;
    }

}
