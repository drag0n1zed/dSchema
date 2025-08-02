package io.github.drag0n1zed.universal.api.events.player;

import io.github.drag0n1zed.universal.api.core.Player;

@FunctionalInterface
public interface PlayerLoggedOut {
    void onPlayerLoggedOut(Player player);
}
