package io.github.drag0n1zed.schema.networking.packets.player;

import java.util.UUID;

import io.github.drag0n1zed.universal.api.core.Player;
import io.github.drag0n1zed.universal.api.networking.NetByteBuf;
import io.github.drag0n1zed.universal.api.networking.NetByteBufSerializer;
import io.github.drag0n1zed.universal.api.networking.ResponsiblePacket;
import io.github.drag0n1zed.schema.building.SingleCommand;
import io.github.drag0n1zed.schema.networking.packets.AllPacketListener;

public record PlayerCommandPacket(
        UUID responseId,
        SingleCommand action
) implements ResponsiblePacket<AllPacketListener> {

    public PlayerCommandPacket(
            SingleCommand action
    ) {
        this(UUID.randomUUID(), action);
    }

    @Override
    public void handle(AllPacketListener packetListener, Player sender) {
        packetListener.handle(this, sender);
    }

    public static class Serializer implements NetByteBufSerializer<PlayerCommandPacket> {

        @Override
        public PlayerCommandPacket read(NetByteBuf byteBuf) {
            return new PlayerCommandPacket(byteBuf.readUUID(), byteBuf.readEnum(SingleCommand.class));
        }

        @Override
        public void write(NetByteBuf byteBuf, PlayerCommandPacket packet) {
            byteBuf.writeUUID(packet.responseId());
            byteBuf.writeEnum(packet.action());
        }

    }

}
