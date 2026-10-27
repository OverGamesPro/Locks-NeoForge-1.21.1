package melonslise.locks.mixin;

import java.util.stream.Collectors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import melonslise.locks.common.capability.LockableHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.state.BlockState;

// When the locked block itself is replaced/destroyed, the lock drops as an item (was ServerWorldMixin)
@Mixin(ServerLevel.class)
public class ServerLevelMixin
{
	@Inject(at = @At("HEAD"), method = "sendBlockUpdated")
	private void sendBlockUpdated(BlockPos pos, BlockState oldState, BlockState newState, int flags, CallbackInfo ci)
	{
		if(oldState.is(newState.getBlock()))
			return;
		ServerLevel level = (ServerLevel) (Object) this;
		LockableHandler handler = LockableHandler.get(level);
		// buffer list, otherwise we would be deleting elements while iterating
		handler.getInChunk(pos).values().stream().filter(lkb -> lkb.bb.intersects(pos)).collect(Collectors.toList()).forEach(lkb ->
		{
			level.playSound(null, pos, SoundEvents.ITEM_BREAK, SoundSource.BLOCKS, 0.8f, 0.8f + level.random.nextFloat() * 0.4f);
			level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5d, pos.getY() + 0.5d, pos.getZ() + 0.5d, lkb.stack));
			handler.remove(lkb.id);
		});
	}
}
