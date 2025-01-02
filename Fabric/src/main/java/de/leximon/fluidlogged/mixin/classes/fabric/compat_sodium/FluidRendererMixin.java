package de.leximon.fluidlogged.mixin.classes.fabric.compat_sodium;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net/caffeinemc/mods/sodium/client/render/chunk/compile/pipeline/DefaultFluidRenderer")
public class FluidRendererMixin {

    @Redirect(
            method = "fluidHeight",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;getFluidState()Lnet/minecraft/world/level/material/FluidState;"
            )
    )
    private FluidState redirectFluidHeight(BlockState blockState, BlockAndTintGetter world, Fluid fluid, BlockPos blockPos, Direction direction) {
        return world.getFluidState(blockPos);
    }

    @Inject(method = "isFluidOccluded", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/world/level/BlockAndTintGetter;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"), cancellable = true)
    private void isFluidOccluded(BlockAndTintGetter world, int x, int y, int z, Direction dir, BlockState blockState, Fluid fluid, CallbackInfoReturnable<Boolean> cir, @Local(ordinal = 1) BlockPos.MutableBlockPos adjPos) {
        if (world.getFluidState(adjPos).getType().isSame(fluid)) {
           cir.setReturnValue(true);
        }
    }
}
