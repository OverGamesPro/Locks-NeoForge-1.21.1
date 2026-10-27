package melonslise.locks.common.event;

import melonslise.locks.Locks;
import melonslise.locks.common.util.LocksUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.PistonEvent;

/*
 * Replaces PistonBlockMixin (isPushable) and PistonBlockStructureHelperMixin (resolve): a piston cannot move a structure containing a locked block.
 */
@EventBusSubscriber(modid = Locks.ID)
public final class LocksProtectionEvents
{
	private LocksProtectionEvents() {}

	@SubscribeEvent
	public static void onPistonMove(PistonEvent.Pre e)
	{
		if(!(e.getLevel() instanceof Level level))
			return;
		PistonStructureResolver helper = e.getStructureHelper();
		if(helper == null || !helper.resolve())
			return;
		for(BlockPos pos : helper.getToPush())
			if(LocksUtil.locked(level, pos))
			{
				e.setCanceled(true);
				return;
			}
	}
}
