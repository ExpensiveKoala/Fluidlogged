package de.leximon.fluidlogged.network.forge;

import de.leximon.fluidlogged.network.ClientPacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ClientboundFluidUpdatePacket(
        BlockPos pos,
        FluidState state
) {

    public void apply(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.handleFluidUpdate(pos, state)));
        context.get().setPacketHandled(true);
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeId(Fluid.FLUID_STATE_REGISTRY, state);
    }

    public static ClientboundFluidUpdatePacket read(FriendlyByteBuf buf) {
        return new ClientboundFluidUpdatePacket(
                buf.readBlockPos(),
                buf.readById(Fluid.FLUID_STATE_REGISTRY)
        );
    }
}