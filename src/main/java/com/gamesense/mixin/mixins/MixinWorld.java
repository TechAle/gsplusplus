/*
    CheckLightFor from lambda (https://github.com/lambda-client/lambda/blob/master/src/main/java/com/lambda/client/mixin/client/world/MixinWorld.java)
 */
package com.gamesense.mixin.mixins;

import com.gamesense.client.module.ModuleManager;
import com.gamesense.client.module.modules.combat.PistonCrystal;
import com.gamesense.client.module.modules.render.NoRender;
import com.gamesense.client.module.modules.render.noGlitchBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public class MixinWorld {

    @Inject(method = "checkLightFor", at = @At("HEAD"), cancellable = true)
    private void updateLightmapHook(EnumSkyBlock lightType, BlockPos pos, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        NoRender noRender = ModuleManager.getModule(NoRender.class);

        if (noRender.isEnabled() && noRender.noSkylight.getValue()) {
            callbackInfoReturnable.setReturnValue(false);
        }
    }

    @Inject(method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;I)Z", at = @At("HEAD"), cancellable = true)
    private void setBlockState(BlockPos pos, IBlockState newState, int flags, CallbackInfoReturnable<Boolean> cir) {
        noGlitchBlock noGlitchBlock = ModuleManager.getModule(noGlitchBlock.class);
        if (noGlitchBlock.placeBlock.getValue() && flags != 3) {
            cir.cancel();
            cir.setReturnValue(false);
        }
    }


}