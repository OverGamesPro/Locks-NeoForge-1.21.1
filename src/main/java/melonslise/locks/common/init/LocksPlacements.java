package melonslise.locks.common.init;

import melonslise.locks.Locks;
import melonslise.locks.common.worldgen.ChestPlacement;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/* Регистрация: LocksPlacements.PLACEMENTS.register(modBus) в конструкторе Locks. */
public final class LocksPlacements
{
	public static final DeferredRegister<PlacementModifierType<?>> PLACEMENTS = DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, Locks.ID);

	public static final DeferredHolder<PlacementModifierType<?>, PlacementModifierType<ChestPlacement>>
		CHEST = PLACEMENTS.register("chest", () -> () -> ChestPlacement.CODEC);

	private LocksPlacements() {}
}
