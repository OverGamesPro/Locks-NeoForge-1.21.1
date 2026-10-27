package melonslise.locks.common.event;

import melonslise.locks.Locks;
import melonslise.locks.common.util.LocksUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.wrapper.EmptyItemHandler;

/*
 * Replaces AbstractFurnaceTileEntityMixin, ChestTileEntityMixin, LockableTileEntityMixin (getCapability(ITEM_HANDLER) -> empty when locked)
 * and HopperTileEntityMixin (NeoForge hoppers go through the item handler capability too).
 * A provider registered with HIGHEST priority is asked first; it returns an empty handler for locked containers and null (= ask the next provider) otherwise.
 */
@EventBusSubscriber(modid = Locks.ID, bus = EventBusSubscriber.Bus.MOD)
public final class LocksCapabilityEvents
{
	private LocksCapabilityEvents() {}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onRegisterCapabilities(RegisterCapabilitiesEvent e)
	{
		for(BlockEntityType<?> type : BuiltInRegistries.BLOCK_ENTITY_TYPE)
			register(e, type);
	}

	// helper to capture the wildcard type
	private static <BE extends BlockEntity> void register(RegisterCapabilitiesEvent e, BlockEntityType<BE> type)
	{
		e.registerBlockEntity(Capabilities.ItemHandler.BLOCK, type, (be, side) ->
			!be.isRemoved() && be instanceof BaseContainerBlockEntity && be.hasLevel() && LocksUtil.locked(be.getLevel(), be.getBlockPos()) ? EmptyItemHandler.INSTANCE : null);
	}
}
