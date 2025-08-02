package io.github.drag0n1zed.universal.api.networking;

import java.util.UUID;

public interface ResponsiblePacket<T extends PacketListener> extends Packet<T> {

    UUID responseId();

}
