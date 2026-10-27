package melonslise.locks.common.container;

import melonslise.locks.Locks;
import melonslise.locks.client.gui.LockPickingScreen;
import melonslise.locks.common.capability.LockableHandler;
import melonslise.locks.common.init.LocksContainerTypes;
import melonslise.locks.common.init.LocksDamageSources;
import melonslise.locks.common.init.LocksEnchantments;
import melonslise.locks.common.init.LocksItemTags;
import melonslise.locks.common.init.LocksSoundEvents;
import melonslise.locks.common.item.LockPickItem;
import melonslise.locks.common.network.toclient.TryPinResultPacket;
import melonslise.locks.common.util.Lockable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.network.PacketDistributor;

/*
 * Порт меню взлома (этап 6.2).
 * Ссылается на ещё не портированные классы: LockPickingScreen (6.4), TryPinResultPacket(boolean correct, boolean reset) (этап 7).
 */
public class LockPickingContainer extends AbstractContainerMenu
{
	public static class HiddenSlot extends Slot
	{
		public HiddenSlot(Container inv, int index, int x, int y)
		{
			super(inv, index, x, y);
		}

		@OnlyIn(Dist.CLIENT)
		@Override
		public boolean isActive()
		{
			return false;
		}
	}

	public static final Component TITLE = Component.translatable(Locks.ID + ".gui.lockpicking.title");

	public final Player player;
	public final InteractionHand hand;
	public final Lockable lockable;

	public final Vec3 pos;

	public final int shocking, sturdy, complexity;

	protected int currIndex = 0;

	public LockPickingContainer(int id, Player player, InteractionHand hand, Lockable lkb)
	{
		super(LocksContainerTypes.LOCK_PICKING.get(), id);
		this.player = player;
		this.hand = hand;
		this.lockable = lkb;

		Lockable.State state = lkb.getLockState(player.level());
		this.pos = state == null ? lkb.bb.center() : state.pos;

		this.shocking = LocksEnchantments.getLevel(player.level(), LocksEnchantments.SHOCKING, lkb.stack);
		this.sturdy = LocksEnchantments.getLevel(player.level(), LocksEnchantments.STURDY, lkb.stack);
		this.complexity = LocksEnchantments.getLevel(player.level(), LocksEnchantments.COMPLEXITY, lkb.stack);

		// Syncs the player inventory
		for(int rows = 0; rows < 3; ++rows)
			for(int cols = 0; cols < 9; ++cols)
				this.addSlot(new HiddenSlot(player.getInventory(), cols + rows * 9 + 9, 0, 0));

		for(int slots = 0; slots < 9; ++slots)
			this.addSlot(new HiddenSlot(player.getInventory(), slots, 0, 0));
	}

	public boolean isValidPick(ItemStack stack)
	{
		return stack.is(LocksItemTags.LOCK_PICKS) && LockPickItem.canPick(stack, this.complexity);
	}

	@Override
	public boolean stillValid(Player player)
	{
		return this.lockable.lock.isLocked() && this.isValidPick(player.getItemInHand(this.hand));
	}

	// Меню не принимает shift-клик: все слоты скрытые (как в оригинале, где метод не переопределялся - в 1.21 он абстрактный)
	@Override
	public ItemStack quickMoveStack(Player player, int index)
	{
		return ItemStack.EMPTY;
	}

	public boolean isOpen()
	{
		return this.currIndex == this.lockable.lock.getLength();
	}

	protected void reset()
	{
		this.currIndex = 0;
	}

	// SERVER ONLY
	public void tryPin(int currPin)
	{
		if(this.isOpen())
			return;
		boolean correct = false;
		boolean reset = false;
		if(this.lockable.lock.checkPin(this.currIndex, currPin))
		{
			++this.currIndex;
			correct = true;
			this.player.level().playSound(null, this.pos.x, this.pos.y, this.pos.z, LocksSoundEvents.PIN_MATCH.get(), SoundSource.BLOCKS, 1f, 1f);
		}
		else
		{
			if(this.tryBreakPick(this.player, currPin))
			{
				reset = true;
				this.reset();
				if(this.shocking > 0)
				{
					this.player.hurt(LocksDamageSources.shock(this.player.level()), this.shocking * 1.5f);
					this.player.level().playSound(null, this.player.getX(), this.player.getY(), this.player.getZ(), LocksSoundEvents.SHOCK.get(), SoundSource.BLOCKS, 1f, 1f);
				}
			}
			else
				this.player.level().playSound(null, this.pos.x, this.pos.y, this.pos.z, LocksSoundEvents.PIN_FAIL.get(), SoundSource.BLOCKS, 1f, 1f);
		}
		PacketDistributor.sendToPlayer((ServerPlayer) this.player, new TryPinResultPacket(correct, reset));
	}

	@OnlyIn(Dist.CLIENT)
	public void handlePin(boolean correct, boolean reset)
	{
		Screen screen = Minecraft.getInstance().screen;
		if(screen instanceof LockPickingScreen)
			((LockPickingScreen) screen).handlePin(correct, reset);
		if(correct)
			++this.currIndex;
		if(reset)
			this.reset();
	}

	protected boolean tryBreakPick(Player player, int pin)
	{
		ItemStack pickStack = player.getItemInHand(this.hand);
		float sturdyModifier = this.sturdy == 0 ? 1f : 0.75f + this.sturdy * 0.5f;
		float ch = LockPickItem.getOrSetStrength(pickStack) / sturdyModifier;
		float ex = (1f - ch) * (1f - this.getBreakChanceMultiplier(pin));

		if(!pickStack.is(LocksItemTags.LOCK_PICKS) || player.level().random.nextFloat() < ex + ch)
			return false;
		player.onEquippedItemBroken(pickStack.getItem(), this.hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
		pickStack.shrink(1);
		if(pickStack.isEmpty())
			for(int a = 0; a < player.getInventory().getContainerSize(); ++a)
			{
				ItemStack stack = player.getInventory().getItem(a);
				if(this.isValidPick(stack))
				{
					player.setItemInHand(this.hand, stack);
					player.getInventory().removeItemNoUpdate(a);
					break;
				}
			}
		return true;
	}

	protected float getBreakChanceMultiplier(int pin)
	{
		return Math.abs(this.lockable.lock.getPin(this.currIndex) - pin) == 1 ? 0.33f : 1f;
	}

	@Override
	public void removed(Player player)
	{
		super.removed(player);
		if(!this.isOpen() || !this.lockable.lock.isLocked())
			return;
		this.lockable.lock.setLocked(!this.lockable.lock.isLocked());
		this.player.level().playSound(player, this.pos.x, this.pos.y, this.pos.z, LocksSoundEvents.LOCK_OPEN.get(), SoundSource.BLOCKS, 1f, 1f);
	}

	// Клиентская фабрика: extra data = рука + id замка (см. writeData)
	public static final IContainerFactory<LockPickingContainer> FACTORY = (id, inv, buf) ->
	{
		InteractionHand hand = buf.readEnum(InteractionHand.class);
		return new LockPickingContainer(id, inv.player, hand, LockableHandler.get(inv.player.level()).getLoaded().get(buf.readInt()));
	};

	// Замена Writer: ServerPlayer.openMenu(provider, buf -> writeData(buf, hand, lkb))
	public static void writeData(FriendlyByteBuf buf, InteractionHand hand, Lockable lkb)
	{
		buf.writeEnum(hand);
		buf.writeInt(lkb.id);
	}

	public static class Provider implements MenuProvider
	{
		public final InteractionHand hand;
		public final Lockable lockable;

		public Provider(InteractionHand hand, Lockable lkb)
		{
			this.hand = hand;
			this.lockable = lkb;
		}

		@Override
		public AbstractContainerMenu createMenu(int id, Inventory inv, Player player)
		{
			return new LockPickingContainer(id, player, this.hand, this.lockable);
		}

		@Override
		public Component getDisplayName()
		{
			return TITLE;
		}
	}
}
