package io.github.drag0n1zed.dschema.networking.packets.player;

import java.util.UUID;

import io.github.drag0n1zed.universal.api.core.Player;
import io.github.drag0n1zed.universal.api.networking.NetByteBuf;
import io.github.drag0n1zed.universal.api.networking.NetByteBufSerializer;
import io.github.drag0n1zed.universal.api.networking.Packet;
import io.github.drag0n1zed.dschema.building.clipboard.Snapshot;
import io.github.drag0n1zed.dschema.networking.packets.AllPacketListener;
import io.github.drag0n1zed.dschema.networking.serializer.SnapshotSerializer;

public record PlayerSnapshotCapturePacket(
        UUID uuid,
        Snapshot snapshot
) implements Packet<AllPacketListener> {

    @Override
    public void handle(AllPacketListener packetListener, Player sender) {
        packetListener.handle(this, sender);
    }

    public static class Serializer implements NetByteBufSerializer<PlayerSnapshotCapturePacket> {

        @Override
        public PlayerSnapshotCapturePacket read(NetByteBuf byteBuf) {
            return new PlayerSnapshotCapturePacket(byteBuf.readUUID(), byteBuf.read(new SnapshotSerializer()));
        }

        @Override
        public void write(NetByteBuf byteBuf, PlayerSnapshotCapturePacket packet) {
            byteBuf.writeUUID(packet.uuid());
            byteBuf.write(packet.snapshot(), new SnapshotSerializer());
        }

    }

}
