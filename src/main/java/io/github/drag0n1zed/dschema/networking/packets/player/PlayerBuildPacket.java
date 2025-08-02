package io.github.drag0n1zed.dschema.networking.packets.player;

import java.util.UUID;

import io.github.drag0n1zed.universal.api.core.Player;
import io.github.drag0n1zed.universal.api.networking.NetByteBuf;
import io.github.drag0n1zed.universal.api.networking.NetByteBufSerializer;
import io.github.drag0n1zed.universal.api.networking.Packet;
import io.github.drag0n1zed.dschema.building.Context;
import io.github.drag0n1zed.dschema.networking.packets.AllPacketListener;
import io.github.drag0n1zed.dschema.networking.serializer.ContextSerializer;

public record PlayerBuildPacket(
        UUID playerId,
        Context context
) implements Packet<AllPacketListener> {

    public static PlayerBuildPacket by(Player player, Context context) {
        return new PlayerBuildPacket(player.getId(), context);
    }

    @Override
    public void handle(AllPacketListener packetListener, Player sender) {
        packetListener.handle(this, sender);
    }

    public static class Serializer implements NetByteBufSerializer<PlayerBuildPacket> {

        @Override
        public PlayerBuildPacket read(NetByteBuf byteBuf) {
            return new PlayerBuildPacket(byteBuf.readUUID(), byteBuf.read(new ContextSerializer()));
        }

        @Override
        public void write(NetByteBuf byteBuf, PlayerBuildPacket packet) {
            byteBuf.writeUUID(packet.playerId());
            byteBuf.write(packet.context(), new ContextSerializer());
        }

    }

}
