package nl.requios.effortlessbuilding.gui.buildmodifier;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import nl.requios.effortlessbuilding.ClientEvents;
import nl.requios.effortlessbuilding.EffortlessBuildingClient;
import nl.requios.effortlessbuilding.buildmodifier.Array;
import nl.requios.effortlessbuilding.buildmodifier.BaseModifier;
import nl.requios.effortlessbuilding.buildmodifier.Mirror;
import nl.requios.effortlessbuilding.buildmodifier.RadialMirror;
import nl.requios.effortlessbuilding.create.foundation.gui.AbstractSimiScreen;
import nl.requios.effortlessbuilding.create.foundation.gui.AllIcons;
import nl.requios.effortlessbuilding.create.foundation.gui.widget.BoxWidget;
import nl.requios.effortlessbuilding.create.foundation.utility.Components;

import javax.annotation.Nonnull;
import java.util.Collections;

@OnlyIn(Dist.CLIENT)
public class ModifiersScreen extends AbstractSimiScreen {
	protected ModifiersScreenList list;
	protected BoxWidget addArrayButton;
	protected BoxWidget addMirrorButton;
	protected BoxWidget addRadialMirrorButton;
	protected BoxWidget closeButton;

	public ModifiersScreen() {
		super(Component.translatable("effortlessbuilding.screen.modifier_settings"));
	}

	@Override
	//Create buttons and labels and add them to buttonList/labelList
	protected void init() {
		super.init();

		int listWidth = Math.min(width - 80, 300);
		int yCenter = height / 2;
		int listL = this.width / 2 - listWidth / 2;
		int listR = this.width / 2 + listWidth / 2;

		list = new ModifiersScreenList(minecraft, listWidth, height - 80, 45, height - 45, 68);
		list.setLeftPos(this.width / 2 - list.getWidth() / 2);

		addRenderableWidget(list);

		initScrollEntries();

		addArrayButton = new BoxWidget(listR - 90, 10, 20, 20)
			.withPadding(2, 2)
			.withCallback(() -> addModifier(new Array()));
		addArrayButton.showingElement(AllIcons.I_ADD.asStencil().withElementRenderer(BoxWidget.gradientFactory.apply(addArrayButton)));
		addArrayButton.getToolTip().add(Components.literal("Add Array"));
		
		addMirrorButton = new BoxWidget(listR - 60, 10, 20, 20)
			.withPadding(2, 2)
			.withCallback(() -> addModifier(new Mirror()));
		addMirrorButton.showingElement(AllIcons.I_ADD.asStencil().withElementRenderer(BoxWidget.gradientFactory.apply(addMirrorButton)));
		addMirrorButton.getToolTip().add(Components.literal("Add Mirror"));
		
		addRadialMirrorButton = new BoxWidget(listR - 30, 10, 20, 20)
			.withPadding(2, 2)
			.withCallback(() -> addModifier(new RadialMirror()));
		addRadialMirrorButton.showingElement(AllIcons.I_ADD.asStencil().withElementRenderer(BoxWidget.gradientFactory.apply(addRadialMirrorButton)));
		addRadialMirrorButton.getToolTip().add(Components.literal("Add Radial Mirror"));

		closeButton = new BoxWidget(listL - 30, yCenter - 10, 20, 20)
			.withPadding(2, 2)
			.withCallback(this::onClose);
		closeButton.showingElement(AllIcons.I_CONFIG_BACK.asStencil().withElementRenderer(BoxWidget.gradientFactory.apply(closeButton)));
		closeButton.getToolTip().add(Components.literal("Close"));

		addRenderableWidget(addArrayButton);
		addRenderableWidget(addMirrorButton);
		addRenderableWidget(addRadialMirrorButton);
		addRenderableWidget(closeButton);
	}

	private void initScrollEntries() {

		list.children().clear();
		var modifierSettingsList = EffortlessBuildingClient.BUILD_MODIFIERS.getModifierSettingsList();
		for (BaseModifier modifier : modifierSettingsList) {
			var entry = createModifierPanel(modifier);
			list.children().add(entry);
		}
	}

	private BaseModifierEntry createModifierPanel(BaseModifier modifier) {
		if (modifier instanceof Mirror) {
			return new MirrorEntry(this, modifier);
		} else if (modifier instanceof Array) {
			return new ArrayEntry(this, modifier);
		} else if (modifier instanceof RadialMirror) {
			return new RadialMirrorEntry(this, modifier);
		} else {
			return null;
		}
	}

	private void addModifier(BaseModifier modifier) {
		var entry = createModifierPanel(modifier);
		list.children().add(entry);
		EffortlessBuildingClient.BUILD_MODIFIERS.addModifierSettings(modifier);
	}
	
	public void removeModifier(BaseModifierEntry entry) {
		list.children().remove(entry);
		EffortlessBuildingClient.BUILD_MODIFIERS.removeModifierSettings(entry.modifier);
	}
	
	public boolean canMoveUp(BaseModifierEntry modifierEntry) {
		int index = list.children().indexOf(modifierEntry);
		return index > 0;
	}
	
	public boolean canMoveDown(BaseModifierEntry modifierEntry) {
		int index = list.children().indexOf(modifierEntry);
		return index < list.children().size() - 1;
	}
	
	public void moveModifierUp(BaseModifierEntry modifierEntry) {
		int index = list.children().indexOf(modifierEntry);
		if (index == 0) return;
		
		Collections.swap(list.children(), index, index - 1);
		EffortlessBuildingClient.BUILD_MODIFIERS.moveUp(modifierEntry.modifier);
	}
	
	public void moveModifierDown(BaseModifierEntry modifierEntry) {
		int index = list.children().indexOf(modifierEntry);
		if (index == list.children().size() - 1) return;
		
		Collections.swap(list.children(), index, index + 1);
		EffortlessBuildingClient.BUILD_MODIFIERS.moveDown(modifierEntry.modifier);
	}
	
	@Override
	public void resize(@Nonnull Minecraft client, int width, int height) {
		double scroll = list.getScrollAmount();
		init(client, width, height);
		list.setScrollAmount(scroll);
	}
	
	@Override
	protected void renderWindow(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
	
	}
	
	@Override
	public void onClose() {
		super.onClose();
		EffortlessBuildingClient.BUILD_MODIFIERS.save();
	}
	
	@Override
	public boolean keyPressed(int keyCode, int p_96553_, int p_96554_) {
		if (keyCode == ClientEvents.keyBindings[1].getKey().getValue()) {
			onClose();
			return true;
		}

		return super.keyPressed(keyCode, p_96553_, p_96554_);
	}
}
