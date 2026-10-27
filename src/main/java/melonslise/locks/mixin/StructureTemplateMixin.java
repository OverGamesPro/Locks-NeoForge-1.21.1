package melonslise.locks.mixin;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import melonslise.locks.Locks;
import melonslise.locks.common.capability.LockableHandler;
import melonslise.locks.common.config.LocksConfig;
import melonslise.locks.common.util.Cuboid6i;
import melonslise.locks.common.util.Lock;
import melonslise.locks.common.util.Lockable;
import melonslise.locks.common.util.LockableInfo;
import melonslise.locks.common.util.LocksUtil;
import melonslise.locks.common.util.Transform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

// Saves/loads/places the locks of structures (was TemplateMixin). Lock data is stored in the structure NBT under "Lockables".
@Mixin(StructureTemplate.class)
public class StructureTemplateMixin
{
	@Unique
	private final List<LockableInfo> locks$lockableInfos = new ArrayList<>();

	@Unique
	private static final String LOCKS$KEY_LOCKABLES = "Lockables";

	// Structure templates have no access to a registry provider; ItemStack (de)serialization needs one -> use the running server's
	@Unique
	@Nullable
	private static HolderLookup.Provider locks$registries()
	{
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		return server == null ? null : server.registryAccess();
	}

	@Inject(at = @At("HEAD"), method = "fillFromWorld")
	private void fillFromWorld(Level level, BlockPos start, Vec3i size, boolean takeEntities, @Nullable Block toIgnore, CallbackInfo ci)
	{
		if(size.getX() >= 1 && size.getY() >= 1 && size.getZ() >= 1)
		{
			this.locks$lockableInfos.clear();
			LockableHandler handler = LockableHandler.get(level);
			Cuboid6i bb = new Cuboid6i(start, start.offset(size.getX() - 1, size.getY() - 1, size.getZ() - 1));
			handler.getLoaded().values().stream()
				.filter(lkb -> lkb.bb.intersects(bb))
				.forEach(lkb ->
				{
					Cuboid6i newBB = bb.intersection(lkb.bb).offset(-start.getX(), -start.getY(), -start.getZ());
					this.locks$lockableInfos.add(new LockableInfo(newBB, lkb.lock, lkb.tr, lkb.stack, lkb.id));
				});
		}
	}

	// Original injected at "RETURN ordinal 1" (the successful return); here: every return, only act when placement succeeded
	@Inject(at = @At("RETURN"), method = "placeInWorld")
	private void placeInWorld(ServerLevelAccessor world, BlockPos start, BlockPos size, StructurePlaceSettings settings, RandomSource rng, int flags, CallbackInfoReturnable<Boolean> cir)
	{
		if(!cir.getReturnValueZ() || this.locks$lockableInfos.isEmpty())
			return;
		Level level;
		try
		{
			level = world.getLevel();
		}
		catch(Exception e)
		{
			Locks.LOGGER.warn(world + "#getLevel threw an error! Skipping lockable placement for this template");
			return;
		}
		LockableHandler handler = LockableHandler.get(level);
		for(LockableInfo lkb : this.locks$lockableInfos)
		{
			BlockPos pos1 = LocksUtil.transform(lkb.bb.x1, lkb.bb.y1, lkb.bb.z1, settings);
			BlockPos pos2 = LocksUtil.transform(lkb.bb.x2, lkb.bb.y2, lkb.bb.z2, settings);
			Cuboid6i bb = new Cuboid6i(pos1.getX() + start.getX(), pos1.getY() + start.getY(), pos1.getZ() + start.getZ(), pos2.getX() + start.getX(), pos2.getY() + start.getY(), pos2.getZ() + start.getZ());
			ItemStack stack = lkb.stack;
			Lock lock;
			if(LocksConfig.RANDOMIZE_LOADED_LOCKS.get())
			{
				ItemStack random = LocksConfig.getRandomLock(rng, level.registryAccess());
				if(!random.isEmpty())
					stack = random;
			}
			// Fresh Lock per placement (the original shared one Lock instance between all placed copies of a template)
			lock = LocksConfig.RANDOMIZE_LOADED_LOCKS.get() && stack != lkb.stack ? Lock.from(stack) : new Lock(lkb.lock.id, lkb.lock.getLength(), lkb.lock.isLocked());
			Transform tr = Transform.fromDirectionAndFace(settings.getRotation().rotate(settings.getMirror().getRotation(lkb.tr.dir).rotate(lkb.tr.dir)), lkb.tr.face, Direction.NORTH);
			handler.add(new Lockable(bb, lock, tr, stack, handler.nextId()));
		}
	}

	@Inject(at = @At("HEAD"), method = "save")
	private void save(CompoundTag nbt, CallbackInfoReturnable<CompoundTag> cir)
	{
		HolderLookup.Provider registries = locks$registries();
		if(registries == null)
			return;
		ListTag list = new ListTag();
		for(LockableInfo lkb : this.locks$lockableInfos)
			list.add(LockableInfo.toNbt(lkb, registries));
		nbt.put(LOCKS$KEY_LOCKABLES, list);
	}

	@Inject(at = @At("HEAD"), method = "load")
	private void read(HolderGetter<Block> blocks, CompoundTag nbt, CallbackInfo ci)
	{
		this.locks$lockableInfos.clear();
		HolderLookup.Provider registries = locks$registries();
		if(registries == null)
			return;
		ListTag list = nbt.getList(LOCKS$KEY_LOCKABLES, Tag.TAG_COMPOUND);
		for(int a = 0, b = list.size(); a < b; ++a)
			this.locks$lockableInfos.add(LockableInfo.fromNbt(list.getCompound(a), registries));
	}
}
