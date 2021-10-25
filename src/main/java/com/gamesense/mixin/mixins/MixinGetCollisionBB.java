package com.gamesense.mixin.mixins;

import com.gamesense.client.module.modules.movement.Avoid;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockCactus;
import net.minecraft.block.BlockFire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.gamesense.api.util.world.BlockUtil.getBlock;

/**
 * @author ToxicAven
 *  */

// from lambda client

@Mixin({Block.class, BlockAir.class, BlockFire.class, BlockCactus.class})
public class MixinGetCollisionBB {

    Minecraft mc = Minecraft.getMinecraft();

    @Inject(method = "getCollisionBoundingBox", at = @At("HEAD"), cancellable = true)
    private void getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos, CallbackInfoReturnable<AxisAlignedBB> cir) {
        if (mc.world != null && Avoid.INSTANCE.isEnabled()) {
            Block checkBlock = getBlock(pos);
            if (checkBlock.equals(Blocks.FIRE) && Avoid.INSTANCE.fire.getValue() && !Avoid.INSTANCE.bigFire.getValue() ||
                    checkBlock.equals(Blocks.CACTUS) && Avoid.INSTANCE.cactus.getValue() ||
                    (!mc.world.isBlockLoaded(pos, false) || pos.getY() < 0) && Avoid.INSTANCE.theVoid.getValue()) {
                cir.cancel();
                cir.setReturnValue(Block.FULL_BLOCK_AABB);
            } else if (Avoid.INSTANCE.fire.getValue() && Avoid.INSTANCE.bigFire.getValue() && Avoid.INSTANCE.isEnabled()) {

                cir.cancel();
                cir.setReturnValue(Block.FULL_BLOCK_AABB.expand(0.1,0.1,0.1));

            }
        }
    }

}
