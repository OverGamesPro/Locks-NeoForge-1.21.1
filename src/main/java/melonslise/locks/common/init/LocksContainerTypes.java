package melonslise.locks.common.init;

import melonslise.locks.Locks;
import melonslise.locks.common.container.KeyRingContainer;
import melonslise.locks.common.container.LockPickingContainer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/*
 * Регистрация: LocksContainerTypes.CONTAINER_TYPES.register(modBus) в конструкторе Locks.
 */
public final class LocksContainerTypes
{
	public static final DeferredRegister<MenuType<?>> CONTAINER_TYPES = DeferredRegister.create(Registries.MENU, Locks.ID);

	public static final DeferredHolder<MenuType<?>, MenuType<KeyRingContainer>>
		KEY_RING = CONTAINER_TYPES.register("key_ring", () -> IMenuTypeExtension.create(KeyRingContainer.FACTORY));

	public static final DeferredHolder<MenuType<?>, MenuType<LockPickingContainer>>
		LOCK_PICKING = CONTAINER_TYPES.register("lock_picking", () -> IMenuTypeExtension.create(LockPickingContainer.FACTORY));

	private LocksContainerTypes() {}
}
