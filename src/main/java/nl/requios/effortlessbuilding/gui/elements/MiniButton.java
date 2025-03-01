package nl.requios.effortlessbuilding.gui.elements;

import net.createmod.catnip.gui.widget.ElementWidget;
import net.minecraft.network.chat.Component;

public class MiniButton extends ElementWidget {
    public MiniButton(int x, int y) {
        super(x, y);
    }
    
    public MiniButton(int x, int y, int width, int height) {
        super(x, y, width, height);
    }
    
    public void setToolTip(Component text) {
        toolTip.clear();
        toolTip.add(text);
    }
}
