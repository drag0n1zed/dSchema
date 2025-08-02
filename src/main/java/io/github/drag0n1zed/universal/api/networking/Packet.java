package io.github.drag0n1zed.universal.api.networking;

import io.github.drag0n1zed.universal.api.core.Player;

public interface Packet<T extends PacketListener> {

    void handle(T packetListener, Player sender);

}
