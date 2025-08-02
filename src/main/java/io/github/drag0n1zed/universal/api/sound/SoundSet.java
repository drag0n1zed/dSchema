package io.github.drag0n1zed.universal.api.sound;

import io.github.drag0n1zed.universal.api.platform.PlatformReference;

public interface SoundSet extends PlatformReference {

    float volume();

    float pitch();

    Sound breakSound();

    Sound stepSound();

    Sound placeSound();

    Sound hitSound();

    Sound fallSound();

}
