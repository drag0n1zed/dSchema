package io.github.drag0n1zed.universal.api.events.networking;

import io.github.drag0n1zed.universal.api.networking.NetworkRegistry;

@FunctionalInterface
public interface RegisterNetwork {
    void onRegisterNetwork(NetworkRegistry registry);
}
