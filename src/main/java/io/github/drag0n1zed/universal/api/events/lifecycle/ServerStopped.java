package io.github.drag0n1zed.universal.api.events.lifecycle;

import io.github.drag0n1zed.universal.api.platform.Server;

@FunctionalInterface
public interface ServerStopped {
    void onServerStopped(Server server);
}
