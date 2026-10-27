package melonslise.locks;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import melonslise.locks.common.config.LocksClientConfig;
import melonslise.locks.common.config.LocksConfig;
import melonslise.locks.common.config.LocksServerConfig;
import melonslise.locks.common.init.LocksAttachments;
import melonslise.locks.common.init.LocksContainerTypes;
import melonslise.locks.common.init.LocksDataComponents;
import melonslise.locks.common.init.LocksFeatures;
import melonslise.locks.common.init.LocksItems;
import melonslise.locks.common.init.LocksPlacements;
import melonslise.locks.common.init.LocksRecipeSerializers;
import melonslise.locks.common.init.LocksSoundEvents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

// Locks (c) Melonslise, CC BY-NC 3.0. NeoForge 1.21.1 port.
// Подписчики событий (@EventBusSubscriber) регистрируются сами, здесь только DeferredRegister'ы и конфиги.
@Mod(Locks.ID)
public final class Locks
{
	public static final String ID = "locks";
	public static final Logger LOGGER = LogUtils.getLogger();

	public Locks(IEventBus modBus, ModContainer container)
	{
		// Конфиги (бывший Locks.java: registerConfig)
		container.registerConfig(ModConfig.Type.SERVER, LocksServerConfig.SPEC);
		container.registerConfig(ModConfig.Type.COMMON, LocksConfig.SPEC);
		container.registerConfig(ModConfig.Type.CLIENT, LocksClientConfig.SPEC);

		// Реестры
		LocksDataComponents.COMPONENTS.register(modBus);
		LocksSoundEvents.SOUND_EVENTS.register(modBus);
		LocksItems.ITEMS.register(modBus);
		LocksItems.CREATIVE_TABS.register(modBus);
		LocksContainerTypes.CONTAINER_TYPES.register(modBus);
		LocksRecipeSerializers.RECIPE_SERIALIZERS.register(modBus);
		LocksAttachments.ATTACHMENTS.register(modBus);
		LocksFeatures.FEATURES.register(modBus);
		LocksPlacements.PLACEMENTS.register(modBus);
	}
}
