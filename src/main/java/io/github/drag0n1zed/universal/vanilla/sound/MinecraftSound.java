package io.github.drag0n1zed.universal.vanilla.sound;

import io.github.drag0n1zed.universal.api.core.ResourceLocation;
import io.github.drag0n1zed.universal.api.sound.Sound;
import io.github.drag0n1zed.universal.vanilla.core.MinecraftResourceLocation;
import net.minecraft.sounds.SoundEvent;

public record MinecraftSound(
    SoundEvent refs
) implements Sound {

    @Override
    public ResourceLocation getId() {
        return new MinecraftResourceLocation(refs.getLocation());
    }
}
