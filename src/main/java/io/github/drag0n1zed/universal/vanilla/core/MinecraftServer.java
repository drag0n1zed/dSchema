package io.github.drag0n1zed.universal.vanilla.core;

import io.github.drag0n1zed.universal.api.core.PlayerProfile;
import io.github.drag0n1zed.universal.api.platform.PlayerList;
import io.github.drag0n1zed.universal.api.platform.Server;

public record MinecraftServer(
        net.minecraft.server.MinecraftServer refs
) implements Server {

    @Override
    public PlayerList getPlayerList() {
        return new MinecraftPlayerList(refs.getPlayerList());
    }

    @Override
    public void execute(Runnable runnable) {
        refs.execute(runnable);
    }

    @Override
    public boolean isSinglePlayerOwner(PlayerProfile profile) {
        return refs.isSingleplayerOwner(profile.reference());
    }

    @Override
    public boolean isDedicatedServer() {
        return refs.isDedicatedServer();
    }

}
