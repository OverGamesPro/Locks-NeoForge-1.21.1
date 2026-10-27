package melonslise.locks.common.init;

import java.util.function.Supplier;

import com.mojang.serialization.Codec;

import melonslise.locks.Locks;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredRegister;

/*
 * Замена NBT-ключей предметов оригинала:
 *   "Id" -> ID, "Length" -> LENGTH, "Open" -> OPEN, "Strength" -> STRENGTH.
 * Регистрация: LocksDataComponents.COMPONENTS.register(modEventBus) в конструкторе Locks.
 */
public final class LocksDataComponents
{
	public static final DeferredRegister<DataComponentType<?>> COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, Locks.ID);

	// id замка/ключа (связывает ключ и замок)
	public static final Supplier<DataComponentType<Integer>> ID = COMPONENTS.register("id",
		() -> DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT).build());

	// длина замка (кол-во пинов)
	public static final Supplier<DataComponentType<Integer>> LENGTH = COMPONENTS.register("length",
		() -> DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT).build());

	// открыт ли замок (для модели open_*_lock)
	public static final Supplier<DataComponentType<Boolean>> OPEN = COMPONENTS.register("open",
		() -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build());

	// сила отмычки
	public static final Supplier<DataComponentType<Float>> STRENGTH = COMPONENTS.register("strength",
		() -> DataComponentType.<Float>builder().persistent(Codec.FLOAT).networkSynchronized(ByteBufCodecs.FLOAT).build());

	private LocksDataComponents() {}
}
