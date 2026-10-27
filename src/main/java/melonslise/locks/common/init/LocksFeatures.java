package melonslise.locks.common.init;

import melonslise.locks.Locks;
import melonslise.locks.common.worldgen.LockChestsFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/* Регистрация: LocksFeatures.FEATURES.register(modBus) в конструкторе Locks. */
public final class LocksFeatures
{
	public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, Locks.ID);

	public static final DeferredHolder<Feature<?>, LockChestsFeature>
		LOCK_CHESTS = FEATURES.register("lock_chests", () -> new LockChestsFeature(NoneFeatureConfiguration.CODEC));

	private LocksFeatures() {}
}
