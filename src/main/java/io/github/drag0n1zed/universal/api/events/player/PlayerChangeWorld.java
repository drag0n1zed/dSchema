package io.github.drag0n1zed.universal.api.events.player;

import io.github.drag0n1zed.universal.api.core.Player;
import io.github.drag0n1zed.universal.api.core.World;

@FunctionalInterface
public interface PlayerChangeWorld {
    void onPlayerChangeWorld(Player player, World origin, World destination);
}
