package nl.requios.effortlessbuilding.create.foundation.gui.element;

import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public interface ScreenElement {

	@OnlyIn(Dist.CLIENT)
	void render(GuiGraphics graphics, int x, int y);

}
