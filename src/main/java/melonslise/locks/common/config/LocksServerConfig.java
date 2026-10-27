package melonslise.locks.common.config;

import java.util.List;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.ModConfigSpec;

public class LocksServerConfig
{
	public static final ModConfigSpec SPEC;

	public static final ModConfigSpec.IntValue MAX_LOCKABLE_VOLUME;
	public static final ModConfigSpec.ConfigValue<List<? extends String>> LOCKABLE_BLOCKS;
	public static final ModConfigSpec.BooleanValue ALLOW_REMOVING_LOCKS;
	public static final ModConfigSpec.BooleanValue PROTECT_LOCKABLES;
	public static final ModConfigSpec.BooleanValue EASY_LOCK;

	// Compiled lazily (and dropped on config (re)load by LocksConfigEvents) - config values must not be read too early
	private static volatile Pattern[] lockableBlocks;

	static
	{
		ModConfigSpec.Builder cfg = new ModConfigSpec.Builder();

		MAX_LOCKABLE_VOLUME = cfg
			.comment("Maximum amount of blocks that can be locked at once")
			.defineInRange("Max Lockable Volume", 6, 1, Integer.MAX_VALUE);
		LOCKABLE_BLOCKS = cfg
			.comment("Blocks that can be locked. Each entry is the mod domain followed by the block's registry name. Can include regular expressions")
			.defineList("Lockable Blocks", Lists.newArrayList(".*chest", ".*barrel", ".*hopper", ".*door", ".*trapdoor", ".*fence_gate", ".*shulker_box"), e -> e instanceof String);
		ALLOW_REMOVING_LOCKS = cfg
			.comment("Open locks can be removed with an empty hand while sneaking")
			.define("Allow Removing Locks", true);
		PROTECT_LOCKABLES = cfg
			.comment("Locked blocks cannot be destroyed in survival mode")
			.define("Protect Lockables", true);
		EASY_LOCK = cfg
			.comment("Lock blocks with just one click! It's magic! (Will probably fail spectacularly with custom doors, custom double chests, etc)")
			.define("Easy Lock", true);

		SPEC = cfg.build();
	}

	private LocksServerConfig() {}

	// Called by LocksConfigEvents on load/reload: forces recompilation on next use
	public static void init()
	{
		lockableBlocks = null;
	}

	private static Pattern[] patterns()
	{
		Pattern[] p = lockableBlocks;
		if(p == null)
			lockableBlocks = p = LOCKABLE_BLOCKS.get().stream().map(Pattern::compile).toArray(Pattern[]::new);
		return p;
	}

	public static boolean canLock(Level level, BlockPos pos)
	{
		String name = BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock()).toString();
		for(Pattern p : patterns())
			if(p.matcher(name).matches())
				return true;
		return false;
	}
}
