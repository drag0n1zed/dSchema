package io.github.drag0n1zed.universal.api.events.impl;

import io.github.drag0n1zed.universal.api.events.Event;
import io.github.drag0n1zed.universal.api.events.EventHolder;
import io.github.drag0n1zed.universal.api.events.lifecycle.ServerStarted;
import io.github.drag0n1zed.universal.api.events.lifecycle.ServerStarting;
import io.github.drag0n1zed.universal.api.events.lifecycle.ServerStopped;
import io.github.drag0n1zed.universal.api.events.lifecycle.ServerStopping;
import io.github.drag0n1zed.universal.api.events.networking.RegisterNetwork;
import io.github.drag0n1zed.universal.api.events.player.PlayerChangeWorld;
import io.github.drag0n1zed.universal.api.events.player.PlayerLoggedIn;
import io.github.drag0n1zed.universal.api.events.player.PlayerLoggedOut;
import io.github.drag0n1zed.universal.api.events.player.PlayerRespawn;

public class EventRegistry extends EventHolder {

    public Event<RegisterNetwork> getRegisterNetworkEvent() {
        return get();
    }

    public Event<PlayerChangeWorld> getPlayerChangeWorldEvent() {
        return get();
    }

    public Event<PlayerRespawn> getPlayerRespawnEvent() {
        return get();
    }

    public Event<PlayerLoggedIn> getPlayerLoggedInEvent() {
        return get();
    }

    public Event<PlayerLoggedOut> getPlayerLoggedOutEvent() {
        return get();
    }

    public Event<ServerStarting> getServerStartingEvent() {
        return get();
    }

    public Event<ServerStarted> getServerStartedEvent() {
        return get();
    }

    public Event<ServerStopping> getServerStoppingEvent() {
        return get();
    }

    public Event<ServerStopped> getServerStoppedEvent() {
        return get();
    }

}
