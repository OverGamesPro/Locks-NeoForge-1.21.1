package melonslise.locks.common.init;

import java.util.Collection;
import java.util.stream.Stream;

import melonslise.locks.Locks;
import melonslise.locks.common.network.toclient.AddLockablePacket;
import melonslise.locks.common.network.toclient.AddLockableToChunkPacket;
import melonslise.locks.common.network.toclient.RemoveLockablePacket;
import melonslise.locks.common.network.toclient.TryPinResultPacket;
import melonslise.locks.common.network.toclient.UpdateLockablePacket;
import melonslise.locks.common.network.toserver.TryPinPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

// Replaces SimpleChannel MAIN + LocksPacketDistributors.TRACKING_AREA (both removed).
// Registers itself on the mod bus (no need to touch Locks constructor or LocksModEvents).
@EventBusSubscriber(modid = Locks.ID, bus = EventBusSubscriber.Bus.MOD)
public final class LocksNetwork
{
	public static final String VERSION = "1";

	private LocksNetwork() {}

	@SubscribeEvent
	public static void register(RegisterPayloadHandlersEvent e)
	{
		// optional(): original channel accepted any remote version / absence (a -> true)
		PayloadRegistrar reg = e.registrar(Locks.ID).versioned(VERSION).optional();
		reg.playToClient(AddLockablePacket.TYPE, AddLockablePacket.STREAM_CODEC, AddLockablePacket::handle);
		reg.playToClient(AddLockableToChunkPacket.TYPE, AddLockableToChunkPacket.STREAM_CODEC, AddLockableToChunkPacket::handle);
		reg.playToClient(RemoveLockablePacket.TYPE, RemoveLockablePacket.STREAM_CODEC, RemoveLockablePacket::handle);
		reg.playToClient(UpdateLockablePacket.TYPE, UpdateLockablePacket.STREAM_CODEC, UpdateLockablePacket::handle);
		reg.playToClient(TryPinResultPacket.TYPE, TryPinResultPacket.STREAM_CODEC, TryPinResultPacket::handle);
		reg.playToServer(TryPinPacket.TYPE, TryPinPacket.STREAM_CODEC, TryPinPacket::handle);
	}

	// Replacement of LocksPacketDistributors.TRACKING_AREA: sends to every player tracking any of the chunks, each player once.
	public static void sendToTracking(Collection<LevelChunk> chunks, CustomPacketPayload pkt)
	{
		chunks.stream()
			.flatMap(chunk -> ((ServerChunkCache) chunk.getLevel().getChunkSource()).chunkMap.getPlayers(chunk.getPos(), false).stream())
			.distinct()
			.forEach(player -> PacketDistributor.sendToPlayer(player, pkt));
	}

	public static void sendToTracking(Stream<LevelChunk> chunks, CustomPacketPayload pkt)
	{
		sendToTracking(chunks.toList(), pkt);
	}
}
