package io.github.drag0n1zed.universal.api.sound;

import io.github.drag0n1zed.universal.api.platform.ContentFactory;

public enum Sounds {
    UI_BUTTON_CLICK,
    UI_TOAST_IN,
    UI_TOAST_OUT,
    ;

    public Sound sound() {
        return ContentFactory.getInstance().getSound(this);
    }
}
