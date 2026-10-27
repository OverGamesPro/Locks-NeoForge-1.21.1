package melonslise.locks.common.recipe;

import melonslise.locks.common.init.LocksDataComponents;
import melonslise.locks.common.init.LocksItems;
import melonslise.locks.common.init.LocksRecipeSerializers;
import melonslise.locks.common.item.LockingItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

// Key copying: any item with a lock id (key / lock / ...) + at least one blank key -> new key with the same id. The original item stays in the grid.
public class KeyRecipe extends CustomRecipe
{
	public KeyRecipe(CraftingBookCategory category)
	{
		super(category);
	}

	// 1.16.5 checked the NBT tag "Id"; 1.21 items store it in the LocksDataComponents.ID data component
	private static boolean hasId(ItemStack stack)
	{
		return stack.has(LocksDataComponents.ID.get());
	}

	@Override
	public RecipeSerializer<?> getSerializer()
	{
		return LocksRecipeSerializers.KEY.get();
	}

	@Override
	public boolean matches(CraftingInput input, Level level)
	{
		boolean hasLocking = false;
		int blanks = 0;

		for(int a = 0; a < input.size(); ++a)
		{
			ItemStack stack = input.getItem(a);
			if(stack.isEmpty())
				continue;
			if(hasId(stack))
			{
				if(hasLocking)
					return false;
				hasLocking = true;
			}
			else if(stack.is(LocksItems.KEY_BLANK.get()))
				++blanks;
			else
				return false;
		}
		return hasLocking && blanks >= 1;
	}

	@Override
	public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries)
	{
		ItemStack locking = ItemStack.EMPTY;
		int blanks = 0;

		for(int a = 0; a < input.size(); ++a)
		{
			ItemStack stack = input.getItem(a);
			if(stack.isEmpty())
				continue;
			if(hasId(stack))
			{
				if(!locking.isEmpty())
					return ItemStack.EMPTY;
				locking = stack;
			}
			else if(stack.is(LocksItems.KEY_BLANK.get()))
				++blanks;
			else
				return ItemStack.EMPTY;
		}

		if(!locking.isEmpty() && blanks >= 1)
			return LockingItem.copyId(locking, new ItemStack(LocksItems.KEY.get()));
		return ItemStack.EMPTY;
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInput input)
	{
		NonNullList<ItemStack> list = NonNullList.withSize(input.size(), ItemStack.EMPTY);

		for(int a = 0; a < list.size(); ++a)
		{
			ItemStack stack = input.getItem(a);
			if(!hasId(stack))
				continue;
			list.set(a, stack.copy());
			break;
		}

		return list;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height)
	{
		return width >= 3 && height >= 3;
	}
}
