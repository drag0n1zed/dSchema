package io.github.drag0n1zed.universal.forge.networking;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.commons.lang3.tuple.Pair;

import com.google.auto.service.AutoService;

import io.github.drag0n1zed.universal.api.core.ResourceLocation;
import io.github.drag0n1zed.universal.api.networking.ByteBufReceiver;
import io.github.drag0n1zed.universal.api.networking.ByteBufSender;
import io.github.drag0n1zed.universal.api.networking.Networking;
import io.github.drag0n1zed.universal.api.networking.Side;
import io.github.drag0n1zed.universal.vanilla.core.MinecraftPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.event.EventNetworkChannel;

@AutoService(Networking.class)
public class ForgeNetworking implements Networking {

    private static final Map<ResourceLocation, EventNetworkChannel> MAP = new HashMap<>();

    private static void register(ResourceLocation channelId, Consumer<NetworkEvent> eventConsumer) {
        MAP.computeIfAbsent(channelId, id -> {
            return NetworkRegistry.ChannelBuilder.named(channelId.reference())
                    .networkProtocolVersion(() -> "0")
                    .clientAcceptedVersions(s -> true)
                    .serverAcceptedVersions(s -> true)
                    .eventNetworkChannel();
        }).addListener(eventConsumer);
    }

    public static ByteBufSender register(ResourceLocation channelId, Side side, ByteBufReceiver receiver) {
        switch (side) {
            case CLIENT -> register(channelId, event -> {
                if (event.getPayload() != null && event.getSource().get().getDirection().equals(NetworkDirection.PLAY_TO_CLIENT)) {
                    receiver.receiveBuffer(event.getPayload(), MinecraftPlayer.ofNullable(event.getSource().get().getSender()));
                    event.getSource().get().setPacketHandled(true);
                }
            });
            case SERVER -> register(channelId, event -> {
                if (event.getPayload() != null && event.getSource().get().getDirection().equals(NetworkDirection.PLAY_TO_SERVER)) {
                    receiver.receiveBuffer(event.getPayload(), MinecraftPlayer.ofNullable(event.getSource().get().getSender()));
                    event.getSource().get().setPacketHandled(true);
                }
            });
        }
        return switch (side) {
            case CLIENT ->
                    (byteBuf, player) -> PacketDistributor.SERVER.noArg().send(NetworkDirection.PLAY_TO_SERVER.buildPacket(Pair.of(new FriendlyByteBuf(byteBuf), 0), channelId.reference()).getThis());
            case SERVER ->
                    (byteBuf, player) -> PacketDistributor.PLAYER.with(player::reference).send(NetworkDirection.PLAY_TO_CLIENT.buildPacket(Pair.of(new FriendlyByteBuf(byteBuf), 0), channelId.reference()).getThis());
        };

    }

}
