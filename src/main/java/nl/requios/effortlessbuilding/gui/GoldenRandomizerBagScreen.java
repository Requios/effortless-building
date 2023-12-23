package nl.requios.effortlessbuilding.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import nl.requios.effortlessbuilding.EffortlessBuilding;

import javax.annotation.ParametersAreNonnullByDefault;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
public class GoldenRandomizerBagScreen extends AbstractContainerScreen<GoldenRandomizerBagContainer> {
	private Inventory inventory;

	private static final ResourceLocation guiTextures = new ResourceLocation(EffortlessBuilding.MODID, "textures/gui/container/goldenrandomizerbag.png");

	public GoldenRandomizerBagScreen(GoldenRandomizerBagContainer randomizerBagContainer, Inventory playerInventory, Component title) {
		super(randomizerBagContainer, playerInventory, title);
		this.inventory = playerInventory;
		imageHeight = 134;
	}

	@Override
	public void render(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		renderBackground(ms);
		super.render(ms, mouseX, mouseY, partialTicks);
		this.renderTooltip(ms, mouseX, mouseY);
	}

	@Override
	protected void renderLabels(PoseStack ms, int mouseX, int mouseY) {
		this.font.draw(ms, this.title, 8, 6, 0x404040);
		this.font.draw(ms, this.playerInventoryTitle, 8, imageHeight - 96 + 2, 0x404040);
	}

	@Override
	protected void renderBg(PoseStack ms, float partialTicks, int mouseX, int mouseY) {
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1f);
		RenderSystem.setShaderTexture(0, guiTextures);
		int marginHorizontal = (width - imageWidth) / 2;
		int marginVertical = (height - imageHeight) / 2;
		blit(ms, marginHorizontal, marginVertical, 0, 0, imageWidth, imageHeight);
	}
}
