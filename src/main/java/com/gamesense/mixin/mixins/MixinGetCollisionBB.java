package com.gamesense.mixin.mixins;

import com.gamesense.api.event.events.BoundingBoxEvent;
import com.gamesense.client.GameSense;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockCactus;
import net.minecraft.block.BlockFire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author ToxicAven
 *  */

// from lambda client

@Mixin(Block.class)
public class MixinGetCollisionBB {

    Minecraft mc = Minecraft.getMinecraft();

    @Inject(method = "getCollisionBoundingBox", at = @At("HEAD"), cancellable = true)
    private void getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos, CallbackInfoReturnable<AxisAlignedBB> cir) {

        BoundingBoxEvent event = new BoundingBoxEvent(blockState.getBlock(), new Vec3d(pos));
        GameSense.EVENT_BUS.post(event);

        if (event.changed) {
            cir.cancel();
            cir.setReturnValue(event.getbb());
        }

    }

}
