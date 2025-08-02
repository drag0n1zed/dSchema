package io.github.drag0n1zed.universal.api.core;

import java.util.UUID;

import io.github.drag0n1zed.universal.api.platform.PlatformReference;
import io.github.drag0n1zed.universal.api.text.Text;

public interface PlayerInfo extends PlatformReference {

    PlayerProfile getProfile();

    Text getDisplayName();

    PlayerSkin getSkin();

    default UUID getId() {
        return getProfile().getId();
    }

    default String getName() {
        return getProfile().getName();
    }

}
