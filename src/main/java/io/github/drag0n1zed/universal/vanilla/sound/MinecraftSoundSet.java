package io.github.drag0n1zed.universal.vanilla.sound;

import io.github.drag0n1zed.universal.api.sound.Sound;
import io.github.drag0n1zed.universal.api.sound.SoundSet;
import net.minecraft.world.level.block.SoundType;

public record MinecraftSoundSet(
        SoundType refs
) implements SoundSet {


    @Override
    public float volume() {
        return refs.getVolume();
    }

    @Override
    public float pitch() {
        return refs.getPitch();
    }

    @Override
    public Sound breakSound() {
        return new MinecraftSound(refs.getBreakSound());
    }

    @Override
    public Sound stepSound() {
        return new MinecraftSound(refs.getStepSound());
    }

    @Override
    public Sound placeSound() {
        return new MinecraftSound(refs.getPlaceSound());
    }

    @Override
    public Sound hitSound() {
        return new MinecraftSound(refs.getHitSound());
    }

    @Override
    public Sound fallSound() {
        return new MinecraftSound(refs.getFallSound());
    }
}
