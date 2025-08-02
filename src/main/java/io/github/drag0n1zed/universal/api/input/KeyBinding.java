package io.github.drag0n1zed.universal.api.input;

import io.github.drag0n1zed.universal.api.platform.ClientContentFactory;
import io.github.drag0n1zed.universal.api.platform.PlatformReference;

public interface KeyBinding extends PlatformReference {

    static KeyBinding of(String name, String category, int code) {
        return ClientContentFactory.getInstance().newKeyBinding(name, category, code);
    }

    String getName();

    String getCategory();

    Key getDefaultKey();

    Key getKey();

    boolean consumeClick();

    boolean isDown();

    default boolean isKeyDown() {
        return getKey().isDown();
    }

}
