package melonslise.locks.common.item;

import java.util.List;

import melonslise.locks.Locks;
import melonslise.locks.common.util.LocksUtil;
import melonslise.locks.common.config.LocksServerConfig;
import melonslise.locks.common.init.LocksDataComponents;
import melonslise.locks.common.init.LocksSoundEvents;
import melonslise.locks.common.capability.LockableHandler;
import melonslise.locks.common.init.LocksAttachments;
import melonslise.locks.common.capability.Selection;
import melonslise.locks.common.util.Cuboid6i;
import melonslise.locks.common.util.Lock;
import melonslise.locks.common.util.Lockable;
import melonslise.locks.common.util.Transform;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

public class LockItem extends LockingItem
{
	public final int length;
	public final int enchantmentValue;
	public final int resistance;

		public LockItem(int length, int enchVal, int resist, Properties props)
	{
		super(props);
		this.length = length;
		this.enchantmentValue = enchVal;
		this.resistance = resist;
	}

	public static boolean isOpen(ItemStack stack)
	{
		return stack.getOrDefault(LocksDataComponents.OPEN.get(), false);
	}

	public static void setOpen(ItemStack stack, boolean open)
	{
		if(!stack.isEmpty())
			stack.set(LocksDataComponents.OPEN.get(), open);
	}

	// WARNING: EXPECTS LOCKITEM STACK
	public static int getOrSetLength(ItemStack stack)
	{
		Integer len = stack.get(LocksDataComponents.LENGTH.get());
		if(len == null)
		{
			len = ((LockItem) stack.getItem()).length;
			stack.set(LocksDataComponents.LENGTH.get(), len);
		}
		return len;
	}

	// WARNING: EXPECTS LOCKITEM STACK
	public static int getResistance(ItemStack stack)
	{
		return ((LockItem) stack.getItem()).resistance;
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx)
	{
		Level level = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();
		if(!LocksServerConfig.canLock(level, pos) || LockableHandler.get(level).getInChunk(pos).values().stream().anyMatch(lkb -> lkb.bb.intersects(pos)))
			return InteractionResult.PASS;
		return LocksServerConfig.EASY_LOCK.get() ? this.easyLock(ctx) : this.freeLock(ctx);
	}

	public InteractionResult freeLock(UseOnContext ctx)
	{
		Player player = ctx.getPlayer();
		BlockPos pos = ctx.getClickedPos();
		Selection select = player.getData(LocksAttachments.SELECTION);
		BlockPos pos1 = select.get();
		if(pos1 == null)
			select.set(pos);
		else
		{
			Level level = ctx.getLevel();
			select.set(null);
			// FIXME Go through the add checks here as well
			level.playSound(player, pos, LocksSoundEvents.LOCK_CLOSE.get(), SoundSource.BLOCKS, 1f, 1f);
			if(level.isClientSide)
				return InteractionResult.SUCCESS;
			ItemStack stack = ctx.getItemInHand();
			ItemStack lockStack = stack.copy();
			lockStack.setCount(1);
			LockableHandler handler = LockableHandler.get(level);
			if(!handler.add(new Lockable(new Cuboid6i(pos1, pos), Lock.from(stack), Transform.fromDirection(ctx.getClickedFace(), player.getDirection().getOpposite()), lockStack, handler.nextId())))
				return InteractionResult.PASS;
			if(!player.isCreative())
				stack.shrink(1);
		}
		return InteractionResult.SUCCESS;
	}

	public InteractionResult easyLock(UseOnContext ctx)
	{
		Player player = ctx.getPlayer();
		Level level = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();
		level.playSound(player, pos, LocksSoundEvents.LOCK_CLOSE.get(), SoundSource.BLOCKS, 1f, 1f);
		if(level.isClientSide)
			return InteractionResult.SUCCESS;
		BlockState state = level.getBlockState(pos);
		BlockPos pos1 = pos;
		if(state.hasProperty(BlockStateProperties.CHEST_TYPE) && state.getValue(BlockStateProperties.CHEST_TYPE) != ChestType.SINGLE)
			pos1 = pos.relative(ChestBlock.getConnectedDirection(state));
		else if(state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF))
		{
			pos1 = state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
			if(state.hasProperty(BlockStateProperties.DOOR_HINGE) && state.hasProperty(BlockStateProperties.HORIZONTAL_FACING))
			{
				Direction dir = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
				BlockPos pos2 = pos1.relative(state.getValue(BlockStateProperties.DOOR_HINGE) == DoorHingeSide.LEFT ? dir.getClockWise() : dir.getCounterClockWise());
				if(level.getBlockState(pos2).is(state.getBlock()))
					pos1 = pos2;
			}
		}
		ItemStack stack = ctx.getItemInHand();
		ItemStack lockStack = stack.copy();
		lockStack.setCount(1);
		LockableHandler handler = LockableHandler.get(level);
		if(!handler.add(new Lockable(new Cuboid6i(pos, pos1), Lock.from(stack), Transform.fromDirection(ctx.getClickedFace(), player.getDirection().getOpposite()), lockStack, handler.nextId())))
			return InteractionResult.PASS;
		if(!player.isCreative())
			stack.shrink(1);
		return InteractionResult.SUCCESS;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
	{
		ItemStack stack = player.getItemInHand(hand);
		if(!isOpen(stack))
			return super.use(level, player, hand);
		setOpen(stack, false);
		level.playSound(player, player.getX(), player.getY(), player.getZ(), LocksSoundEvents.PIN_MATCH.get(), SoundSource.PLAYERS, 1f, 1f);
		return super.use(level, player, hand);
	}

	// 1.21.1: компонента ENCHANTABLE ещё нет (появилась в 1.21.2), поэтому как в оригинале - переопределения
	@Override
	public boolean isEnchantable(ItemStack stack)
	{
		return true;
	}

	@Override
	public int getEnchantmentValue()
	{
		return this.enchantmentValue;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext ctx, List<Component> lines, TooltipFlag flag)
	{
		super.appendHoverText(stack, ctx, lines, flag);
		lines.add(Component.translatable(Locks.ID + ".tooltip.length", LocksUtil.FORMAT.format(stack.getOrDefault(LocksDataComponents.LENGTH.get(), this.length))).withStyle(ChatFormatting.DARK_GREEN));
	}
}
