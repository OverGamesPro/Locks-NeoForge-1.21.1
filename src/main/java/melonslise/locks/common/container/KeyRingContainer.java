package melonslise.locks.common.container;

import melonslise.locks.common.init.LocksContainerTypes;
import melonslise.locks.common.init.LocksSoundEvents;
import melonslise.locks.common.item.KeyRingItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.neoforged.neoforge.network.IContainerFactory;

/*
 * Порт меню связки ключей (этап 6.1).
 * Инвентарь берётся напрямую из KeyRingItem.inventory(stack) (вместо stack.getCapability).
 * Регистрация MenuType - LocksContainerTypes.KEY_RING.
 */
public class KeyRingContainer extends AbstractContainerMenu
{
	public static class KeyRingSlot extends SlotItemHandler
	{
		public final Player player;

		public KeyRingSlot(Player player, IItemHandler inv, int index, int x, int y)
		{
			super(inv, index, x, y);
			this.player = player;
		}

		private void playKeyRingSound()
		{
			if(!this.player.level().isClientSide)
				this.player.level().playSound(null, this.player.getX(), this.player.getY(), this.player.getZ(), LocksSoundEvents.KEY_RING.get(), SoundSource.PLAYERS, 1f, 1f);
		}

		// TODO PITCH
		@Override
		public void set(ItemStack stack)
		{
			super.set(stack);
			this.playKeyRingSound();
		}

		// В 1.21.1 Slot.onTake возвращает void (в 1.16.5 возвращал ItemStack)
		@Override
		public void onTake(Player player, ItemStack stack)
		{
			this.playKeyRingSound();
			super.onTake(player, stack);
		}
	}

	public final ItemStack stack;
	public final IItemHandler inv;
	public final int rows;

	public KeyRingContainer(int id, Player player, ItemStack stack)
	{
		super(LocksContainerTypes.KEY_RING.get(), id);
		this.stack = stack;
		this.inv = KeyRingItem.inventory(stack);

		this.rows = this.inv.getSlots() / 9;
		for(int row = 0; row < this.rows; ++row)
			for(int col = 0; col < 9; ++col)
				this.addSlot(new KeyRingSlot(player, this.inv, col + row * 9, 8 + col * 18, 18 + row * 18));

		int offset = (this.rows - 4) * 18;
		for(int row = 0; row < 3; ++row)
			for(int col = 0; col < 9; ++col)
				this.addSlot(new Slot(player.getInventory(), col + row * 9 + 9, 8 + col * 18, 103 + row * 18 + offset));

		for(int col = 0; col < 9; ++col)
			this.addSlot(new Slot(player.getInventory(), col, 8 + col * 18, 161 + offset));
	}

	@Override
	public boolean stillValid(Player player)
	{
		return !this.stack.isEmpty();
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index)
	{
		ItemStack result = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if(slot == null || !slot.hasItem())
			return result;
		ItemStack stack1 = slot.getItem();
		result = stack1.copy();
		if(index < this.inv.getSlots())
		{
			if(!this.moveItemStackTo(stack1, this.inv.getSlots(), this.slots.size(), true))
				return ItemStack.EMPTY;
		}
		else if(!this.moveItemStackTo(stack1, 0, this.inv.getSlots(), false))
			return ItemStack.EMPTY;
		if(stack1.isEmpty())
			slot.set(ItemStack.EMPTY);
		else
			slot.setChanged();
		return result;
	}

	// Клиентская фабрика: extra data = рука (см. writeData)
	public static final IContainerFactory<KeyRingContainer> FACTORY = (id, inv, buf) ->
		new KeyRingContainer(id, inv.player, inv.player.getItemInHand(buf.readEnum(InteractionHand.class)));

	// Замена Writer: вызывается из ServerPlayer.openMenu(provider, buf -> writeData(buf, hand))
	public static void writeData(FriendlyByteBuf buf, InteractionHand hand)
	{
		buf.writeEnum(hand);
	}

	public static class Provider implements MenuProvider
	{
		public final ItemStack stack;

		public Provider(ItemStack stack)
		{
			this.stack = stack;
		}

		@Override
		public AbstractContainerMenu createMenu(int id, Inventory inv, Player player)
		{
			return new KeyRingContainer(id, player, this.stack);
		}

		@Override
		public Component getDisplayName()
		{
			return this.stack.getHoverName();
		}
	}
}
