package io.github.drag0n1zed.schema.networking.packets.player;

import io.github.drag0n1zed.universal.api.core.Player;
import io.github.drag0n1zed.universal.api.networking.NetByteBuf;
import io.github.drag0n1zed.universal.api.networking.NetByteBufSerializer;
import io.github.drag0n1zed.universal.api.networking.Packet;
import io.github.drag0n1zed.schema.networking.packets.AllPacketListener;

public record PlayerSettingsPacket(
) implements Packet<AllPacketListener> {

    @Override
    public void handle(AllPacketListener packetListener, Player sender) {
        packetListener.handle(this, sender);
    }

    public static class Serializer implements NetByteBufSerializer<PlayerSettingsPacket> {

        @Override
        public PlayerSettingsPacket read(NetByteBuf byteBuf) {
            return new PlayerSettingsPacket();
        }

        @Override
        public void write(NetByteBuf byteBuf, PlayerSettingsPacket packet) {
        }

    }

}
