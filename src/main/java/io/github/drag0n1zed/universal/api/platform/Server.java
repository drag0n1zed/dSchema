package io.github.drag0n1zed.universal.api.platform;

import io.github.drag0n1zed.universal.api.core.PlayerProfile;

public interface Server extends PlatformReference {

    PlayerList getPlayerList();

    void execute(Runnable runnable);

    default boolean isOperator(PlayerProfile profile) {
        return getPlayerList().isOperator(profile);
    }

    boolean isSinglePlayerOwner(PlayerProfile profile);

    boolean isDedicatedServer();

}
