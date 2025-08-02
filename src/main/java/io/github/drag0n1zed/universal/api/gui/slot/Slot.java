package io.github.drag0n1zed.universal.api.gui.slot;

import io.github.drag0n1zed.universal.api.gui.AbstractWidget;
import io.github.drag0n1zed.universal.api.platform.Entrance;
import io.github.drag0n1zed.universal.api.text.Text;

public abstract class Slot extends AbstractWidget {

    public Slot(Entrance entrance, int x, int y, int width, int height, Text message) {
        super(entrance, x, y, width, height, message);
    }

    public abstract int getFullWidth();

}
