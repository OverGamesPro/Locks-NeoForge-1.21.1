package melonslise.locks.common.util;

import java.util.Observable;
import java.util.Random;

import net.minecraft.nbt.CompoundTag;
import melonslise.locks.common.item.LockItem;
import melonslise.locks.common.item.LockingItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

@SuppressWarnings("deprecation") // java.util.Observable - оставлен как в оригинале
public class Lock extends Observable
{
	public final int id;
	// index is the order, value is the pin number
	protected final byte[] combo;
	protected boolean locked;

	// TODO if lock is reshuffled any time other than during creation, then next time it is loaded it will have the initial combination and not the newly reshuffled one. Thankfully reshuffling like that does not happen, but this should be changed if it does happen
	// java.util.Random (а не RandomSource) намеренно: комбинация должна совпадать с оригиналом для одного и того же id
	public final Random rng;

	public Lock(int id, int length, boolean locked)
	{
		this.id = id;
		this.rng = new Random(id);
		this.combo = this.shuffle(length);
		this.locked = locked;
	}

	public static Lock from(ItemStack stack)
	{
		return new Lock(LockingItem.getOrSetId(stack), LockItem.getOrSetLength(stack), !LockItem.isOpen(stack));
	}

	public static final String KEY_ID = "Id", KEY_LENGTH = "Length", KEY_LOCKED = "Locked";

	public static Lock fromNbt(CompoundTag nbt)
	{
		return new Lock(nbt.getInt(KEY_ID), nbt.getByte(KEY_LENGTH), nbt.getBoolean(KEY_LOCKED));
	}

	public static CompoundTag toNbt(Lock lock)
	{
		CompoundTag nbt = new CompoundTag();
		nbt.putInt(KEY_ID, lock.id);
		nbt.putByte(KEY_LENGTH, (byte) lock.combo.length);
		nbt.putBoolean(KEY_LOCKED, lock.locked);
		return nbt;
	}

	public static Lock fromBuf(FriendlyByteBuf buf)
	{
		return new Lock(buf.readInt(), (int) buf.readByte(), buf.readBoolean());
	}

	public static void toBuf(FriendlyByteBuf buf, Lock lock)
	{
		buf.writeInt(lock.id);
		buf.writeByte((int) lock.getLength());
		buf.writeBoolean(lock.isLocked());
	}

	public byte[] shuffle(int length)
	{
		byte[] combo = new byte[length];
		for(byte a = 0; a < length; ++a)
			combo[a] = a;
		LocksUtil.shuffle(combo, this.rng);
		return combo;
	}

	public int getLength()
	{
		return this.combo.length;
	}

	public boolean isLocked()
	{
		return this.locked;
	}

	public void setLocked(boolean locked)
	{
		if(this.locked == locked)
			return;
		this.locked = locked;
		this.setChanged();
		this.notifyObservers();
	}

	public int getPin(int index)
	{
		return this.combo[index];
	}

	public boolean checkPin(int index, int pin)
	{
		return this.getPin(index) == pin;
	}
}
