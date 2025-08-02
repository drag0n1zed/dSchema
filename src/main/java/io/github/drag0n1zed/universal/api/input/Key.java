package io.github.drag0n1zed.universal.api.input;

import io.github.drag0n1zed.universal.api.platform.ClientEntrance;
import io.github.drag0n1zed.universal.api.platform.PlatformReference;
import io.github.drag0n1zed.universal.api.text.Text;

public interface Key extends PlatformReference {

    String getName();

    int getValue();

    default boolean isDown() {
        return ClientEntrance.getInstance().getClient().getWindow().isKeyDown(getValue());
    }

    default Text getNameText() {
        return Text.translate(getName());
    }

}
