package de.leximon.fluidlogged.network.fabric;

import de.leximon.fluidlogged.FluidloggedCommon;
import de.leximon.fluidlogged.network.ClientPacketHandler;
import it.unimi.dsi.fastutil.shorts.ShortIterator;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

public class ClientboundSectionFluidsUpdatePacket implements FabricPacket {

    public static final ResourceLocation ID = FluidloggedCommon.id("section_fluids_update");
    public static final PacketType<ClientboundSectionFluidsUpdatePacket> PACKET_TYPE = PacketType.create(ID, ClientboundSectionFluidsUpdatePacket::read);

    private final SectionPos sectionPos;
    private final short[] positions;
    private final FluidState[] states;

    private ClientboundSectionFluidsUpdatePacket(SectionPos sectionPos, short[] positions, FluidState[] states) {
        this.sectionPos = sectionPos;
        this.positions = positions;
        this.states = states;
    }

    public ClientboundSectionFluidsUpdatePacket(SectionPos sectionPos, ShortSet shortSet, LevelChunkSection levelChunkSection) {
        int length = shortSet.size();
        this.sectionPos = sectionPos;
        this.positions = new short[length];
        this.states = new FluidState[length];

        int index = 0;
        for(short pos : shortSet) {
            this.positions[index] = pos;
            this.states[index] = levelChunkSection.getFluidState(
                    SectionPos.sectionRelativeX(pos),
                    SectionPos.sectionRelativeY(pos),
                    SectionPos.sectionRelativeZ(pos)
            );
        }
    }

    @Override
    public PacketType<?> getType() {
        return PACKET_TYPE;
    }

    public void apply(LocalPlayer localPlayer, PacketSender packetSender) {
        ClientPacketHandler.handleSectionFluidsUpdate(sectionPos, positions, states);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeLong(this.sectionPos.asLong());
        buf.writeVarInt(this.positions.length);

        for(int i = 0; i < this.positions.length; i++)
            buf.writeVarLong((long) FluidloggedCommon.getFluidId(this.states[i]) << 12 | (long) this.positions[i]);
    }

    public static ClientboundSectionFluidsUpdatePacket read(FriendlyByteBuf buf) {
        SectionPos sectionPos = SectionPos.of(buf.readLong());
        int i = buf.readVarInt();
        short[] positions = new short[i];
        FluidState[] states = new FluidState[i];

        for(int j = 0; j < i; ++j) {
            long idAndPos = buf.readVarLong();
            positions[j] = (short)((int) (idAndPos & 0xFFF));
            states[j] = Fluid.FLUID_STATE_REGISTRY.byId((int) (idAndPos >>> 12));
        }

        return new ClientboundSectionFluidsUpdatePacket(sectionPos, positions, states);
    }
}
