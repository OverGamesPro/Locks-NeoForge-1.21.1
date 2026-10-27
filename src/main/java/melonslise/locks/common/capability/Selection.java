package melonslise.locks.common.capability;

import net.minecraft.core.BlockPos;

/*
 * Выбор позиции замка игроком. Data attachment на Player (НЕ сериализуется, НЕ копируется) - см. LocksAttachments.SELECTION.
 * Использование: player.getData(LocksAttachments.SELECTION).get()/set(pos)
 */
public class Selection
{
	public BlockPos pos;

	public BlockPos get()
	{
		return this.pos;
	}

	public void set(BlockPos pos)
	{
		this.pos = pos;
	}
}
