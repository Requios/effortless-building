package nl.requios.effortlessbuilding.gui.elements;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.ScreenUtils;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * This class provides a checkbox style control.
 */
@ParametersAreNonnullByDefault
public class GuiCheckBoxFixed extends Button {
	private final int boxWidth;
	private boolean isChecked;

	public GuiCheckBoxFixed(int xPos, int yPos, String displayString, boolean isChecked) {
		super(xPos, yPos, Minecraft.getInstance().font.width(displayString) + 2 + 11, 11, Component.literal(displayString), b -> {
		}, DEFAULT_NARRATION);
		this.isChecked = isChecked;
		this.boxWidth = 11;
		this.height = 11;
		this.width = this.boxWidth + 2 + Minecraft.getInstance().font.width(displayString);
	}

	@Override
	public void renderWidget(PoseStack ms, int mouseX, int mouseY, float partial) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
		//Is deprecated but still works
		ScreenUtils.blitWithBorder(ms, this.getX(), this.getY(), 0, 46, this.boxWidth, this.height, 200, 20, 2, 3, 2, 2, 0);
		int color = 14737632;

		if (packedFGColor != 0) {
			color = packedFGColor;
		} else if (!this.active) {
			color = 10526880;
		}

		Font font = Minecraft.getInstance().font;

		if (this.isChecked)
			drawCenteredString(ms, font, "x", this.getX() + this.boxWidth / 2 + 1, this.getY() + 1, 14737632);

		font.draw(ms, getMessage(), this.getX() + this.boxWidth + 2, this.getY() + 2, color);
	}

	@Override
	public void onPress() {
		this.isChecked = !this.isChecked;
	}

	public boolean isChecked() {
		return this.isChecked;
	}

	public void setIsChecked(boolean isChecked) {
		this.isChecked = isChecked;
	}
}