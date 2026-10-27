package melonslise.locks.common.init;

import melonslise.locks.Locks;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.Level;

/*
 * В 1.21 DamageType - datapack: data/locks/damage_type/shock.json
 * + тег data/minecraft/tags/damage_type/bypasses_armor.json (замена .bypassArmor()).
 */
public final class LocksDamageSources
{
	public static final ResourceKey<DamageType> SHOCK = ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(Locks.ID, "shock"));

	private LocksDamageSources() {}

	public static DamageSource shock(Level level)
	{
		return level.damageSources().source(SHOCK);
	}
}
