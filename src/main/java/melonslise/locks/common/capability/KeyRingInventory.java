package melonslise.locks.common.capability;

import java.util.ArrayList;
import java.util.List;

import melonslise.locks.common.init.LocksItemTags;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

/*
 * Инвентарь связки ключей. Хранится в data component DataComponents.CONTAINER (ItemContainerContents) на самом предмете
 * (раньше - NBT "Items"). Регистрация как item capability (Capabilities.ItemHandler.ITEM) - в этапе 5 вместе с KeyRingItem:
 *   event.registerItem(Capabilities.ItemHandler.ITEM, (stack, ctx) -> new KeyRingInventory(stack, rows, 9), LocksItems.KEY_RING.get());
 * Thanks to Gigaherz
 */
public class KeyRingInventory implements IItemHandlerModifiable
{
	public final int size;
	public final ItemStack stack;

	public KeyRingInventory(ItemStack stack, int rows, int col)
	{
		this.size = rows * col;
		this.stack = stack;
	}

	@Override
	public int getSlots()
	{
		return this.size;
	}

	@Override
	public ItemStack getStackInSlot(int slot)
	{
		this.validateSlotIndex(slot);
		ItemContainerContents contents = this.stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
		return slot < contents.getSlots() ? contents.getStackInSlot(slot) : ItemStack.EMPTY;
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack)
	{
		this.validateSlotIndex(slot);
		ItemContainerContents contents = this.stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
		int n = Math.max(contents.getSlots(), slot + 1);
		List<ItemStack> list = new ArrayList<>(n);
		for(int a = 0; a < n; ++a)
			list.add(a == slot ? stack : a < contents.getSlots() ? contents.getStackInSlot(a) : ItemStack.EMPTY);
		this.stack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(list));
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
	{
		if (stack.isEmpty())
			return ItemStack.EMPTY;
		this.validateSlotIndex(slot);
		ItemStack existing = getStackInSlot(slot);
		int limit = stack.getMaxStackSize();
		if (!existing.isEmpty())
		{
			if (!ItemStack.isSameItemSameComponents(stack, existing))
				return stack;
			limit -= existing.getCount();
		}
		if (limit <= 0)
			return stack;
		boolean reachedLimit = stack.getCount() > limit;
		if (!simulate)
		{
			if (existing.getCount() <= 0)
				existing = reachedLimit ? stack.copyWithCount(limit) : stack;
			else
				existing.grow(reachedLimit ? limit : stack.getCount());
			this.setStackInSlot(slot, existing);
		}
		return reachedLimit ? stack.copyWithCount(stack.getCount() - limit) : ItemStack.EMPTY;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate)
	{
		if (amount == 0)
			return ItemStack.EMPTY;
		this.validateSlotIndex(slot);
		ItemStack existing = this.getStackInSlot(slot);
		if (existing.isEmpty())
			return ItemStack.EMPTY;
		int toExtract = Math.min(amount, existing.getMaxStackSize());
		if (existing.getCount() <= toExtract)
		{
			if (!simulate)
				this.setStackInSlot(slot, ItemStack.EMPTY);
			return existing;
		}
		else
		{
			if (!simulate)
				this.setStackInSlot(slot, existing.copyWithCount(existing.getCount() - toExtract));
			return existing.copyWithCount(toExtract);
		}
	}

	@Override
	public int getSlotLimit(int slot)
	{
		return 64;
	}

	private void validateSlotIndex(int slot)
	{
		if (slot < 0 || slot >= this.getSlots())
			throw new RuntimeException("Slot " + slot + " not in valid range - [0," + getSlots() + ")");
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack)
	{
		return stack.is(LocksItemTags.KEYS);
	}
}
