package melonslise.locks.common.item;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import melonslise.locks.Locks;
import melonslise.locks.common.util.LocksUtil;
import melonslise.locks.common.init.LocksDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class LockingItem extends Item
{
	public LockingItem(Properties props)
	{
		super(props.stacksTo(1));
	}

	public static ItemStack copyId(ItemStack from, ItemStack to)
	{
		to.set(LocksDataComponents.ID.get(), getOrSetId(from));
		return to;
	}

	// Пустой стек (слот связки ключей) не трогаем: на EMPTY нельзя вешать компоненты. 0 = "нет id"
	public static int getOrSetId(ItemStack stack)
	{
		if(stack.isEmpty())
			return 0;
		Integer id = stack.get(LocksDataComponents.ID.get());
		if(id == null)
		{
			id = ThreadLocalRandom.current().nextInt();
			stack.set(LocksDataComponents.ID.get(), id);
		}
		return id;
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected)
	{
		if(!level.isClientSide)
			getOrSetId(stack);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext ctx, List<Component> lines, TooltipFlag flag)
	{
		Integer id = stack.get(LocksDataComponents.ID.get());
		if(id != null)
			lines.add(Component.translatable(Locks.ID + ".tooltip.id", LocksUtil.FORMAT.format(id)).withStyle(ChatFormatting.DARK_GREEN));
	}
}
