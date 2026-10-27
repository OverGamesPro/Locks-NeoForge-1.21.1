package melonslise.locks.common.network.toclient;

import melonslise.locks.Locks;
import melonslise.locks.common.capability.LockableHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RemoveLockablePacket(int id) implements CustomPacketPayload
{
	public static final CustomPacketPayload.Type<RemoveLockablePacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Locks.ID, "remove_lockable"));

	public static final StreamCodec<RegistryFriendlyByteBuf, RemoveLockablePacket> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.INT, RemoveLockablePacket::id, RemoveLockablePacket::new);

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type()
	{
		return TYPE;
	}

	public static void handle(RemoveLockablePacket pkt, IPayloadContext ctx)
	{
		LockableHandler.get(ctx.player().level()).remove(pkt.id);
	}
}
