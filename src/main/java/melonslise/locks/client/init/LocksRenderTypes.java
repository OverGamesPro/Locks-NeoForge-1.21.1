package melonslise.locks.client.init;

import java.util.OptionalDouble;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import melonslise.locks.Locks;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

// Наследование от RenderType нужно только ради доступа к protected-константам RenderStateShard (шейдеры, состояния), как в оригинале
@OnlyIn(Dist.CLIENT)
public final class LocksRenderTypes extends RenderType
{
	// FIXME оригинальный комментарий: depth всё ещё как-то работает (NO_DEPTH_TEST не всегда спасает).
	// В 1.21 формат линий - POSITION_COLOR_NORMAL (нужен шейдеру RENDERTYPE_LINES, LevelRenderer.renderLineBox пишет нормали)
	public static final RenderType OVERLAY_LINES = RenderType.create(Locks.ID + ".overlay_lines", DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.LINES, 256, false, false, RenderType.CompositeState.builder()
		.setShaderState(RENDERTYPE_LINES_SHADER)
		.setLineState(new LineStateShard(OptionalDouble.empty()))
		.setLayeringState(VIEW_OFFSET_Z_LAYERING)
		.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
		.setDepthTestState(NO_DEPTH_TEST)
		.setWriteMaskState(COLOR_WRITE)
		.createCompositeState(false));

	private LocksRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int bufSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setup, Runnable clear)
	{
		super(name, format, mode, bufSize, affectsCrumbling, sortOnUpload, setup, clear);
	}
}
