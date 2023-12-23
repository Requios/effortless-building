package nl.requios.effortlessbuilding.create.foundation.gui.widget;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import nl.requios.effortlessbuilding.create.foundation.gui.AllGuiTextures;

public class Indicator extends AbstractSimiWidget {

	public State state;

	public Indicator(int x, int y, Component tooltip) {
		super(x, y, AllGuiTextures.INDICATOR.width, AllGuiTextures.INDICATOR.height);
		this.toolTip = toolTip.isEmpty() ? ImmutableList.of() : ImmutableList.of(tooltip);
		this.state = State.OFF;
	}

	@Override
	public void render(PoseStack ms, int mouseX, int mouseY, float partialTicks ) {
		if (!visible)
			return;
		AllGuiTextures toDraw;
		switch (state) {
			case ON: toDraw = AllGuiTextures.INDICATOR_WHITE; break;
			case OFF: toDraw = AllGuiTextures.INDICATOR; break;
			case RED: toDraw = AllGuiTextures.INDICATOR_RED; break;
			case YELLOW: toDraw = AllGuiTextures.INDICATOR_YELLOW; break;
			case GREEN: toDraw = AllGuiTextures.INDICATOR_GREEN; break;
			default: toDraw = AllGuiTextures.INDICATOR; break;
		}
		toDraw.render(ms, getX(), getY());
	}

	public enum State {
		OFF, ON,
		RED, YELLOW, GREEN;
	}

}
