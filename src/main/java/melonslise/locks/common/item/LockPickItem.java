package melonslise.locks.common.item;

import java.util.List;
import java.util.stream.Collectors;

import melonslise.locks.Locks;
import melonslise.locks.common.container.LockPickingContainer;
import melonslise.locks.common.init.LocksDataComponents;
import melonslise.locks.common.init.LocksEnchantments;
import melonslise.locks.common.util.Lockable;
import melonslise.locks.common.util.LocksPredicates;
import melonslise.locks.common.util.LocksUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class LockPickItem extends Item
{
	public static final Component TOO_COMPLEX_MESSAGE = Component.translatable(Locks.ID + ".status.too_complex");

	public final float strength;

	public LockPickItem(float strength, Properties props)
	{
		super(props);
		this.strength = strength;
	}

	// WARNING: EXPECTS LOCKPICKITEM STACK
	public static float getOrSetStrength(ItemStack stack)
	{
		Float str = stack.get(LocksDataComponents.STRENGTH.get());
		if(str == null)
		{
			str = ((LockPickItem) stack.getItem()).strength;
			stack.set(LocksDataComponents.STRENGTH.get(), str);
		}
		return str;
	}

	public static boolean canPick(ItemStack stack, int cmp)
	{
		return getOrSetStrength(stack) > cmp * 0.25f;
	}

	// Зачарования в 1.21 - datapack; уровень читаем через LocksEnchantments.getLevel (0, если зачарование не найдено)
	public static boolean canPick(ItemStack stack, Level level, Lockable lkb)
	{
		return canPick(stack, LocksEnchantments.getLevel(level, LocksEnchantments.COMPLEXITY, lkb.stack));
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx)
	{
		Level level = ctx.getLevel();
		Player player = ctx.getPlayer();
		BlockPos pos = ctx.getClickedPos();
		List<Lockable> match = LocksUtil.intersecting(level, pos).filter(LocksPredicates.LOCKED).collect(Collectors.toList());
		if(match.isEmpty())
			return InteractionResult.PASS;
		Lockable lkb = match.get(0);
		if(!canPick(ctx.getItemInHand(), level, lkb))
		{
			if(level.isClientSide)
				player.displayClientMessage(TOO_COMPLEX_MESSAGE, true);
			return InteractionResult.PASS;
		}
		if(level.isClientSide)
			return InteractionResult.SUCCESS;
		InteractionHand hand = ctx.getHand();
		// TODO(этап 6): LockPickingContainer.Provider(hand, lkb) = SimpleMenuProvider-подобный MenuProvider,
		// LockPickingContainer.writeData(buf, hand, lkb) = extra data (замена Writer)
		((ServerPlayer) player).openMenu(new LockPickingContainer.Provider(hand, lkb), buf -> LockPickingContainer.writeData(buf, hand, lkb));
		return InteractionResult.SUCCESS;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext ctx, List<Component> lines, TooltipFlag flag)
	{
		super.appendHoverText(stack, ctx, lines, flag);
		lines.add(Component.translatable(Locks.ID + ".tooltip.strength", LocksUtil.FORMAT.format(stack.getOrDefault(LocksDataComponents.STRENGTH.get(), this.strength))).withStyle(ChatFormatting.DARK_GREEN));
	}
}
