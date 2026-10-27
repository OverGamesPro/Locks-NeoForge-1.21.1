package melonslise.locks.client.util;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class LocksClientUtil
{
	private LocksClientUtil() {}

	public static Camera getCamera()
	{
		return Minecraft.getInstance().gameRenderer.getMainCamera();
	}

	// getClippingHelper УДАЛЁН: во фрустум брать RenderLevelStageEvent#getFrustum() (этап 12)

	public static double distanceToLineSq(Vec3 p, Vec3 l1, Vec3 l2)
	{
		Vec3 l = l2.subtract(l1);
		return l.cross(p.subtract(l1)).lengthSqr() / l.lengthSqr();
	}

	// https://forums.minecraftforge.net/topic/88562-116solved-3d-to-2d-conversion/
	// And big thanks to JTK222 Lukas!!!
	// Требует AT: public net.minecraft.client.renderer.GameRenderer getFov(Lnet/minecraft/client/Camera;FZ)D
	public static Vector3f worldToScreen(Vec3 pos, float partialTicks)
	{
		Minecraft mc = Minecraft.getInstance();
		Camera cam = getCamera();
		Vec3 o = cam.getPosition();

		Vector3f pos1 = new Vector3f((float) (o.x - pos.x), (float) (o.y - pos.y), (float) (o.z - pos.z));
		Quaternionf rot = new Quaternionf(cam.rotation()).conjugate();
		rot.transform(pos1);

		// Account for view bobbing
		if(mc.options.bobView().get() && mc.getCameraEntity() instanceof Player player)
		{
			float f = player.walkDist - player.walkDistO;
			float f1 = -(player.walkDist + f * partialTicks);
			float f2 = Mth.lerp(partialTicks, player.oBob, player.bob);

			Quaternionf rot1 = Axis.XP.rotationDegrees(Math.abs(Mth.cos(f1 * (float) Math.PI - 0.2f) * f2) * 5f).conjugate();
			Quaternionf rot2 = Axis.ZP.rotationDegrees(Mth.sin(f1 * (float) Math.PI) * f2 * 3f).conjugate();
			rot1.transform(pos1);
			rot2.transform(pos1);
			pos1.add(Mth.sin(f1 * (float) Math.PI) * f2 * 0.5f, Math.abs(Mth.cos(f1 * (float) Math.PI) * f2), 0f);
		}

		Window w = mc.getWindow();
		float sc = w.getGuiScaledHeight() / 2f / pos1.z() / (float) Math.tan(Math.toRadians(mc.gameRenderer.getFov(cam, partialTicks, true) / 2f));
		pos1.mul(-sc, -sc, 1f);
		pos1.add(w.getGuiScaledWidth() / 2f, w.getGuiScaledHeight() / 2f, 0f);

		return pos1;
	}

	// Перед вызовом вызывающий обязан сделать RenderSystem.setShaderTexture(0, tex)
	public static void texture(PoseStack mtx, float x, float y, int u, int v, int width, int height, int texWidth, int texHeight, float alpha)
	{
		Matrix4f last = mtx.last().pose();
		float f = 1f / texWidth;
		float f1 = 1f / texHeight;

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
		BufferBuilder buf = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
		buf.addVertex(last, x, y + height, 0f).setUv(u * f, (v + height) * f1).setColor(1f, 1f, 1f, alpha);
		buf.addVertex(last, x + width, y + height, 0f).setUv((u + width) * f, (v + height) * f1).setColor(1f, 1f, 1f, alpha);
		buf.addVertex(last, x + width, y, 0f).setUv((u + width) * f, v * f1).setColor(1f, 1f, 1f, alpha);
		buf.addVertex(last, x, y, 0f).setUv(u * f, v * f1).setColor(1f, 1f, 1f, alpha);
		BufferUploader.drawWithShader(buf.buildOrThrow());
	}

	// Вызывающий создаёт buf через Tesselator.getInstance().begin(QUADS, POSITION_COLOR) и сам рисует buildOrThrow()
	// https://stackoverflow.com/questions/7854043/drawing-rectangle-between-two-points-with-arbitrary-width
	public static void line(BufferBuilder buf, PoseStack mtx, float x1, float y1, float x2, float y2, float width, float r, float g, float b, float a)
	{
		Matrix4f last = mtx.last().pose();
		// Construct perpendicular
		float pX = y2 - y1;
		float pY = x1 - x2;
		// Normalize and scale by half width
		float pL = Mth.sqrt(pX * pX + pY * pY);
		pX *= width / 2f / pL;
		pY *= width / 2f / pL;

		buf.addVertex(last, x1 + pX, y1 + pY, 0f).setColor(r, g, b, a);
		buf.addVertex(last, x1 - pX, y1 - pY, 0f).setColor(r, g, b, a);
		buf.addVertex(last, x2 - pX, y2 - pY, 0f).setColor(r, g, b, a);
		buf.addVertex(last, x2 + pX, y2 + pY, 0f).setColor(r, g, b, a);
	}

	public static void square(BufferBuilder buf, PoseStack mtx, float x, float y, float length, float r, float g, float b, float a)
	{
		Matrix4f last = mtx.last().pose();
		length /= 2f;
		buf.addVertex(last, x - length, y - length, 0f).setColor(r, g, b, a);
		buf.addVertex(last, x - length, y + length, 0f).setColor(r, g, b, a);
		buf.addVertex(last, x + length, y + length, 0f).setColor(r, g, b, a);
		buf.addVertex(last, x + length, y - length, 0f).setColor(r, g, b, a);
	}

	public static void vGradient(BufferBuilder bld, PoseStack mtx, int x1, int y1, int x2, int y2, float r1, float g1, float b1, float a1, float r2, float g2, float b2, float a2)
	{
		Matrix4f last = mtx.last().pose();
		bld.addVertex(last, x2, y1, 0f).setColor(r1, g1, b1, a1);
		bld.addVertex(last, x1, y1, 0f).setColor(r1, g1, b1, a1);
		bld.addVertex(last, x1, y2, 0f).setColor(r2, g2, b2, a2);
		bld.addVertex(last, x2, y2, 0f).setColor(r2, g2, b2, a2);
	}

	public static float lerp(float start, float end, float progress)
	{
		return start + (end - start) * progress;
	}

	public static double lerp(double start, double end, double progress)
	{
		return start + (end - start) * progress;
	}

	/*
	 * Implement 2d cubic bezier function
	 * https://stackoverflow.com/questions/11696736/recreating-css3-transitions-cubic-bezier-curve
	 */
	public static float cubicBezier1d(float anchor1, float anchor2, float progress)
	{
		float omp = 1f - progress;
		return 3f * omp * omp * progress * anchor1 + 3f * omp * progress * progress * anchor2 + progress * progress * progress;
	}
}
