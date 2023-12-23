package nl.requios.effortlessbuilding.gui.buildmodifier;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import nl.requios.effortlessbuilding.AllGuiTextures;
import nl.requios.effortlessbuilding.buildmodifier.BaseModifier;
import nl.requios.effortlessbuilding.create.foundation.gui.widget.Label;
import nl.requios.effortlessbuilding.create.foundation.utility.Components;
import nl.requios.effortlessbuilding.gui.elements.MiniButton;

public abstract class BaseModifierEntry<T extends BaseModifier> extends ModifiersScreenList.Entry {

    public T modifier;
    protected AllGuiTextures background;
    protected MiniButton enableButton;
    protected Label nameLabel;
    protected MiniButton moveUpButton;
    protected MiniButton moveDownButton;
    protected MiniButton removeButton;
    protected Label reachLabel;
    
    protected static final int BACKGROUND_WIDTH = 226;
    protected static final int BACKGROUND_HEIGHT = 60;
    protected int left = 0;
    protected int right = 0;
    protected int top = 0;
    protected int bottom = 0;
    

    public BaseModifierEntry(ModifiersScreen screen, T modifier, Component name, AllGuiTextures background) {
        super(screen);

        this.modifier = modifier;
        this.background = background;

        enableButton = new MiniButton(0, 0, 100, 9)
            .showing(AllGuiTextures.ENABLE_BUTTON_BACKGROUND)
            .withCallback(() -> {
                modifier.enabled = !modifier.enabled;
                onValueChanged();
            });
        listeners.add(enableButton);
        
        nameLabel = new Label(65, 8, name);
        nameLabel.text = name;
        
        moveUpButton = new MiniButton(0, 0, 9, 9)
            .showing(AllGuiTextures.ARROW_UP)
            .withCallback(() -> {
                screen.moveModifierUp(this);
                onValueChanged();
            });
        moveUpButton.setToolTip(Components.literal("Move up"));
        listeners.add(moveUpButton);
        
        moveDownButton = new MiniButton(0, 0, 9, 9)
            .showing(AllGuiTextures.ARROW_DOWN)
            .withCallback(() -> {
                screen.moveModifierDown(this);
                onValueChanged();
            });
        moveDownButton.setToolTip(Components.literal("Move down"));
        listeners.add(moveDownButton);
        
        removeButton = new MiniButton(0, 0, 9, 9)
            .showing(AllGuiTextures.TRASH)
            .withCallback(() -> {
                screen.removeModifier(this);
            });
        removeButton.setToolTip(Components.literal("Remove"));
        listeners.add(removeButton);
        
        reachLabel = new Label(0, 0, Components.immutableEmpty()).withShadow();
        listeners.add(reachLabel);
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void render(PoseStack ms, int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean p_230432_9_, float partialTicks) {

        left = x + width / 2 - BACKGROUND_WIDTH / 2;
        right = x + width / 2 + BACKGROUND_WIDTH / 2;
        top = y;
        bottom = y + BACKGROUND_HEIGHT;
        
        background.render(ms, left, top);
        
        enableButton.setX(left + 4);
        enableButton.setY(top + 3);
        enableButton.render(ms, mouseX, mouseY, partialTicks);
        if (modifier.enabled)
            AllGuiTextures.CHECKMARK.render(ms, left + 5, top + 3);
        
        nameLabel.setX(left + 18);
        nameLabel.setY(top + 4);
        nameLabel.render(ms, mouseX, mouseY, partialTicks);
    
        moveUpButton.visible = screen.canMoveUp(this);
        moveDownButton.visible = screen.canMoveDown(this);
        
        moveUpButton.setX(right - 31);
        moveUpButton.setY(top + 3);
        moveUpButton.render(ms, mouseX, mouseY, partialTicks);
        
        moveDownButton.setX(right - 22);
        moveDownButton.setY(top + 3);
        moveDownButton.render(ms, mouseX, mouseY, partialTicks);
        
        removeButton.setX(right - 13);
        removeButton.setY(top + 3);
        removeButton.render(ms, mouseX, mouseY, partialTicks);
    }

    public void onValueChanged() {
        if (modifier.enabled)
            enableButton.setToolTip(Components.literal("Disable this modifier"));
        else
            enableButton.setToolTip(Components.literal("Enable this modifier"));
    }
}
