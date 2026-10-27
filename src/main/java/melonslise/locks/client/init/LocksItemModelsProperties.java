package melonslise.locks.client.init;

import melonslise.locks.Locks;
import melonslise.locks.common.init.LocksItems;
import melonslise.locks.common.item.KeyRingItem;
import melonslise.locks.common.item.LockItem;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

// Свойства моделей: locks:keys (связка ключей) и locks:open (замки). JSON-модели с overrides уже лежат в assets/locks/models/item.
// Регистрируется сам на MOD-шине (FMLClientSetupEvent), поэтому LocksClientModEvents.onSetup менять не нужно.
@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = Locks.ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class LocksItemModelsProperties
{
	private LocksItemModelsProperties() {}

	@SubscribeEvent
	public static void onSetup(FMLClientSetupEvent e)
	{
		e.enqueueWork(LocksItemModelsProperties::register);
	}

	public static void register()
	{
		ItemProperties.register(LocksItems.KEY_RING.get(), ResourceLocation.fromNamespaceAndPath(Locks.ID, "keys"), (stack, level, entity, seed) ->
		{
			var inv = KeyRingItem.inventory(stack);
			int slots = inv.getSlots();
			if(slots == 0)
				return 0f;
			int keys = 0;
			for(int a = 0; a < slots; ++a)
				if(!inv.getStackInSlot(a).isEmpty())
					++keys;
			return (float) keys / slots;
		});

		ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Locks.ID, "open");
		ClampedItemPropertyFunction getter = (stack, level, entity, seed) -> LockItem.isOpen(stack) ? 1f : 0f;
		ItemProperties.register(LocksItems.WOOD_LOCK.get(), id, getter);
		ItemProperties.register(LocksItems.IRON_LOCK.get(), id, getter);
		ItemProperties.register(LocksItems.STEEL_LOCK.get(), id, getter);
		ItemProperties.register(LocksItems.GOLD_LOCK.get(), id, getter);
		ItemProperties.register(LocksItems.DIAMOND_LOCK.get(), id, getter);
	}
}
