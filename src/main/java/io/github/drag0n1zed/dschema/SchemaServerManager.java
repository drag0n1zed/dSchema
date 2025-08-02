package io.github.drag0n1zed.dschema;

import java.util.concurrent.atomic.AtomicReference;

import io.github.drag0n1zed.universal.api.platform.Server;
import io.github.drag0n1zed.universal.api.platform.ServerManager;

public final class SchemaServerManager implements ServerManager {

    private final Schema entrance;

    private final AtomicReference<Server> runningServer = new AtomicReference<>();

    private int interactionCooldown = 0;

    public SchemaServerManager(Schema entrance) {
        this.entrance = entrance;

        getEntrance().getEventRegistry().getServerStartedEvent().register(this::onServerStarted);
        getEntrance().getEventRegistry().getServerStoppedEvent().register(this::onServerStarted);
    }

    private Schema getEntrance() {
        return entrance;
    }

    public void onServerStarted(Server server) {
        this.runningServer.set(server);
    }

    public void onServerStopped(Server server) {
        this.runningServer.set(null);
    }

    public Server getRunningServer() {
        return runningServer.get();
    }

}
