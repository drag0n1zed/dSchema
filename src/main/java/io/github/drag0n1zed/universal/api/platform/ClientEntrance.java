package io.github.drag0n1zed.universal.api.platform;

import io.github.drag0n1zed.universal.api.events.impl.ClientEventRegistry;

public interface ClientEntrance extends Entrance {

    static ClientEntrance getInstance() {
        return PlatformLoader.getSingleton();
    }

    ClientManager getClientManager();

    ClientEventRegistry getEventRegistry();

    default Client getClient() {
        return getClientManager().getRunningClient();
    }

    @Override
    default ServerManager getServerManager() {
        throw new UnsupportedOperationException();
    }

}

