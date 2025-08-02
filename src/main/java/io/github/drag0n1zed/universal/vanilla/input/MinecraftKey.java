package io.github.drag0n1zed.universal.vanilla.input;

import com.mojang.blaze3d.platform.InputConstants;

import io.github.drag0n1zed.universal.api.input.Key;

public record MinecraftKey(
        InputConstants.Key refs
) implements Key {

    @Override
    public String getName() {
        return refs.getName();
    }

    @Override
    public int getValue() {
        return refs.getValue();
    }
}
