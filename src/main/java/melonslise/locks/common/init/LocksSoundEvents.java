package melonslise.locks.common.init;

import java.util.function.Supplier;

import melonslise.locks.Locks;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class LocksSoundEvents
{
	public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, Locks.ID);

	public static final Supplier<SoundEvent>
		KEY_RING = add("key_ring"),
		LOCK_CLOSE = add("lock.close"),
		LOCK_OPEN = add("lock.open"),
		LOCK_RATTLE = add("lock.rattle"),
		PIN_FAIL = add("pin.fail"),
		PIN_MATCH = add("pin.match"),
		SHOCK = add("shock");

	private LocksSoundEvents() {}

	public static Supplier<SoundEvent> add(String name)
	{
		return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Locks.ID, name)));
	}
}
