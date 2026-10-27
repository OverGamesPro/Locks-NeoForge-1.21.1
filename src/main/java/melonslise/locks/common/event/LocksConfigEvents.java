package melonslise.locks.common.event;

import melonslise.locks.Locks;
import melonslise.locks.common.config.LocksConfig;
import melonslise.locks.common.config.LocksServerConfig;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;

// Replaces LocksModEvents.onConfigLoad (ModConfig.ModConfigEvent) - REMOVE that method from LocksModEvents.
@EventBusSubscriber(modid = Locks.ID, bus = EventBusSubscriber.Bus.MOD)
public final class LocksConfigEvents
{
	private LocksConfigEvents() {}

	@SubscribeEvent
	public static void onLoad(ModConfigEvent.Loading e)
	{
		refresh(e.getConfig());
	}

	@SubscribeEvent
	public static void onReload(ModConfigEvent.Reloading e)
	{
		refresh(e.getConfig());
	}

	private static void refresh(ModConfig cfg)
	{
		if(cfg.getSpec() == LocksConfig.SPEC)
			LocksConfig.init();
		if(cfg.getSpec() == LocksServerConfig.SPEC)
			LocksServerConfig.init();
	}
}
