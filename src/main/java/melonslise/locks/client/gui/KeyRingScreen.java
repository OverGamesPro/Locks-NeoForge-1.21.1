package melonslise.locks.client.gui;

import melonslise.locks.common.container.KeyRingContainer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class KeyRingScreen extends AbstractContainerScreen<KeyRingContainer>
{
	public static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");

	public KeyRingScreen(KeyRingContainer menu, Inventory inv, Component title)
	{
		super(menu, inv, title);
		this.imageHeight = 114 + menu.rows * 18;
		this.inventoryLabelY = this.imageHeight - 94;
	}

	@Override
	public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick)
	{
		// In 1.21.1 AbstractContainerScreen.render already draws the background (dim/blur), so no explicit renderBackground call
		super.render(gui, mouseX, mouseY, partialTick);
		this.renderTooltip(gui, mouseX, mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics gui, float partialTick, int mouseX, int mouseY)
	{
		int rows = this.getMenu().rows;
		int cornerX = (this.width - this.imageWidth) / 2;
		int cornerY = (this.height - this.imageHeight) / 2;
		gui.blit(TEXTURE, cornerX, cornerY, 0, 0, this.imageWidth, rows * 18 + 17);
		gui.blit(TEXTURE, cornerX, cornerY + rows * 18 + 17, 0, 126, this.imageWidth, 96);
	}
}
