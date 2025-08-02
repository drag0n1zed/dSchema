package io.github.drag0n1zed.universal.vanilla.core;

import io.github.drag0n1zed.universal.api.core.ResourceKey;
import io.github.drag0n1zed.universal.api.core.ResourceLocation;

public record MinecraftResourceKey<T>(
        net.minecraft.resources.ResourceKey<?> refs
) implements ResourceKey<T> {

    @Override
    public ResourceLocation registry() {
        return new MinecraftResourceLocation(refs.registry());
    }

    @Override
    public ResourceLocation location() {
        return new MinecraftResourceLocation(refs.location());
    }

}
