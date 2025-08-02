package io.github.drag0n1zed.schema.networking.packets.session;

import io.github.drag0n1zed.universal.api.core.Player;
import io.github.drag0n1zed.universal.api.networking.NetByteBuf;
import io.github.drag0n1zed.universal.api.networking.NetByteBufSerializer;
import io.github.drag0n1zed.universal.api.networking.Packet;
import io.github.drag0n1zed.schema.networking.packets.AllPacketListener;
import io.github.drag0n1zed.schema.networking.serializer.SessionSerializer;
import io.github.drag0n1zed.schema.session.Session;

public record SessionPacket(
        Session session
) implements Packet<AllPacketListener> {

    @Override
    public void handle(AllPacketListener packetListener, Player sender) {
        packetListener.handle(this, sender);
    }

    public static class Serializer implements NetByteBufSerializer<SessionPacket> {

        @Override
        public SessionPacket read(NetByteBuf byteBuf) {
            return new SessionPacket(byteBuf.read(new SessionSerializer()));
        }

        @Override
        public void write(NetByteBuf byteBuf, SessionPacket packet) {
            byteBuf.write(packet.session(), new SessionSerializer());
        }

    }
}
