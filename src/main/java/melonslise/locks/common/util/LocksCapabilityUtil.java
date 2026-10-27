package melonslise.locks.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public final class LocksCapabilityUtil
{
	private LocksCapabilityUtil() {}

	// Item handler capabilities of locked blocks depend on lock state (see LocksCapabilityEvents) -> caches of pipes/hoppers must be invalidated
	// whenever a lockable is added, removed or (un)locked. Call from LockableHandler.add/remove/update (server side).
	public static void invalidate(Level level, Lockable lkb)
	{
		if(level.isClientSide)
			return;
		for(BlockPos pos : lkb.bb.getContainedPos())
			level.invalidateCapabilities(pos);
	}
}
