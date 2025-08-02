package io.github.drag0n1zed.schema.networking.packets.player;

import io.github.drag0n1zed.universal.api.core.Player;
import io.github.drag0n1zed.universal.api.networking.NetByteBuf;
import io.github.drag0n1zed.universal.api.networking.NetByteBufSerializer;
import io.github.drag0n1zed.universal.api.networking.Packet;
import io.github.drag0n1zed.schema.building.clipboard.Snapshot;
import io.github.drag0n1zed.schema.networking.packets.AllPacketListener;
import io.github.drag0n1zed.schema.networking.serializer.SnapshotSerializer;

import java.util.UUID;

public record PlayerSnapshotSharePacket(
        UUID from,
        UUID to,
        Snapshot snapshot
) implements Packet<AllPacketListener> {

    @Override
    public void handle(AllPacketListener packetListener, Player sender) {
        packetListener.handle(this, sender);
    }

    public static class Serializer implements NetByteBufSerializer<PlayerSnapshotSharePacket> {

        @Override
        public PlayerSnapshotSharePacket read(NetByteBuf byteBuf) {
            return new PlayerSnapshotSharePacket(byteBuf.readUUID(), byteBuf.readUUID(), byteBuf.read(new SnapshotSerializer()));
        }

        @Override
        public void write(NetByteBuf byteBuf, PlayerSnapshotSharePacket packet) {
            byteBuf.writeUUID(packet.from());
            byteBuf.writeUUID(packet.to());
            byteBuf.write(packet.snapshot(), new SnapshotSerializer());
        }

    }

}
