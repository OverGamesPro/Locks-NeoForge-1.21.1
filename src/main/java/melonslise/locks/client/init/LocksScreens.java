package melonslise.locks.client.init;

import melonslise.locks.client.gui.KeyRingScreen;
import melonslise.locks.client.gui.LockPickingScreen;
import melonslise.locks.common.init.LocksContainerTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@OnlyIn(Dist.CLIENT)
public final class LocksScreens
{
	private LocksScreens() {}

	// 1.21: ScreenManager.register -> RegisterMenuScreensEvent (mod bus)
	public static void register(RegisterMenuScreensEvent e)
	{
		e.register(LocksContainerTypes.LOCK_PICKING.get(), LockPickingScreen::new);
		e.register(LocksContainerTypes.KEY_RING.get(), KeyRingScreen::new);
	}
}
