package io.github.drag0n1zed.universal.api.platform;

import io.github.drag0n1zed.universal.api.events.impl.EventRegistry;
import io.github.drag0n1zed.universal.api.networking.NetworkChannel;

import java.util.Optional;

public interface Entrance {

    int PROTOCOL_VERSION = 0;

    static Entrance getInstance() {
        return PlatformLoader.getSingleton();
    }

    String getId();

    EventRegistry getEventRegistry();

    NetworkChannel<?> getChannel();

    ServerManager getServerManager();

    default Server getServer() {
        return getServerManager().getRunningServer();
    }

    default <T extends Plugin> Optional<T> findPlugin(Class<T> pluginClass) {
        try {
            return Optional.of(PlatformLoader.getSingleton(pluginClass));
        } catch (LinkageError e) {
            return Optional.empty();
        }
    }

}

