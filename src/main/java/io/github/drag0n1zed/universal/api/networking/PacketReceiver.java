package io.github.drag0n1zed.universal.api.networking;

import io.github.drag0n1zed.universal.api.core.Player;

public interface PacketReceiver extends ByteBufReceiver {

    void receivePacket(Packet packet, Player player);

}
