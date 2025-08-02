package io.github.drag0n1zed.universal.api.events.lifecycle;

import io.github.drag0n1zed.universal.api.platform.Client;

@FunctionalInterface
public interface ClientStart {
    void onClientStart(Client client);
}
