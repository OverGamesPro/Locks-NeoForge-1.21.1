package melonslise.locks.mixin;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import melonslise.locks.common.item.LockItem;
import melonslise.locks.common.util.LocksPredicates;
import melonslise.locks.common.util.LocksUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

// Locked blocks are at least as blast-resistant as their lock (was ExplosionContextMixin).
// The level is taken from the BlockGetter argument (Explosion passes its level), so the Explosion.level AT is not needed.
@Mixin(ExplosionDamageCalculator.class)
public class ExplosionDamageCalculatorMixin
{
	@Inject(at = @At("RETURN"), method = "getBlockExplosionResistance", cancellable = true)
	private void getBlockExplosionResistance(Explosion ex, BlockGetter reader, BlockPos pos, BlockState state, FluidState fluid, CallbackInfoReturnable<Optional<Float>> cir)
	{
		if(!(reader instanceof Level level))
			return;
		cir.setReturnValue(cir.getReturnValue().map(r ->
		{
			float lockResistance = LocksUtil.intersecting(level, pos).filter(LocksPredicates.LOCKED).findFirst().map(lkb -> (float) LockItem.getResistance(lkb.stack)).orElse(0f);
			return Math.max(r, lockResistance);
		}));
	}
}
