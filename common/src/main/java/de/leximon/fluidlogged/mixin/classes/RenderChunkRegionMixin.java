package de.leximon.fluidlogged.mixin.classes;

import de.leximon.fluidlogged.mixin.extensions.RenderChunkExtension;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(RenderChunkRegion.class)
public abstract class RenderChunkRegionMixin {
    
    @Shadow protected abstract RenderChunk getChunk(int x, int z);
    
    /**
     * @author Leximon (Fluidlogged)
     * @reason get chunk specific fluid state
     */
    @Overwrite
    public FluidState getFluidState(BlockPos blockPos) {
        int x = SectionPos.blockToSectionCoord(blockPos.getX());
        int z = SectionPos.blockToSectionCoord(blockPos.getZ());
        return ((RenderChunkExtension) getChunk(x, z)).getFluidState(blockPos);
    }

}
