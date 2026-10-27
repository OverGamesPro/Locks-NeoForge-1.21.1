package melonslise.locks.common.init;

import melonslise.locks.Locks;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

/*
 * В 1.21 зачарования - datapack JSON (data/locks/enchantment/*.json, этап 8).
 * Здесь только ключи + хелпер для чтения уровня.
 */
public final class LocksEnchantments
{
	public static final ResourceKey<Enchantment>
		SHOCKING = key("shocking"),
		STURDY = key("sturdy"),
		COMPLEXITY = key("complexity");

	private LocksEnchantments() {}

	public static ResourceKey<Enchantment> key(String name)
	{
		return ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(Locks.ID, name));
	}

	// 0, если зачарование не зарегистрировано (datapack отключён/ещё не портирован) - не падаем
	public static int getLevel(Level level, ResourceKey<Enchantment> key, ItemStack stack)
	{
		return level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolder(key).map(h -> EnchantmentHelper.getItemEnchantmentLevel((Holder<Enchantment>) h, stack)).orElse(0);
	}
}
