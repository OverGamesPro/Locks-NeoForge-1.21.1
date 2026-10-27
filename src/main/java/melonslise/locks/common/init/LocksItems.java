package melonslise.locks.common.init;

import java.util.function.Supplier;

import melonslise.locks.Locks;
import melonslise.locks.common.item.KeyItem;
import melonslise.locks.common.item.KeyRingItem;
import melonslise.locks.common.item.LockItem;
import melonslise.locks.common.item.LockPickItem;
import melonslise.locks.common.item.MasterKeyItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/*
 * Регистрация: LocksItems.ITEMS.register(modBus); LocksItems.CREATIVE_TABS.register(modBus) в конструкторе Locks.
 */
public final class LocksItems
{
	public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Locks.ID);
	public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Locks.ID);

	public static final DeferredItem<Item>
		SPRING = ITEMS.registerSimpleItem("spring"),
		WOOD_LOCK_MECHANISM = ITEMS.registerSimpleItem("wood_lock_mechanism"),
		IRON_LOCK_MECHANISM = ITEMS.registerSimpleItem("iron_lock_mechanism"),
		STEEL_LOCK_MECHANISM = ITEMS.registerSimpleItem("steel_lock_mechanism"),
		KEY_BLANK = ITEMS.registerSimpleItem("key_blank"),
		WOOD_LOCK = ITEMS.register("wood_lock", () -> new LockItem(5, 15, 4, new Item.Properties())),
		IRON_LOCK = ITEMS.register("iron_lock", () -> new LockItem(7, 14, 12, new Item.Properties())),
		STEEL_LOCK = ITEMS.register("steel_lock", () -> new LockItem(9, 12, 20, new Item.Properties())),
		GOLD_LOCK = ITEMS.register("gold_lock", () -> new LockItem(6, 22, 6, new Item.Properties())),
		DIAMOND_LOCK = ITEMS.register("diamond_lock", () -> new LockItem(11, 10, 100, new Item.Properties())),
		KEY = ITEMS.register("key", () -> new KeyItem(new Item.Properties())),
		MASTER_KEY = ITEMS.register("master_key", () -> new MasterKeyItem(new Item.Properties())),
		KEY_RING = ITEMS.register("key_ring", () -> new KeyRingItem(1, new Item.Properties())),
		WOOD_LOCK_PICK = ITEMS.register("wood_lock_pick", () -> new LockPickItem(0.2f, new Item.Properties())),
		IRON_LOCK_PICK = ITEMS.register("iron_lock_pick", () -> new LockPickItem(0.35f, new Item.Properties())),
		STEEL_LOCK_PICK = ITEMS.register("steel_lock_pick", () -> new LockPickItem(0.7f, new Item.Properties())),
		GOLD_LOCK_PICK = ITEMS.register("gold_lock_pick", () -> new LockPickItem(0.25f, new Item.Properties())),
		DIAMOND_LOCK_PICK = ITEMS.register("diamond_lock_pick", () -> new LockPickItem(0.85f, new Item.Properties()));

	// Вместо ItemGroup TAB. Название: lang-ключ "itemGroup.locks" (как в оригинале)
	public static final Supplier<CreativeModeTab> TAB = CREATIVE_TABS.register("main", () -> CreativeModeTab.builder()
		.title(Component.translatable("itemGroup." + Locks.ID))
		.icon(() -> new ItemStack(IRON_LOCK.get()))
		.displayItems((params, output) -> ITEMS.getEntries().forEach(e -> output.accept(e.get())))
		.build());

	private LocksItems() {}
}
