package io.github.drag0n1zed.universal.api.networking;

public interface PacketListener {

    default boolean shouldPropagateHandlingExceptions() {
        return true;
    }

}
