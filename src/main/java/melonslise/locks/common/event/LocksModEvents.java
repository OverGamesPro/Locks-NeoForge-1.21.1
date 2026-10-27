package melonslise.locks.common.event;

import melonslise.locks.Locks;
import melonslise.locks.common.capability.KeyRingInventory;
import melonslise.locks.common.init.LocksItems;
import melonslise.locks.common.item.KeyRingItem;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(modid = Locks.ID, bus = EventBusSubscriber.Bus.MOD)
public final class LocksModEvents
{
	private LocksModEvents() {}

	@SubscribeEvent
	public static void registerCapabilities(RegisterCapabilitiesEvent event)
	{
		event.registerItem(Capabilities.ItemHandler.ITEM, (stack, ctx) -> new KeyRingInventory(stack, ((KeyRingItem) stack.getItem()).rows, KeyRingItem.COLUMNS), LocksItems.KEY_RING.get());
	}
}
