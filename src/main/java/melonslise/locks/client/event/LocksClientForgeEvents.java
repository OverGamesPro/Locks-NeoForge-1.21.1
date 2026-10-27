package melonslise.locks.client.event;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;

import melonslise.locks.Locks;
import melonslise.locks.client.init.LocksRenderTypes;
import melonslise.locks.client.util.LocksClientUtil;
import melonslise.locks.common.capability.LockableHandler;
import melonslise.locks.common.config.LocksServerConfig;
import melonslise.locks.common.init.LocksAttachments;
import melonslise.locks.common.init.LocksItemTags;
import melonslise.locks.common.util.Lockable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Vector3f;

@EventBusSubscriber(modid = Locks.ID, value = Dist.CLIENT)
public final class LocksClientForgeEvents
{
	public static Lockable tooltipLockable;

	private LocksClientForgeEvents() {}

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent.Pre e)
	{
		Minecraft mc = Minecraft.getInstance();
		if(mc.level == null || mc.isPaused())
			return;
		LockableHandler.get(mc.level).getLoaded().values().forEach(lkb -> lkb.tick());
	}

	// Выход из мира: хендлер клиента не должен хранить замки старого мира (LevelEvent.Unload на клиенте не гарантирован, см. HANDOFF этап 4)
	@SubscribeEvent
	public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut e)
	{
		LockableHandler.clear();
		tooltipLockable = null;
	}

	@SubscribeEvent
	public static void onRenderLevel(RenderLevelStageEvent e)
	{
		Minecraft mc = Minecraft.getInstance();
		if(mc.level == null || mc.player == null)
			return;
		// Замки рисуем вместе с блок-сущностями (аналог миксина WorldRendererMixin оригинала: чтобы модели не пропадали в воде и при fabulous graphics),
		// рамку выделения - в самом конце, поверх всего
		if(e.getStage() == RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES)
			renderLocks(e.getPoseStack(), mc.renderBuffers().bufferSource(), e.getFrustum(), e.getCamera().getPosition(), e.getPartialTick().getGameTimeDeltaPartialTick(false));
		else if(e.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES)
			renderSelection(e.getPoseStack(), mc.renderBuffers().bufferSource(), e.getCamera().getPosition());
	}

	public static boolean holdingPick(Player player)
	{
		for(InteractionHand hand : InteractionHand.values())
			if(player.getItemInHand(hand).is(LocksItemTags.LOCK_PICKS))
				return true;
		return false;
	}

	@SubscribeEvent
	public static void onRenderOverlay(RenderGuiEvent.Pre e)
	{
		Minecraft mc = Minecraft.getInstance();
		if(tooltipLockable == null || mc.level == null || mc.player == null)
			return;
		Lockable lkb = tooltipLockable;
		tooltipLockable = null;
		if(!holdingPick(mc.player))
			return;
		Lockable.State state = lkb.getLockState(mc.level);
		if(state == null)
			return;
		float pt = e.getPartialTick().getGameTimeDeltaPartialTick(false);
		Vector3f vec = LocksClientUtil.worldToScreen(state.pos, pt);
		if(vec.z() >= 0d)
			return;
		List<FormattedCharSequence> lines = new ArrayList<>();
		for(Component c : lkb.stack.getTooltipLines(Item.TooltipContext.of(mc.level), mc.player, mc.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL))
			lines.add(c.getVisualOrderText());
		GuiGraphics gui = e.getGuiGraphics();
		PoseStack mtx = gui.pose();
		mtx.pushPose();
		mtx.translate(vec.x(), vec.y(), 0f);
		renderHudTooltip(gui, mtx, lines, mc.font);
		mtx.popPose();
	}

	public static void renderLocks(PoseStack mtx, MultiBufferSource.BufferSource buf, net.minecraft.client.renderer.culling.Frustum frustum, Vec3 o, float pt)
	{
		Minecraft mc = Minecraft.getInstance();

		double dMin = 0d;

		for(Lockable lkb : LockableHandler.get(mc.level).getLoaded().values())
		{
			Lockable.State state = lkb.getLockState(mc.level);
			if(state == null || !state.inRange(o) || !state.inView(frustum))
				continue;

			double d = o.subtract(state.pos).lengthSqr();
			if(d <= 25d)
			{
				Vec3 look = o.add(mc.player.getViewVector(pt));
				double d1 = LocksClientUtil.distanceToLineSq(state.pos, o, look);
				if(d1 <= 4d && (dMin == 0d || d1 < dMin))
				{
					tooltipLockable = lkb;
					dMin = d1;
				}
			}

			mtx.pushPose();
			// For some reason translating by negative player position and then the point coords causes jittering in very big z and x coords. Why? Thus we use 1 translation instead
			mtx.translate(state.pos.x - o.x, state.pos.y - o.y, state.pos.z - o.z);
			mtx.mulPose(Axis.YP.rotationDegrees(-state.tr.dir.toYRot() - 180f));
			if(state.tr.face != AttachFace.WALL)
				mtx.mulPose(Axis.XP.rotationDegrees(90f));
			mtx.translate(0d, 0.1d, 0d);
			// maxSwingTicks == 0 (замок ни разу не качался): в оригинале получалось 0/0 = NaN, здесь качание просто пропускается
			if(lkb.maxSwingTicks > 0)
				mtx.mulPose(Axis.ZP.rotationDegrees(Mth.sin(LocksClientUtil.cubicBezier1d(1f, 1f, LocksClientUtil.lerp(lkb.maxSwingTicks - lkb.oldSwingTicks, lkb.maxSwingTicks - lkb.swingTicks, pt) / lkb.maxSwingTicks) * lkb.maxSwingTicks / 5f * 3.14f) * 10f));
			mtx.translate(0d, -0.1d, 0d);
			mtx.scale(0.5f, 0.5f, 0.5f);
			int light = LevelRenderer.getLightColor(mc.level, BlockPos.containing(state.pos));
			mc.getItemRenderer().renderStatic(lkb.stack, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY, mtx, buf, mc.level, 0);
			mtx.popPose();
		}
		buf.endBatch();
	}

	public static void renderSelection(PoseStack mtx, MultiBufferSource.BufferSource buf, Vec3 o)
	{
		Minecraft mc = Minecraft.getInstance();
		BlockPos pos = mc.player.getData(LocksAttachments.SELECTION).get();
		if(pos == null)
			return;
		BlockPos pos1 = mc.hitResult instanceof BlockHitResult ? ((BlockHitResult) mc.hitResult).getBlockPos() : pos;
		boolean allow = Math.abs(pos.getX() - pos1.getX()) * Math.abs(pos.getY() - pos1.getY()) * Math.abs(pos.getZ() - pos1.getZ()) <= LocksServerConfig.MAX_LOCKABLE_VOLUME.get() && LocksServerConfig.canLock(mc.level, pos1);
		// Same as above
		LevelRenderer.renderLineBox(mtx, buf.getBuffer(LocksRenderTypes.OVERLAY_LINES), Math.min(pos.getX(), pos1.getX()) - o.x, Math.min(pos.getY(), pos1.getY()) - o.y, Math.min(pos.getZ(), pos1.getZ()) - o.z, Math.max(pos.getX(), pos1.getX()) + 1d - o.x, Math.max(pos.getY(), pos1.getY()) + 1d - o.y, Math.max(pos.getZ(), pos1.getZ()) + 1d - o.z, allow ? 0f : 1f, allow ? 1f : 0f, 0f, 0.5f);
		RenderSystem.disableDepthTest();
		buf.endBatch(LocksRenderTypes.OVERLAY_LINES);
		RenderSystem.enableDepthTest();
	}

	// Taken from Screen and modified to draw and fancy line and square and removed color recalculation
	public static void renderHudTooltip(GuiGraphics gui, PoseStack mtx, List<FormattedCharSequence> lines, Font font)
	{
		if (lines.isEmpty())
			return;
		int width = 0;
		for (FormattedCharSequence line : lines)
		{
			int j = font.width(line);
			if (j > width)
				width = j;
		}

		int x = 36;
		int y = -36;
		int height = 8;
		if (lines.size() > 1)
			height += 2 + (lines.size() - 1) * 10;

		mtx.pushPose();

		BufferBuilder buf = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		LocksClientUtil.square(buf, mtx, 0f, 0f, 4f, 0.05f, 0f, 0.3f, 0.8f);
		LocksClientUtil.line(buf, mtx, 1f, -1f, x / 3f + 0.6f, y / 2f, 2f, 0.05f, 0f, 0.3f, 0.8f);
		LocksClientUtil.line(buf, mtx, x / 3f, y / 2f, x - 3f, y / 2f, 2f, 0.05f, 0f, 0.3f, 0.8f);
		LocksClientUtil.vGradient(buf, mtx, x - 3, y - 4, x + width + 3, y - 3, 0.0627451f, 0f, 0.0627451f, 0.9411765f, 0.0627451f, 0f, 0.0627451f, 0.9411765f);
		LocksClientUtil.vGradient(buf, mtx, x - 3, y + height + 3, x + width + 3, y + height + 4, 0.0627451f, 0f, 0.0627451f, 0.9411765f, 0.0627451f, 0f, 0.0627451f, 0.9411765f);
		LocksClientUtil.vGradient(buf, mtx, x - 3, y - 3, x + width + 3, y + height + 3, 0.0627451f, 0f, 0.0627451f, 0.9411765f, 0.0627451f, 0f, 0.0627451f, 0.9411765f);
		LocksClientUtil.vGradient(buf, mtx, x - 4, y - 3, x - 3, y + height + 3, 0.0627451f, 0f, 0.0627451f, 0.9411765f, 0.0627451f, 0f, 0.0627451f, 0.9411765f);
		LocksClientUtil.vGradient(buf, mtx, x + width + 3, y - 3, x + width + 4, y + height + 3, 0.0627451f, 0f, 0.0627451f, 0.9411765f, 0.0627451f, 0f, 0.0627451f, 0.9411765f);
		LocksClientUtil.vGradient(buf, mtx, x - 3, y - 3 + 1, x - 3 + 1, y + height + 3 - 1, 0.3137255f, 0f, 1f, 0.3137255f, 0.15686275f, 0f, 0.49803922f, 0.3137255f);
		LocksClientUtil.vGradient(buf, mtx, x + width + 2, y - 3 + 1, x + width + 3, y + height + 3 - 1, 0.3137255f, 0f, 1f, 0.3137255f, 0.15686275f, 0f, 0.49803922f, 0.3137255f);
		LocksClientUtil.vGradient(buf, mtx, x - 3, y - 3, x + width + 3, y - 3 + 1, 0.3137255f, 0f, 1f, 0.3137255f, 0.3137255f, 0f, 1f, 0.3137255f);
		LocksClientUtil.vGradient(buf, mtx, x - 3, y + height + 2, x + width + 3, y + height + 3, 0.15686275f, 0f, 0.49803922f, 0.3137255f, 0.15686275f, 0f, 0.49803922f, 0.3137255f);
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		BufferUploader.drawWithShader(buf.buildOrThrow());
		RenderSystem.disableBlend();

		// Текст ставится в очередь GuiGraphics и рисуется после фона выше
		for (int a = 0; a < lines.size(); ++a)
		{
			FormattedCharSequence line = lines.get(a);
			if (line != null)
				gui.drawString(font, line, x, y, -1, true);
			if (a == 0)
				y += 2;
			y += 10;
		}

		mtx.popPose();
	}
}
