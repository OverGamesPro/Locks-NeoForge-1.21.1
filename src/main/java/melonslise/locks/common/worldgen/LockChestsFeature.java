package melonslise.locks.common.worldgen;

import com.mojang.serialization.Codec;

import melonslise.locks.common.capability.LockableHandler;
import melonslise.locks.common.config.LocksConfig;
import melonslise.locks.common.init.LocksAttachments;
import melonslise.locks.common.util.Cuboid6i;
import melonslise.locks.common.util.Lock;
import melonslise.locks.common.util.Lockable;
import melonslise.locks.common.util.Transform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

// Adds a random lock to a chest during world generation. The lockable is put into the attachment of every (proto)chunk it touches;
// when the chunk becomes a LevelChunk, ChunkEvent.Load -> LockableHandler.onChunkLoad registers it, ChunkWatchEvent.Sent syncs it to players.
public class LockChestsFeature extends Feature<NoneFeatureConfiguration>
{
	public LockChestsFeature(Codec<NoneFeatureConfiguration> codec)
	{
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx)
	{
		WorldGenLevel level = ctx.level();
		RandomSource rng = ctx.random();
		BlockPos pos = ctx.origin();

		if(!LocksConfig.canGen(rng))
			return false;
		BlockState state = level.getBlockState(pos);
		if(!(state.getBlock() instanceof ChestBlock))
			return false;
		BlockPos pos1 = state.getValue(ChestBlock.TYPE) == ChestType.SINGLE ? pos : pos.relative(ChestBlock.getConnectedDirection(state));
		ItemStack stack = LocksConfig.getRandomLock(rng, level.registryAccess());
		if(stack.isEmpty())
			return false;
		// WorldGenLevel#getLevel is the ServerLevel; ids come from its handler
		int id = LockableHandler.get(level.getLevel()).nextId();
		Lockable lkb = new Lockable(new Cuboid6i(pos, pos1), Lock.from(stack), Transform.fromDirection(state.getValue(ChestBlock.FACING), Direction.NORTH), stack, id);
		lkb.bb.getContainedChunks((x, z) ->
		{
			// NOTE: if LocksAttachments.LOCKABLE_STORAGE is a DeferredHolder/Supplier, add .get()
			level.getChunk(x, z).getData(LocksAttachments.LOCKABLE_STORAGE).add(lkb);
			return true;
		});
		return true;
	}
}
