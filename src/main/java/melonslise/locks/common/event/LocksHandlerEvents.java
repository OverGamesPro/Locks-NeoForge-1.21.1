package melonslise.locks.common.event;

import melonslise.locks.Locks;
import melonslise.locks.common.capability.LockableHandler;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

// Связывает LockableHandler с жизненным циклом чанков/уровней (заменяет часть LocksForgeEvents.onChunkUnload)
@EventBusSubscriber(modid = Locks.ID)
public final class LocksHandlerEvents
{
	private LocksHandlerEvents() {}

	@SubscribeEvent
	public static void onChunkLoad(ChunkEvent.Load e)
	{
		if(e.getChunk() instanceof LevelChunk ch && ch.getLevel() != null)
			LockableHandler.get(ch.getLevel()).onChunkLoad(ch);
	}

	@SubscribeEvent
	public static void onChunkUnload(ChunkEvent.Unload e)
	{
		if(e.getChunk() instanceof LevelChunk ch && ch.getLevel() != null)
			LockableHandler.get(ch.getLevel()).onChunkUnload(ch);
	}

	@SubscribeEvent
	public static void onLevelUnload(LevelEvent.Unload e)
	{
		LevelAccessor acc = e.getLevel();
		if(acc instanceof net.minecraft.world.level.Level level)
			LockableHandler.remove(level);
	}
}
