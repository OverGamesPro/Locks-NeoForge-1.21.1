package melonslise.locks.client.event;

import melonslise.locks.Locks;
import melonslise.locks.client.init.LocksScreens;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

// MOD-bus client events. If `bus = EventBusSubscriber.Bus.MOD` is rejected by the NeoForge version in use (deprecated in later 21.x),
// drop the parameter - events implementing IModBusEvent are auto-routed to the mod bus.
@EventBusSubscriber(modid = Locks.ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class LocksClientModEvents
{
	private LocksClientModEvents() {}

	@SubscribeEvent
	public static void onRegisterScreens(RegisterMenuScreensEvent e)
	{
		LocksScreens.register(e);
	}

	@SubscribeEvent
	public static void onSetup(FMLClientSetupEvent e)
	{
		// Свойства моделей предметов регистрирует сам LocksItemModelsProperties (свой подписчик на FMLClientSetupEvent) - здесь ничего не нужно
	}
}
