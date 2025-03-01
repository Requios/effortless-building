package nl.requios.effortlessbuilding.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import nl.requios.effortlessbuilding.EffortlessBuilding;

import javax.annotation.ParametersAreNonnullByDefault;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
public class RandomizerBagScreen extends AbstractContainerScreen<RandomizerBagContainer> {
	private Inventory inventory;

	private static final ResourceLocation guiTextures = EffortlessBuilding.asResource("textures/gui/container/randomizerbag.png");

	public RandomizerBagScreen(RandomizerBagContainer randomizerBagContainer, Inventory playerInventory, Component title) {
		super(randomizerBagContainer, playerInventory, title);
		this.inventory = playerInventory;
		imageHeight = 134;
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
//		renderBackground(guiGraphics);
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
		this.renderTooltip(guiGraphics, mouseX, mouseY);
	}

	@Override
	protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		guiGraphics.drawString(this.font, this.title, 8, 6, 0x404040, false);
		guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, imageHeight - 96 + 2, 0x404040, false);
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
		int marginHorizontal = (width - imageWidth) / 2;
		int marginVertical = (height - imageHeight) / 2;
		guiGraphics.blit(guiTextures, marginHorizontal, marginVertical, 0, 0, imageWidth, imageHeight);
	}
}
