package melonslise.locks.common.util;

import java.util.Random;
import java.util.stream.Stream;

import melonslise.locks.common.capability.LockableHandler;
import net.minecraft.world.level.Level;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/*
 * Этап 3. Из оригинала сюда НЕ перенесено (ждёт своих этапов):
 *  - resourceManager / lootTableFrom / mergeEntries (рефлексия ForgeHooks$LootTableContext + миксины LootPool/LootTable) -> этап 10 (Global Loot Modifier)
 *  (intersecting/locked возвращены в этапе 4 через LockableHandler.get(level))
 */
public final class LocksUtil
{
	// Замена ItemStack.ATTRIBUTE_MODIFIER_FORMAT (в 1.21.1 недоступен): "#.##", точка как разделитель
	public static final java.text.DecimalFormat FORMAT = new java.text.DecimalFormat("#.##", java.text.DecimalFormatSymbols.getInstance(java.util.Locale.ROOT));

	private LocksUtil() {}

	public static void shuffle(byte[] array, Random rng)
	{
		for (int a = array.length - 1; a > 0; --a)
		{
			int index = rng.nextInt(a + 1);
			byte temp = array[index];
			array[index] = array[a];
			array[a] = temp;
		}
	}

	public static boolean chance(Random rng, double ch)
	{
		return ch == 1d || ch != 0d && rng.nextDouble() <= ch;
	}

	// В 1.21 worldgen/structure-код использует RandomSource
	public static boolean chance(RandomSource rng, double ch)
	{
		return ch == 1d || ch != 0d && rng.nextDouble() <= ch;
	}

	public static BlockPos transform(int x, int y, int z, StructurePlaceSettings settings)
	{
		switch(settings.getMirror())
		{
		case LEFT_RIGHT:
			z = -z + 1;
			break;
		case FRONT_BACK:
			x = -x + 1;
			break;
		default:
			break;
		}
		int x1 = settings.getRotationPivot().getX();
		int z1 = settings.getRotationPivot().getZ();
		switch(settings.getRotation())
		{
		case COUNTERCLOCKWISE_90:
			return new BlockPos(x1 - z1 + z, y, x1 + z1 - x + 1);
		case CLOCKWISE_90:
			return new BlockPos(x1 + z1 - z + 1, y, z1 - x1 + x);
		case CLOCKWISE_180:
			return new BlockPos(x1 + x1 - x + 1, y, z1 + z1 - z + 1);
		default:
			return new BlockPos(x, y, z);
		}
	}

	public static AttachFace faceFromDir(Direction dir)
	{
		return dir == Direction.UP ? AttachFace.CEILING : dir == Direction.DOWN ? AttachFace.FLOOR : AttachFace.WALL;
	}

	public static AABB rotateY(AABB bb)
	{
		return new AABB(bb.minZ, bb.minY, bb.minX, bb.maxZ, bb.maxY, bb.maxX);
	}

	public static AABB rotateX(AABB bb)
	{
		return new AABB(bb.minX, bb.minZ, bb.minY, bb.maxX, bb.maxZ, bb.maxY);
	}

	public static boolean intersectsInclusive(AABB bb1, AABB bb2)
	{
		return bb1.minX <= bb2.maxX && bb1.maxX >= bb2.minX && bb1.minY <= bb2.maxY && bb1.maxY >= bb2.minY && bb1.minZ <= bb2.maxZ && bb1.maxZ >= bb2.minZ;
	}

	public static Vec3 sideCenter(AABB bb, Direction side)
	{
		Vec3i dir = side.getNormal();
		return new Vec3((bb.minX + bb.maxX + (bb.maxX - bb.minX) * dir.getX()) * 0.5d, (bb.minY + bb.maxY + (bb.maxY - bb.minY) * dir.getY()) * 0.5d, (bb.minZ + bb.maxZ + (bb.maxZ - bb.minZ) * dir.getZ()) * 0.5d);
	}

	public static Stream<Lockable> intersecting(Level world, BlockPos pos)
	{
		return LockableHandler.get(world).getInChunk(pos).values().stream().filter(lkb -> lkb.bb.intersects(pos));
	}

	public static boolean locked(Level world, BlockPos pos)
	{
		return intersecting(world, pos).anyMatch(LocksPredicates.LOCKED);
	}
}
