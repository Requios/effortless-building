package nl.requios.effortlessbuilding.gui.buildmodifier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import nl.requios.effortlessbuilding.AllGuiTextures;
import nl.requios.effortlessbuilding.AllIcons;
import nl.requios.effortlessbuilding.buildmodifier.BaseModifier;
import nl.requios.effortlessbuilding.buildmodifier.Mirror;
import nl.requios.effortlessbuilding.capability.CapabilityHandler;
import nl.requios.effortlessbuilding.create.foundation.gui.widget.IconButton;
import nl.requios.effortlessbuilding.create.foundation.gui.widget.ScrollInput;
import nl.requios.effortlessbuilding.create.foundation.utility.Components;
import nl.requios.effortlessbuilding.gui.elements.LabeledScrollInput;
import nl.requios.effortlessbuilding.utilities.MathHelper;

import java.text.DecimalFormat;
import java.util.Vector;

@OnlyIn(Dist.CLIENT)
public class MirrorEntry extends BaseModifierEntry<Mirror> {
	
	protected Vector<ScrollInput> positionInputs;
	protected IconButton playerPositionButton;
	protected IconButton toggleOffsetButton;
	protected Vector<IconButton> axisButtons;
	protected ScrollInput radiusInput;
	protected IconButton showLinesButton;
	protected IconButton showAreasButton;
	protected DecimalFormat df = new DecimalFormat("#.#");
	
	public MirrorEntry(ModifiersScreen screen, BaseModifier mirror) {
		super(screen, (Mirror) mirror, Component.literal("Mirror"), AllGuiTextures.MIRROR_ENTRY);
		
		positionInputs = new Vector<>();
		axisButtons = new Vector<>();
		
		//Position
		//ScrollInput works with double the value, so we can have 0.5 increments
		for (int i = 0; i < 3; i++) {
			final int index = i;
			var scrollInput = new LabeledScrollInput(0, 0, 36, 18)
				.showControlScrollsSlowerTooltip()
				.titled(Component.literal(i == 0 ? "X Position" : i == 1 ? "Y Position" : "Z Position"))
				.format(integer -> Component.literal(df.format(integer / 2.0)))
				.withStepFunction(stepContext -> stepContext.shift ? 20 : stepContext.control ? 1 : 2)
				.calling(value -> {
					modifier.position = MathHelper.with(modifier.position, index, value / 2.0);
					onValueChanged();
				});
			scrollInput.setState((int) (MathHelper.get(modifier.position, index) * 2.0));
			positionInputs.add(scrollInput);
		}
		listeners.addAll(positionInputs);
		
		//Player position button
		playerPositionButton = new IconButton(0, 0, AllIcons.I_PLAYER)
			.withCallback(() -> {
				modifier.position = Vec3.atLowerCornerOf(Minecraft.getInstance().player.blockPosition());
				onValueChanged();
			});
		playerPositionButton.setToolTip(Components.literal("Set to player position"));
		listeners.add(playerPositionButton);
		
		//Toggle offset button
		toggleOffsetButton = new IconButton(0, 0, AllIcons.I_BLOCK_CENTER)
			.withCallback(() -> {
				if (modifier.position.x == Math.floor(modifier.position.x)) {
					modifier.position = new Vec3(
						Math.floor(modifier.position.x) + 0.5,
						Math.floor(modifier.position.y) + 0.5,
						Math.floor(modifier.position.z) + 0.5
					);
				}
				else {
					modifier.position = new Vec3(
						Math.floor(modifier.position.x),
						Math.floor(modifier.position.y),
						Math.floor(modifier.position.z)
					);
				}
				onValueChanged();
			});
		listeners.add(toggleOffsetButton);
		
		//Axis buttons
		for (int i = 0; i < 3; i++) {
			final int index = i;
			IconButton button = new IconButton(0, 0, i == 0 ? AllIcons.I_X_ON : i == 1 ? AllIcons.I_Y_OFF : AllIcons.I_Z_OFF)
				.withCallback(() -> {
					modifier.toggleMirrorAxis(index);
					onValueChanged();
				});
			button.setToolTip(Components.literal(i == 0 ? "X Axis" : i == 1 ? "Y Axis" : "Z Axis"));
			axisButtons.add(button);
		}
		listeners.addAll(axisButtons);
		
		//Radius
		radiusInput = new LabeledScrollInput(0, 0, 27, 18)
			.withRange(0, CapabilityHandler.getMaxMirrorRadius(Minecraft.getInstance().player, false))
			.titled(Minecraft.getInstance().player.isCreative() ?
					Component.literal("Radius") :
					Component.literal("Radius. Use Reach Upgrade items to increase maximum."))
			.calling(value -> {
				modifier.radius = value;
				onValueChanged();
			});
		radiusInput.setState(modifier.radius);
		listeners.add(radiusInput);
		
		//Show lines button
		showLinesButton = new IconButton(0, 0, AllIcons.I_SHOW_LINES)
			.withCallback(() -> {
				modifier.drawLines = !modifier.drawLines;
				onValueChanged();
			});
		listeners.add(showLinesButton);
		
		//Show areas button
		showAreasButton = new IconButton(0, 0, AllIcons.I_SHOW_AREAS)
			.withCallback(() -> {
				modifier.drawPlanes = !modifier.drawPlanes;
				onValueChanged();
			});
		listeners.add(showAreasButton);
		
		for (ScrollInput positionInput : positionInputs) {
			positionInput.onChanged();
		}
		radiusInput.onChanged();
	}
	
	@Override
	public void render(GuiGraphics guiGraphics, int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean p_230432_9_, float partialTicks) {
		super.render(guiGraphics, index, y, x, width, height, mouseX, mouseY, p_230432_9_, partialTicks);
		
		//draw position inputs
		for (int i = 0; i < 3; i++) {
			ScrollInput input = positionInputs.get(i);
			input.setX(left + 49 + 38 * i);
			input.setY(top + 19);
			input.render(guiGraphics, mouseX, mouseY, partialTicks);
		}
		
		//draw player position button
		playerPositionButton.setX(left + 163);
		playerPositionButton.setY(top + 19);
		playerPositionButton.render(guiGraphics, mouseX, mouseY, partialTicks);
		
		//draw toggle offset button
		toggleOffsetButton.setX(left + 183);
		toggleOffsetButton.setY(top + 19);
		toggleOffsetButton.render(guiGraphics, mouseX, mouseY, partialTicks);
		
		//draw axis buttons
		for (int i = 0; i < 3; i++) {
			IconButton button = axisButtons.get(i);
			button.setX(left + 49 + 18 * i);
			button.setY(top + 41);
			button.render(guiGraphics, mouseX, mouseY, partialTicks);
		}
		
		//draw radius input
		radiusInput.setX(left + 134);
		radiusInput.setY(top + 41);
		radiusInput.render(guiGraphics, mouseX, mouseY, partialTicks);
		
		//draw show lines button
		showLinesButton.setX(left + 163);
		showLinesButton.setY(top + 41);
		showLinesButton.render(guiGraphics, mouseX, mouseY, partialTicks);
		
		//draw show areas button
		showAreasButton.setX(left + 183);
		showAreasButton.setY(top + 41);
		showAreasButton.render(guiGraphics, mouseX, mouseY, partialTicks);
	}
	
	@Override
	public void onValueChanged() {
		super.onValueChanged();
		
		//Position
		for (int i = 0; i < 3; i++) {
			ScrollInput input = positionInputs.get(i);
			input.setState((int) (MathHelper.get(modifier.position, i) * 2.0));
		}
		
		//Toggle offset button
		if (modifier.position.x == Math.floor(modifier.position.x)) {
			toggleOffsetButton.setIcon(AllIcons.I_BLOCK_CENTER);
			toggleOffsetButton.setToolTip(Components.literal("Set position to center of block, for uneven numbered builds."));
		}
		else {
			toggleOffsetButton.setIcon(AllIcons.I_BLOCK_CORNER);
			toggleOffsetButton.setToolTip(Components.literal("Set position to corner of block, for even numbered builds."));
		}
		
		//Axis buttons
		for (int i = 0; i < 3; i++) {
			IconButton button = axisButtons.get(i);
			if (modifier.getMirrorAxis(i)) {
				button.setIcon(i == 0 ? AllIcons.I_X_ON : i == 1 ? AllIcons.I_Y_ON : AllIcons.I_Z_ON);
			}
			else {
				button.setIcon(i == 0 ? AllIcons.I_X_OFF : i == 1 ? AllIcons.I_Y_OFF : AllIcons.I_Z_OFF);
			}
		}
		
		//Show lines button
		if (modifier.drawLines) {
			showLinesButton.setIcon(AllIcons.I_SHOW_LINES);
			showLinesButton.setToolTip(Components.literal("Show mirror lines"));
		}
		else {
			showLinesButton.setIcon(AllIcons.I_HIDE_LINES);
			showLinesButton.setToolTip(Components.literal("Show mirror lines"));
		}
		
		//Show areas button
		if (modifier.drawPlanes) {
			showAreasButton.setIcon(AllIcons.I_SHOW_AREAS);
			showAreasButton.setToolTip(Components.literal("Show mirror areas"));
		}
		else {
			showAreasButton.setIcon(AllIcons.I_HIDE_AREAS);
			showAreasButton.setToolTip(Components.literal("Show mirror areas"));
		}
	}
}
