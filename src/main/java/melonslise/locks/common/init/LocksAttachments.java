package melonslise.locks.common.init;

import java.util.function.Supplier;

import melonslise.locks.Locks;
import melonslise.locks.common.capability.LockableStorage;
import melonslise.locks.common.capability.Selection;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/*
 * Замена LocksCapabilities. Регистрация: LocksAttachments.ATTACHMENTS.register(modEventBus) в конструкторе Locks.
 */
public final class LocksAttachments
{
	public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Locks.ID);

	// На LevelChunk: замки чанка, сохраняются в NBT чанка
	public static final Supplier<AttachmentType<LockableStorage>> LOCKABLE_STORAGE = ATTACHMENTS.register("lockable_storage",
		() -> AttachmentType.builder(holder -> new LockableStorage(holder)).serialize(LockableStorage.SERIALIZER).build());

	// На Player: выбранная позиция, не сохраняется
	public static final Supplier<AttachmentType<Selection>> SELECTION = ATTACHMENTS.register("selection",
		() -> AttachmentType.builder(() -> new Selection()).build());

	private LocksAttachments() {}
}
