package io.github.drag0n1zed.universal.api.events.player;

import io.github.drag0n1zed.universal.api.core.Player;

@FunctionalInterface
public interface PlayerRespawn {
    void onPlayerRespawn(Player oldPlayer, Player newPlayer, boolean alive);
}
