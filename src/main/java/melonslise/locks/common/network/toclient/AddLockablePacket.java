package melonslise.locks.common.network.toclient;

import melonslise.locks.Locks;
import melonslise.locks.common.capability.LockableHandler;
import melonslise.locks.common.util.Lockable;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record AddLockablePacket(Lockable lockable) implements CustomPacketPayload
{
	public static final CustomPacketPayload.Type<AddLockablePacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Locks.ID, "add_lockable"));

	public static final StreamCodec<RegistryFriendlyByteBuf, AddLockablePacket> STREAM_CODEC = StreamCodec.composite(Lockable.STREAM_CODEC, AddLockablePacket::lockable, AddLockablePacket::new);

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type()
	{
		return TYPE;
	}

	// Runs on the main (client) thread. Only the player/level from the context is used, so no client-only classes are referenced here.
	public static void handle(AddLockablePacket pkt, IPayloadContext ctx)
	{
		LockableHandler.get(ctx.player().level()).add(pkt.lockable);
	}
}
