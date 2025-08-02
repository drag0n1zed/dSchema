package io.github.drag0n1zed.universal.api.platform;

import java.util.List;
import java.util.UUID;

import io.github.drag0n1zed.universal.api.core.Player;
import io.github.drag0n1zed.universal.api.core.PlayerProfile;

public interface PlayerList extends PlatformReference {

    List<Player> getPlayers();

    Player getPlayer(UUID uuid);

    boolean isOperator(PlayerProfile profile);

}
