package melonslise.locks.common.event;

import melonslise.locks.Locks;
import melonslise.locks.common.init.LocksAttachments;
import melonslise.locks.common.network.toclient.AddLockableToChunkPacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.neoforged.neoforge.network.PacketDistributor;

// Replaces ChunkManagerMixin (playerLoadedChunk -> send all lockables of the chunk) and the packet sending part of ChunkMixin.
// Only the player that just received the chunk gets the packets (the original broadcast to all tracking players).
@EventBusSubscriber(modid = Locks.ID)
public final class LocksWorldEvents
{
	private LocksWorldEvents() {}

	@SubscribeEvent
	public static void onChunkSent(ChunkWatchEvent.Sent e)
	{
		// NOTE: if LocksAttachments.LOCKABLE_STORAGE is a DeferredHolder/Supplier, add .get()
		e.getChunk().getData(LocksAttachments.LOCKABLE_STORAGE).get().values()
			.forEach(lkb -> PacketDistributor.sendToPlayer(e.getPlayer(), new AddLockableToChunkPacket(lkb, e.getChunk())));
	}
}
