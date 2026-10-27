package melonslise.locks.common.capability;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import melonslise.locks.common.config.LocksServerConfig;
import melonslise.locks.common.init.LocksAttachments;
import melonslise.locks.common.init.LocksNetwork;
import melonslise.locks.common.network.toclient.AddLockablePacket;
import melonslise.locks.common.network.toclient.RemoveLockablePacket;
import melonslise.locks.common.network.toclient.UpdateLockablePacket;
import melonslise.locks.common.util.Lockable;
import melonslise.locks.common.util.LocksCapabilityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

/*
 * Управляет логикой всех ЗАГРУЖЕННЫХ замков уровня, обращаясь к LockableStorage чанков.
 * Больше НЕ capability: получать через LockableHandler.get(level). Живёт в рантайм-карте (чистится на LevelEvent.Unload / выходе клиента),
 * счётчик id сохраняется в SavedData (только сервер).
 */
public class LockableHandler implements Observer
{
	private static final Map<Level, LockableHandler> HANDLERS = new ConcurrentHashMap<>();

	public static LockableHandler get(Level level)
	{
		return HANDLERS.computeIfAbsent(level, LockableHandler::new);
	}

	public static void remove(Level level)
	{
		HANDLERS.remove(level);
	}

	public static void clear()
	{
		HANDLERS.clear();
	}

	public final Level world;

	private final LockableIdData idData;
	private int lastId;

	public Int2ObjectMap<Lockable> lockables = new Int2ObjectLinkedOpenHashMap<Lockable>();

	private LockableHandler(Level world)
	{
		this.world = world;
		if(world instanceof ServerLevel sl)
		{
			this.idData = sl.getDataStorage().computeIfAbsent(LockableIdData.FACTORY, LockableIdData.NAME);
			this.lastId = this.idData.lastId;
		}
		else
			this.idData = null;
	}

	public synchronized int nextId()
	{
		int id = ++this.lastId;
		if(this.idData != null)
		{
			this.idData.lastId = id;
			this.idData.setDirty();
		}
		return id;
	}

	public Int2ObjectMap<Lockable> getLoaded()
	{
		return this.lockables;
	}

	// Пустая карта (а не null, как в оригинале), если чанк не загружен
	public Int2ObjectMap<Lockable> getInChunk(BlockPos pos)
	{
		if(!this.world.hasChunkAt(pos))
			return Int2ObjectMaps.emptyMap();
		return this.world.getChunkAt(pos).getData(LocksAttachments.LOCKABLE_STORAGE).get();
	}

	public boolean add(Lockable lkb)
	{
		if(lkb.bb.volume() > LocksServerConfig.MAX_LOCKABLE_VOLUME.get())
			return false;
		List<LockableStorage> sts = lkb.bb.<LockableStorage>containedChunksTo((x, z) ->
		{
			if(!this.world.hasChunk(x, z))
				return null;
			LockableStorage st = this.world.getChunk(x, z).getData(LocksAttachments.LOCKABLE_STORAGE);
			return st.get().values().stream().anyMatch(lkb1 -> lkb1.bb.intersects(lkb.bb)) ? null : st;
		}, true);
		if(sts == null)
			return false;

		// Add to chunk
		for(int a = 0; a < sts.size(); ++a)
			sts.get(a).add(lkb);
		// Add to world
		this.lockables.put(lkb.id, lkb);
		lkb.addObserver(this);
		// Do client/server extras
		if(this.world.isClientSide)
			lkb.swing(10);
		else
		{
			LocksNetwork.sendToTracking(this.chunksOf(lkb), new AddLockablePacket(lkb));
			LocksCapabilityUtil.invalidate(this.world, lkb);
		}
		return true;
	}

	public boolean remove(int id)
	{
		Lockable lkb = this.lockables.get(id);
		if(lkb == null)
			return false;
		List<LevelChunk> chs = this.chunksOf(lkb);

		// Remove from chunk
		for(int a = 0; a < chs.size(); ++a)
			chs.get(a).getData(LocksAttachments.LOCKABLE_STORAGE).remove(id);
		// Remove from world
		this.lockables.remove(id);
		lkb.deleteObserver(this);
		// Do client/server extras
		if(this.world.isClientSide)
			return true;
		LocksNetwork.sendToTracking(chs, new RemoveLockablePacket(id));
		LocksCapabilityUtil.invalidate(this.world, lkb);
		return true;
	}

	// Загруженные чанки, которые содержит замок
	private List<LevelChunk> chunksOf(Lockable lkb)
	{
		return lkb.bb.<LevelChunk>containedChunksTo((x, z) -> this.world.hasChunk(x, z) ? this.world.getChunk(x, z) : null, false)
			.stream().filter(Objects::nonNull).collect(Collectors.toList());
	}

	@Override
	public void update(Observable o, Object arg)
	{
		if(this.world.isClientSide || !(o instanceof Lockable lockable))
			return;
		LocksNetwork.sendToTracking(this.chunksOf(lockable), new UpdateLockablePacket(lockable));
		LocksCapabilityUtil.invalidate(this.world, lockable);
	}

	/*
	 * Вызывается на ChunkEvent.Load (сервер и клиент): замки, загруженные из NBT чанка, объединяются с уже загруженными
	 * (замок, лежащий в нескольких чанках, должен быть одним объектом) и регистрируются в хендлере.
	 */
	public void onChunkLoad(LevelChunk ch)
	{
		LockableStorage st = ch.getData(LocksAttachments.LOCKABLE_STORAGE);
		for(Lockable lkb : st.get().values().toArray(new Lockable[0]))
		{
			Lockable loaded = this.lockables.get(lkb.id);
			if(loaded == null)
			{
				lkb.addObserver(this);
				this.lockables.put(lkb.id, lkb);
			}
			else if(loaded != lkb)
				st.get().put(lkb.id, loaded);
		}
	}

	public void onChunkUnload(LevelChunk ch)
	{
		ch.getData(LocksAttachments.LOCKABLE_STORAGE).get().values().forEach(lkb ->
		{
			this.lockables.remove(lkb.id);
			lkb.deleteObserver(this);
		});
	}
}
