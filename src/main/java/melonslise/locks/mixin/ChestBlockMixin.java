package melonslise.locks.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import melonslise.locks.common.util.LocksUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

// A chest cannot merge into a double chest with a locked neighbour
@Mixin(ChestBlock.class)
public class ChestBlockMixin
{
	@Inject(at = @At("HEAD"), method = "candidatePartnerFacing", cancellable = true)
	private void candidatePartnerFacing(BlockPlaceContext ctx, Direction dir, CallbackInfoReturnable<Direction> cir)
	{
		Level level = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos().relative(dir);
		BlockState state = level.getBlockState(pos);
		cir.setReturnValue(state.is((ChestBlock) (Object) this) && state.getValue(ChestBlock.TYPE) == ChestType.SINGLE && !LocksUtil.locked(level, pos) ? state.getValue(ChestBlock.FACING) : null);
	}
}
