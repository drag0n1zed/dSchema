package io.github.drag0n1zed.dschema.networking.packets.session;

import io.github.drag0n1zed.universal.api.core.Player;
import io.github.drag0n1zed.universal.api.networking.NetByteBuf;
import io.github.drag0n1zed.universal.api.networking.NetByteBufSerializer;
import io.github.drag0n1zed.universal.api.networking.Packet;
import io.github.drag0n1zed.dschema.networking.packets.AllPacketListener;
import io.github.drag0n1zed.dschema.networking.serializer.SessionConfigSerializer;
import io.github.drag0n1zed.dschema.session.config.SessionConfig;

public record SessionConfigPacket(
        SessionConfig sessionConfig
) implements Packet<AllPacketListener> {

    @Override
    public void handle(AllPacketListener packetListener, Player sender) {
        packetListener.handle(this, sender);
    }

    public static class Serializer implements NetByteBufSerializer<SessionConfigPacket> {

        @Override
        public SessionConfigPacket read(NetByteBuf byteBuf) {
            return new SessionConfigPacket(byteBuf.read(new SessionConfigSerializer()));
        }

        @Override
        public void write(NetByteBuf byteBuf, SessionConfigPacket packet) {
            byteBuf.write(packet.sessionConfig(), new SessionConfigSerializer());
        }

    }
}
