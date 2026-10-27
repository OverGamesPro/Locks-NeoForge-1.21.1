package melonslise.locks.common.capability;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

/*
 * Хранит последний выданный id замка для измерения (раньше это был NBT capability на World).
 * Файл: data/locks_lockable_handler.dat
 */
public class LockableIdData extends SavedData
{
	public static final String NAME = "locks_lockable_handler";

	public static final SavedData.Factory<LockableIdData> FACTORY = new SavedData.Factory<>(LockableIdData::new, LockableIdData::load, null);

	public int lastId;

	public LockableIdData() {}

	public static LockableIdData load(CompoundTag tag, HolderLookup.Provider provider)
	{
		LockableIdData data = new LockableIdData();
		data.lastId = tag.getInt("LastId");
		return data;
	}

	@Override
	public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider)
	{
		tag.putInt("LastId", this.lastId);
		return tag;
	}
}
