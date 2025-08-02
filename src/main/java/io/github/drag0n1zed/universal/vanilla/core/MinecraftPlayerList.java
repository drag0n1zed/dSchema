package io.github.drag0n1zed.universal.vanilla.core;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import io.github.drag0n1zed.universal.api.core.Player;
import io.github.drag0n1zed.universal.api.core.PlayerProfile;
import io.github.drag0n1zed.universal.api.platform.PlayerList;

public record MinecraftPlayerList(
        net.minecraft.server.players.PlayerList refs
) implements PlayerList {

    @Override
    public List<Player> getPlayers() {
        return refs.getPlayers().stream().map(MinecraftPlayer::ofNullable).collect(Collectors.toList());
    }

    @Override
    public Player getPlayer(UUID uuid) {
        return MinecraftPlayer.ofNullable(refs.getPlayer(uuid));
    }

    @Override
    public boolean isOperator(PlayerProfile profile) {
        return refs.isOp(profile.reference());
    }

}
