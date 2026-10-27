package melonslise.locks.common.event;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import melonslise.locks.Locks;
import melonslise.locks.common.capability.LockableHandler;
import melonslise.locks.common.config.LocksClientConfig;
import melonslise.locks.common.config.LocksServerConfig;
import melonslise.locks.common.init.LocksAttachments;
import melonslise.locks.common.init.LocksItemTags;
import melonslise.locks.common.init.LocksItems;
import melonslise.locks.common.init.LocksSoundEvents;
import melonslise.locks.common.item.KeyRingItem;
import melonslise.locks.common.item.LockingItem;
import melonslise.locks.common.util.Lockable;
import melonslise.locks.common.util.LocksPredicates;
import melonslise.locks.common.util.LocksUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.neoforged.neoforge.event.village.WandererTradesEvent;

/*
 * Порт LocksForgeEvents (этап 13). Что УБРАНО по сравнению с оригиналом:
 *  - attachCapabilitiesTo{World,Chunk,Entity}  -> Data Attachments (этап 4)
 *  - onBiomeLoad (LocksConfiguredFeatures.addTo) -> biome modifier JSON (этап 11)
 *  - onLootTableLoad                            -> Global Loot Modifier JSON (этап 12)
 *  - onChunkUnload                              -> LocksHandlerEvents (этап 4, ChunkEvent.Unload)
 * Игровая шина (EventBusSubscriber по умолчанию).
 */
@EventBusSubscriber(modid = Locks.ID)
public final class LocksForgeEvents
{
	public static final Component LOCKED_MESSAGE = Component.translatable(Locks.ID + ".status.locked");

	private LocksForgeEvents() {}

	// В 1.21.1 VillagerTrades.ItemsForEmeralds / EnchantedItemForEmeralds не public -> свои ItemListing с той же логикой (без AT)
	private static VillagerTrades.ItemListing itemsForEmeralds(Item item, int emeraldCost, int count, int maxUses, int xp, float priceMult)
	{
		return (trader, rng) -> new MerchantOffer(new ItemCost(Items.EMERALD, emeraldCost), new ItemStack(item, count), maxUses, xp, priceMult);
	}

	// Как ванильный EnchantedItemForEmeralds: случайный уровень зачарования 5..19, цена = базовая + уровень (не больше 64)
	private static VillagerTrades.ItemListing enchantedItemForEmeralds(Item item, int baseCost, int maxUses, int xp)
	{
		return (trader, rng) ->
		{
			int level = 5 + rng.nextInt(15);
			RegistryAccess access = trader.level().registryAccess();
			ItemStack stack = EnchantmentHelper.enchantItem(rng, new ItemStack(item), level, access, access.registryOrThrow(Registries.ENCHANTMENT).getTag(EnchantmentTags.ON_TRADED_EQUIPMENT));
			return new MerchantOffer(new ItemCost(Items.EMERALD, Math.min(baseCost + level, 64)), stack, maxUses, xp, 0.05f);
		};
	}

	@SubscribeEvent
	public static void addVillagerTrades(VillagerTradesEvent e)
	{
		if(e.getType() != VillagerProfession.TOOLSMITH)
			return;
		Int2ObjectMap<List<VillagerTrades.ItemListing>> levels = e.getTrades();
		List<VillagerTrades.ItemListing> trades;
		trades = levels.get(1);
		trades.add(itemsForEmeralds(LocksItems.WOOD_LOCK_PICK.get(), 1, 2, 16, 2, 0.05f));
		trades.add(itemsForEmeralds(LocksItems.WOOD_LOCK_MECHANISM.get(), 2, 1, 12, 1, 0.2f));
		trades = levels.get(2);
		trades.add(itemsForEmeralds(LocksItems.IRON_LOCK_PICK.get(), 2, 2, 16, 5, 0.05f));
		trades = levels.get(3);
		trades.add(itemsForEmeralds(LocksItems.GOLD_LOCK_PICK.get(), 6, 2, 12, 20, 0.05f));
		trades.add(itemsForEmeralds(LocksItems.IRON_LOCK_MECHANISM.get(), 5, 1, 8, 10, 0.2f));
		trades = levels.get(4);
		trades.add(itemsForEmeralds(LocksItems.STEEL_LOCK_PICK.get(), 4, 2, 16, 20, 0.05f));
		trades = levels.get(5);
		trades.add(itemsForEmeralds(LocksItems.DIAMOND_LOCK_PICK.get(), 8, 2, 12, 30, 0.05f));
		trades.add(itemsForEmeralds(LocksItems.STEEL_LOCK_MECHANISM.get(), 8, 1, 8, 30, 0.2f));
	}

	@SubscribeEvent
	public static void addWandererTrades(WandererTradesEvent e)
	{
		List<VillagerTrades.ItemListing> trades;
		trades = e.getGenericTrades();
		trades.add(itemsForEmeralds(LocksItems.GOLD_LOCK_PICK.get(), 5, 2, 6, 1, 0.05f));
		trades.add(itemsForEmeralds(LocksItems.STEEL_LOCK_PICK.get(), 3, 2, 8, 1, 0.05f));
		// Enchanted lock trades pick enchantments from the minecraft:on_traded_equipment tag (our enchantments are added to it in the datapack)
		trades.add(enchantedItemForEmeralds(LocksItems.STEEL_LOCK.get(), 16, 4, 1));
		trades = e.getRareTrades();
		trades.add(itemsForEmeralds(LocksItems.STEEL_LOCK_MECHANISM.get(), 6, 1, 4, 1, 0.05f));
		trades.add(enchantedItemForEmeralds(LocksItems.DIAMOND_LOCK.get(), 28, 4, 1));
	}

	@SubscribeEvent
	public static void onRightClick(PlayerInteractEvent.RightClickBlock e)
	{
		BlockPos pos = e.getPos();
		Level level = e.getLevel();
		Player player = e.getEntity();
		LockableHandler handler = LockableHandler.get(level);
		Lockable[] intersect = handler.getInChunk(pos).values().stream().filter(lkb -> lkb.bb.intersects(pos)).toArray(Lockable[]::new);
		if(intersect.length == 0)
			return;
		if(e.getHand() != InteractionHand.MAIN_HAND) // FIXME (original) Better way to prevent firing multiple times
		{
			e.setUseBlock(TriState.FALSE);
			return;
		}
		ItemStack stack = e.getItemStack();
		Optional<Lockable> locked = Arrays.stream(intersect).filter(LocksPredicates.LOCKED).findFirst();
		if(locked.isPresent())
		{
			Lockable lkb = locked.get();
			e.setUseBlock(TriState.FALSE);
			// FIXME (original) hard coded list of items that can interact with a locked block
			if(!stack.is(LocksItemTags.LOCK_PICKS) && !stack.is(LocksItems.MASTER_KEY.get())
				&& (!stack.is(LocksItemTags.KEYS) || LockingItem.getOrSetId(stack) != lkb.lock.id)
				&& (!stack.is(LocksItems.KEY_RING.get()) || !KeyRingItem.containsId(stack, lkb.lock.id)))
			{
				lkb.swing(20);
				level.playSound(player, pos, LocksSoundEvents.LOCK_RATTLE.get(), SoundSource.BLOCKS, 1f, 1f);
			}
			player.swing(InteractionHand.MAIN_HAND);
			if(level.isClientSide && LocksClientConfig.DEAF_MODE.get())
				player.displayClientMessage(LOCKED_MESSAGE, true);
			return;
		}
		if(LocksServerConfig.ALLOW_REMOVING_LOCKS.get() && player.isShiftKeyDown() && stack.isEmpty())
		{
			Lockable[] match = Arrays.stream(intersect).filter(LocksPredicates.NOT_LOCKED).toArray(Lockable[]::new);
			if(match.length == 0)
				return;
			e.setUseBlock(TriState.FALSE);
			level.playSound(player, pos, SoundEvents.ITEM_BREAK, SoundSource.BLOCKS, 0.8f, 0.8f + level.random.nextFloat() * 0.4f);
			player.swing(InteractionHand.MAIN_HAND);
			if(!level.isClientSide)
				for(Lockable lkb : match)
				{
					level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5d, pos.getY() + 0.5d, pos.getZ() + 0.5d, lkb.stack));
					handler.remove(lkb.id);
				}
		}
	}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Pre e)
	{
		// Selection = attachment on the player (stage 4); API (get()/set()) kept as in the original ISelection
		var select = e.getEntity().getData(LocksAttachments.SELECTION);
		if(select.get() == null)
			return;
		for(ItemStack stack : e.getEntity().getHandSlots())
			if(stack.is(LocksItemTags.LOCKS))
				return;
		select.set(null);
	}

	public static boolean canBreakLockable(Player player, BlockPos pos)
	{
		return !LocksServerConfig.PROTECT_LOCKABLES.get() || player.isCreative() || !LocksUtil.locked(player.level(), pos);
	}

	@SubscribeEvent
	public static void onBlockBreaking(PlayerEvent.BreakSpeed e)
	{
		// 1.21: BreakSpeed#getPosition is Optional<BlockPos> (empty when the block position is unknown)
		e.getPosition().ifPresent(pos ->
		{
			if(!canBreakLockable(e.getEntity(), pos))
				e.setCanceled(true);
		});
	}

	@SubscribeEvent
	public static void onBlockBreak(BlockEvent.BreakEvent e)
	{
		if(!canBreakLockable(e.getPlayer(), e.getPos()))
			e.setCanceled(true);
	}
}
