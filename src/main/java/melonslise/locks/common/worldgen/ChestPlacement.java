package melonslise.locks.common.worldgen;

import java.util.stream.Stream;

import com.mojang.serialization.MapCodec;

import melonslise.locks.common.init.LocksPlacements;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

// Yields the position of every chest in the chunk being generated (for double chests - only one half)
public class ChestPlacement extends PlacementModifier
{
	public static final ChestPlacement INSTANCE = new ChestPlacement();

	public static final MapCodec<ChestPlacement> CODEC = MapCodec.unit(INSTANCE);

	private ChestPlacement() {}

	@Override
	public Stream<BlockPos> getPositions(PlacementContext ctx, RandomSource rng, BlockPos pos)
	{
		return ctx.getLevel().getChunk(pos).getBlockEntitiesPos().stream()
			.filter(tePos ->
			{
				BlockState state = ctx.getLevel().getBlockState(tePos);
				// Prevent from adding double chests twice
				return state.hasProperty(ChestBlock.TYPE) && state.getValue(ChestBlock.TYPE) != ChestType.RIGHT;
			});
	}

	@Override
	public PlacementModifierType<?> type()
	{
		return LocksPlacements.CHEST.get();
	}
}
