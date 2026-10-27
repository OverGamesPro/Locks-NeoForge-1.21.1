package melonslise.locks.client.gui.sprite.action;

import melonslise.locks.client.gui.sprite.Sprite;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@SuppressWarnings({"unchecked", "rawtypes"})
public class WaitAction<S extends Sprite> extends TimedAction<S>
{
	public static <Z extends Sprite> WaitAction<Z> ticks(int ticks)
	{
		return (WaitAction<Z>) new WaitAction().time(ticks);
	}
}