package melonslise.locks.common.config;

import java.util.List;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;

import com.google.common.collect.Lists;

import melonslise.locks.Locks;
import melonslise.locks.common.util.LocksUtil;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class LocksConfig
{
	public static final ModConfigSpec SPEC;

	public static final ModConfigSpec.DoubleValue GENERATION_CHANCE;
	public static final ModConfigSpec.DoubleValue GENERATION_ENCHANT_CHANCE;
	public static final ModConfigSpec.ConfigValue<List<? extends String>> GENERATED_LOCKS;
	public static final ModConfigSpec.ConfigValue<List<? extends Integer>> GENERATED_LOCK_WEIGHTS;
	public static final ModConfigSpec.BooleanValue RANDOMIZE_LOADED_LOCKS;

	// Built lazily (config is loaded before registries may be ready) and dropped on (re)load by LocksConfigEvents
	private static volatile NavigableMap<Integer, Item> weightedGeneratedLocks;
	private static volatile int weightTotal;

	static
	{
		ModConfigSpec.Builder cfg = new ModConfigSpec.Builder();

		GENERATION_CHANCE = cfg
			.comment("Chance to generate a random lock on every new chest during world generation. Set to 0 to disable")
			.defineInRange("Generation Chance", 0.85d, 0d, 1d);
		GENERATION_ENCHANT_CHANCE = cfg
			.comment("Chance to randomly enchant a generated lock during world generation. Set to 0 to disable")
			.defineInRange("Generation Enchant Chance", 0.4d, 0d, 1d);
		GENERATED_LOCKS = cfg
			.comment("Items that can be generated as locks (must be instance of LockItem in code!)")
			.defineList("Generated Locks", Lists.newArrayList("locks:wood_lock", "locks:iron_lock", "locks:steel_lock", "locks:gold_lock", "locks:diamond_lock"), e -> e instanceof String);
		GENERATED_LOCK_WEIGHTS = cfg
			.comment("WARNING: THE AMOUNT OF NUMBERS SHOULD BE EQUAL TO THE AMOUNT OF GENERATED LOCK ITEMS!!!", "The relative probability that the corresponding lock item will be generated on a chest. Higher number = higher chance to generate")
			.defineList("Generated Lock Chances", Lists.newArrayList(3, 3, 2, 2, 1), e -> e instanceof Integer);
		RANDOMIZE_LOADED_LOCKS = cfg
			.comment("Randomize lock IDs and combinations when loading them from a structure file. Randomization works just like during world generation")
			.define("Randomize Loaded Locks", false);

		SPEC = cfg.build();
	}

	private LocksConfig() {}

	// Called by LocksConfigEvents on load/reload: forces rebuild of the weight table on next use
	public static void init()
	{
		weightedGeneratedLocks = null;
	}

	// https://gist.github.com/raws/1667807
	private static synchronized void build()
	{
		NavigableMap<Integer, Item> map = new TreeMap<>();
		int total = 0;
		List<? extends String> locks = GENERATED_LOCKS.get();
		List<? extends Integer> weights = GENERATED_LOCK_WEIGHTS.get();
		for(int a = 0; a < locks.size(); ++a)
		{
			ResourceLocation id = ResourceLocation.tryParse(locks.get(a));
			Optional<Item> item = id == null ? Optional.empty() : BuiltInRegistries.ITEM.getOptional(id);
			if(item.isEmpty())
			{
				Locks.LOGGER.warn("Unknown generated lock item in config: {}", locks.get(a));
				continue;
			}
			int weight = a < weights.size() ? Math.max(weights.get(a), 0) : 1;
			if(weight == 0)
				continue;
			total += weight;
			map.put(total, item.get());
		}
		weightTotal = total;
		weightedGeneratedLocks = map;
	}

	public static boolean canGen(RandomSource rng)
	{
		return LocksUtil.chance(rng, GENERATION_CHANCE.get());
	}

	public static boolean canEnchant(RandomSource rng)
	{
		return LocksUtil.chance(rng, GENERATION_ENCHANT_CHANCE.get());
	}

	// 1.21: enchanting needs a RegistryAccess (enchantments are datapack registry entries). Returns EMPTY if no valid lock item is configured.
	public static ItemStack getRandomLock(RandomSource rng, RegistryAccess registries)
	{
		if(weightedGeneratedLocks == null)
			build();
		NavigableMap<Integer, Item> map = weightedGeneratedLocks;
		if(map.isEmpty())
			return ItemStack.EMPTY;
		ItemStack stack = new ItemStack(map.ceilingEntry(rng.nextInt(weightTotal) + 1).getValue());
		return canEnchant(rng) ? EnchantmentHelper.enchantItem(rng, stack, 5 + rng.nextInt(30), registries, Optional.empty()) : stack;
	}
}
