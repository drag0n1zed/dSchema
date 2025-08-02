package io.github.drag0n1zed.universal.api.events.input;

import io.github.drag0n1zed.universal.api.input.KeyBindingOwner;

public interface KeyRegistry {

    void register(KeyBindingOwner key);
}
