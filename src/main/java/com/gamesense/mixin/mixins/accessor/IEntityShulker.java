package com.gamesense.mixin.mixins.accessor;

import com.google.common.base.Optional;
import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityShulker.class)
public interface IEntityShulker {

    @Accessor("ATTACHED_FACE")
    static DataParameter<EnumFacing> getATTACHED_FACE() {
        return null;
    }

    @Accessor("ATTACHED_BLOCK_POS")
    static DataParameter<Optional<BlockPos>> getATTACHED_BLOCK_POS() {
        return null;
    }

    @Accessor("PEEK_TICK")
    static DataParameter<Byte> getPEEK_TICK() {
        return null;
    }

}
