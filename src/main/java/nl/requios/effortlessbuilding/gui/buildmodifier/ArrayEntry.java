package nl.requios.effortlessbuilding.gui.buildmodifier;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import nl.requios.effortlessbuilding.AllGuiTextures;
import nl.requios.effortlessbuilding.buildmodifier.Array;
import nl.requios.effortlessbuilding.buildmodifier.BaseModifier;
import nl.requios.effortlessbuilding.capability.CapabilityHandler;
import nl.requios.effortlessbuilding.create.foundation.gui.widget.ScrollInput;
import nl.requios.effortlessbuilding.gui.elements.LabeledScrollInput;
import nl.requios.effortlessbuilding.utilities.MathHelper;

import java.util.Vector;

@OnlyIn(Dist.CLIENT)
public class ArrayEntry extends BaseModifierEntry<Array> {

	protected Vector<ScrollInput> offsetInputs = new Vector<>(3);
	protected ScrollInput countInput;

	public ArrayEntry(ModifiersScreen screen, BaseModifier array) {
		super(screen, (Array) array, Component.literal("Array"), AllGuiTextures.ARRAY_ENTRY);

		offsetInputs.clear();

		for (int i = 0; i < 3; i++) {
			final int index = i;
			var scrollInput = new LabeledScrollInput(0, 0, 18, 18)
				.titled(Component.literal(i == 0 ? "X Offset" : i == 1 ? "Y Offset" : "Z Offset"))
				.calling(value -> {
					modifier.offset = MathHelper.with(modifier.offset, index, value);
					onValueChanged();
				});
			scrollInput.setState(MathHelper.get(modifier.offset, index));
			offsetInputs.add(scrollInput);
		}
		listeners.addAll(offsetInputs);

		countInput = new LabeledScrollInput(0, 0, 18, 18)
			.withRange(1, 100)
			.titled(Component.literal("Count"))
			.calling(value -> {
				modifier.count = value;
				onValueChanged();
			});
		countInput.setState(modifier.count);
		listeners.add(countInput);
		
		for (int i = 0; i < 3; i++) {
			offsetInputs.get(i).onChanged();
		}
		countInput.onChanged();
		
		onValueChanged();
	}

	@Override
	public void render(GuiGraphics guiGraphics, int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean p_230432_9_, float partialTicks) {
		super.render(guiGraphics, index, y, x, width, height, mouseX, mouseY, p_230432_9_, partialTicks);

		//draw offset inputs
		for (int i = 0; i < 3; i++) {
			offsetInputs.get(i).setX(left + 49 + 20 * i);
			offsetInputs.get(i).setY(top + 19);
			offsetInputs.get(i).render(guiGraphics, mouseX, mouseY, partialTicks);
		}
		
		//draw count input
		countInput.setX(left + 49);
		countInput.setY(top + 41);
		countInput.render(guiGraphics, mouseX, mouseY, partialTicks);
		
		//draw reach label
		reachLabel.setX(right - 8 - getFont().width(reachLabel.text));
		reachLabel.setY(top + 24);
		reachLabel.render(guiGraphics, mouseX, mouseY, partialTicks);
	}

	@Override
	public void onValueChanged() {
		super.onValueChanged();
		
		int currentReach = Math.max(-1, getArrayReach());
		int maxReach = CapabilityHandler.getMaxBlocksPerAxis(Minecraft.getInstance().player, false);
		ChatFormatting reachColor = isCurrentReachValid(currentReach, maxReach) ? ChatFormatting.GRAY : ChatFormatting.RED;
		var reachText = "" + reachColor + currentReach + ChatFormatting.GRAY + "/" + ChatFormatting.GRAY + maxReach;
		reachLabel.text = Component.literal(reachText);
	}

	private int getArrayReach() {
		try {
			//find largest offset
			double x = Math.abs(modifier.offset.getX());
			double y = Math.abs(modifier.offset.getY());
			double z = Math.abs(modifier.offset.getZ());
			double largestOffset = Math.max(Math.max(x, y), z);
			return (int) (largestOffset * modifier.count);
		} catch (NumberFormatException | NullPointerException ex) {
			return -1;
		}
	}

	private boolean isCurrentReachValid(int currentReach, int maxReach) {
		return currentReach <= maxReach && currentReach > -1;
	}
}
