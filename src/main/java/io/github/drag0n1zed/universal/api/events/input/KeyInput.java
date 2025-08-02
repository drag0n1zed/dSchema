package io.github.drag0n1zed.universal.api.events.input;

import io.github.drag0n1zed.universal.api.input.InputKey;

@FunctionalInterface
public interface KeyInput {
    void onKeyInput(InputKey key);
}
