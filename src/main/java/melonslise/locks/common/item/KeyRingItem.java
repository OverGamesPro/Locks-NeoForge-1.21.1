package melonslise.locks.common.item;

import java.util.List;
import java.util.stream.Collectors;

import melonslise.locks.common.capability.KeyRingInventory;
import melonslise.locks.common.container.KeyRingContainer;
import melonslise.locks.common.init.LocksSoundEvents;
import melonslise.locks.common.util.Lockable;
import melonslise.locks.common.util.LocksUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;

public class KeyRingItem extends Item
{
	public static final int COLUMNS = 9;

	public final int rows;

	public KeyRingItem(int rows, Properties props)
	{
		super(props.stacksTo(1));
		this.rows = rows;
	}

	// Вместо capability-провайдера: инвентарь строится напрямую (capability для хопперов и т.п. регистрируется в LocksModEvents)
	public static IItemHandler inventory(ItemStack stack)
	{
		return new KeyRingInventory(stack, ((KeyRingItem) stack.getItem()).rows, COLUMNS);
	}

	public static boolean containsId(ItemStack stack, int id)
	{
		IItemHandler inv = inventory(stack);
		for(int a = 0; a < inv.getSlots(); ++a)
		{
			ItemStack key = inv.getStackInSlot(a);
			if(!key.isEmpty() && LockingItem.getOrSetId(key) == id)
				return true;
		}
		return false;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
	{
		ItemStack stack = player.getItemInHand(hand);
		if(!level.isClientSide)
			// TODO(этап 6): KeyRingContainer.Provider(stack) (MenuProvider), writeData(buf, hand) - extra data (замена Writer)
			((ServerPlayer) player).openMenu(new KeyRingContainer.Provider(stack), buf -> KeyRingContainer.writeData(buf, hand));
		return InteractionResultHolder.pass(stack);
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx)
	{
		Level level = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();
		IItemHandler inv = inventory(ctx.getItemInHand());
		List<Lockable> intersect = LocksUtil.intersecting(level, pos).collect(Collectors.toList());
		if(intersect.isEmpty())
			return InteractionResult.PASS;
		for(int a = 0; a < inv.getSlots(); ++a)
		{
			ItemStack key = inv.getStackInSlot(a);
			if(key.isEmpty())
				continue;
			int id = LockingItem.getOrSetId(key);
			List<Lockable> match = intersect.stream().filter(lkb -> lkb.lock.id == id).collect(Collectors.toList());
			if(match.isEmpty())
				continue;
			level.playSound(ctx.getPlayer(), pos, LocksSoundEvents.LOCK_OPEN.get(), SoundSource.BLOCKS, 1f, 1f);
			if(level.isClientSide)
				return InteractionResult.SUCCESS;
			for(Lockable lkb : match)
				lkb.lock.setLocked(!lkb.lock.isLocked());
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.SUCCESS;
	}
}
