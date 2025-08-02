package io.github.drag0n1zed.universal.api.events.lifecycle;

import io.github.drag0n1zed.universal.api.platform.Client;

@FunctionalInterface
public interface ClientTick {
    void onClientTick(Client client, Phase phase);

    enum Phase {
        START,
        END
    }
}
