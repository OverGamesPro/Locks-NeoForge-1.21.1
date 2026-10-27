package melonslise.locks.common.capability;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import melonslise.locks.common.util.Lockable;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

/*
 * Хранилище замков одного чанка (data attachment на ChunkAccess/LevelChunk) почти без логики.
 * Замки, пересекающие несколько чанков, лежат в хранилище каждого из них. Дубликаты (после загрузки с диска)
 * объединяются в LockableHandler.onChunkLoad, т.к. при десериализации уровень недоступен.
 */
public class LockableStorage
{
	public static final IAttachmentSerializer<ListTag, LockableStorage> SERIALIZER = new IAttachmentSerializer<>()
	{
		@Override
		public LockableStorage read(IAttachmentHolder holder, ListTag tag, HolderLookup.Provider provider)
		{
			LockableStorage st = new LockableStorage(holder);
			for(int a = 0; a < tag.size(); ++a)
			{
				Lockable lkb = Lockable.fromNbt(tag.getCompound(a), provider);
				st.lockables.put(lkb.id, lkb);
			}
			return st;
		}

		@Override
		public ListTag write(LockableStorage st, HolderLookup.Provider provider)
		{
			if(st.lockables.isEmpty())
				return null; // не писать пустое
			ListTag list = new ListTag();
			for(Lockable lkb : st.lockables.values())
				list.add((Tag) Lockable.toNbt(lkb, provider));
			return list;
		}
	};

	public final IAttachmentHolder holder;

	public Int2ObjectMap<Lockable> lockables = new Int2ObjectLinkedOpenHashMap<Lockable>();

	public LockableStorage(IAttachmentHolder holder)
	{
		this.holder = holder;
	}

	public Int2ObjectMap<Lockable> get()
	{
		return this.lockables;
	}

	public void add(Lockable lkb)
	{
		this.lockables.put(lkb.id, lkb);
		this.markUnsaved();
	}

	public void remove(int id)
	{
		this.lockables.remove(id);
		this.markUnsaved();
	}

	private void markUnsaved()
	{
		if(this.holder instanceof ChunkAccess ch)
			ch.setUnsaved(true);
	}
}
