package io.github.drag0n1zed.universal.vanilla.input;

import io.github.drag0n1zed.universal.api.input.Key;
import io.github.drag0n1zed.universal.api.input.KeyBinding;
import net.minecraft.client.KeyMapping;

public record MinecraftKeyBinding(
        KeyMapping refs
) implements KeyBinding {

    @Override
    public String getName() {
        return refs.getName();
    }

    @Override
    public String getCategory() {
        return refs.getCategory();
    }

    @Override
    public Key getDefaultKey() {
        return new MinecraftKey(refs.getDefaultKey());
    }

    @Override
    public Key getKey() {
        return new MinecraftKey(refs.key);
    }

    @Override
    public boolean consumeClick() {
        return refs.consumeClick();
    }

    @Override
    public boolean isDown() {
        return refs.isDown();
    }

}
