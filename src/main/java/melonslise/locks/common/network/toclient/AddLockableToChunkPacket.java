package melonslise.locks.common.network.toclient;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import melonslise.locks.Locks;
import melonslise.locks.common.capability.LockableHandler;
import melonslise.locks.common.init.LocksAttachments;
import melonslise.locks.common.util.Lockable;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record AddLockableToChunkPacket(Lockable lockable, int x, int z) implements CustomPacketPayload
{
	public static final CustomPacketPayload.Type<AddLockableToChunkPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Locks.ID, "add_lockable_to_chunk"));

	public static final StreamCodec<RegistryFriendlyByteBuf, AddLockableToChunkPacket> STREAM_CODEC = StreamCodec.composite(
		Lockable.STREAM_CODEC, AddLockableToChunkPacket::lockable,
		ByteBufCodecs.INT, AddLockableToChunkPacket::x,
		ByteBufCodecs.INT, AddLockableToChunkPacket::z,
		AddLockableToChunkPacket::new);

	public AddLockableToChunkPacket(Lockable lkb, ChunkPos pos)
	{
		this(lkb, pos.x, pos.z);
	}

	public AddLockableToChunkPacket(Lockable lkb, LevelChunk ch)
	{
		this(lkb, ch.getPos());
	}

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type()
	{
		return TYPE;
	}

	public static void handle(AddLockableToChunkPacket pkt, IPayloadContext ctx)
	{
		Level level = ctx.player().level();
		// NOTE: if LocksAttachments.LOCKABLE_STORAGE is a DeferredHolder/Supplier in stage 4, add .get() (see HANDOFF)
		var storage = level.getChunk(pkt.x, pkt.z).getData(LocksAttachments.LOCKABLE_STORAGE);
		LockableHandler handler = LockableHandler.get(level);
		Int2ObjectMap<Lockable> lkbs = handler.getLoaded();
		Lockable lkb = lkbs.get(pkt.lockable.id);
		if(lkb == lkbs.defaultReturnValue())
		{
			lkb = pkt.lockable;
			lkb.addObserver(handler);
			lkbs.put(lkb.id, lkb);
		}
		storage.add(lkb);
	}
}
