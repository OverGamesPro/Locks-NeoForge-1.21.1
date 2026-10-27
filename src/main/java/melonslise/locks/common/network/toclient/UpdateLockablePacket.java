package melonslise.locks.common.network.toclient;

import melonslise.locks.Locks;
import melonslise.locks.common.capability.LockableHandler;
import melonslise.locks.common.util.Lockable;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

// "locked" is the only synced field for now (expandable, as in the original)
public record UpdateLockablePacket(int id, boolean locked) implements CustomPacketPayload
{
	public static final CustomPacketPayload.Type<UpdateLockablePacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Locks.ID, "update_lockable"));

	public static final StreamCodec<RegistryFriendlyByteBuf, UpdateLockablePacket> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.INT, UpdateLockablePacket::id,
		ByteBufCodecs.BOOL, UpdateLockablePacket::locked,
		UpdateLockablePacket::new);

	public UpdateLockablePacket(Lockable lkb)
	{
		this(lkb.id, lkb.lock.isLocked());
	}

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type()
	{
		return TYPE;
	}

	public static void handle(UpdateLockablePacket pkt, IPayloadContext ctx)
	{
		Lockable lkb = LockableHandler.get(ctx.player().level()).getLoaded().get(pkt.id);
		// Original did not null-check; a late packet for an already unloaded lockable would NPE, so guard it
		if(lkb != null)
			lkb.lock.setLocked(pkt.locked);
	}
}
