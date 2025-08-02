package io.github.drag0n1zed.dschema;

import java.util.logging.Logger;

import io.github.drag0n1zed.universal.api.core.Player;
import io.github.drag0n1zed.universal.api.networking.NetworkChannel;
import io.github.drag0n1zed.universal.api.networking.Packet;
import io.github.drag0n1zed.universal.api.networking.Side;
import io.github.drag0n1zed.universal.api.text.Text;
import io.github.drag0n1zed.dschema.networking.packets.AllPacketListener;
import io.github.drag0n1zed.dschema.networking.packets.player.PlayerBuildPacket;
import io.github.drag0n1zed.dschema.networking.packets.player.PlayerBuildTooltipPacket;
import io.github.drag0n1zed.dschema.networking.packets.player.PlayerCommandPacket;
import io.github.drag0n1zed.dschema.networking.packets.player.PlayerPermissionCheckPacket;
import io.github.drag0n1zed.dschema.networking.packets.player.PlayerSettingsPacket;
import io.github.drag0n1zed.dschema.networking.packets.player.PlayerSnapshotCapturePacket;
import io.github.drag0n1zed.dschema.networking.packets.player.PlayerSnapshotSharePacket;
import io.github.drag0n1zed.dschema.networking.packets.session.SessionConfigPacket;
import io.github.drag0n1zed.dschema.networking.packets.session.SessionPacket;

public final class SchemaNetworkChannel extends NetworkChannel<AllPacketListener> {

    private static final int COMPATIBILITY_VERSION = Schema.PROTOCOL_VERSION;
    private final Schema entrance;
    private final AllPacketListener listener;

    public SchemaNetworkChannel(Schema entrance) {
        this(entrance, Schema.DEFAULT_CHANNEL);
    }

    public SchemaNetworkChannel(Schema entrance, String name) {
        super(entrance, name, Side.SERVER);
        this.entrance = entrance;
        this.listener = new ServerPacketListener();

        registerPacket(SessionPacket.class, new SessionPacket.Serializer());
        registerPacket(SessionConfigPacket.class, new SessionConfigPacket.Serializer());

        registerPacket(PlayerCommandPacket.class, new PlayerCommandPacket.Serializer());
        registerPacket(PlayerSettingsPacket.class, new PlayerSettingsPacket.Serializer());
        registerPacket(PlayerBuildPacket.class, new PlayerBuildPacket.Serializer());
        registerPacket(PlayerPermissionCheckPacket.class, new PlayerPermissionCheckPacket.Serializer());
        registerPacket(PlayerBuildTooltipPacket.class, new PlayerBuildTooltipPacket.Serializer());
        registerPacket(PlayerSnapshotCapturePacket.class, new PlayerSnapshotCapturePacket.Serializer());
        registerPacket(PlayerSnapshotSharePacket.class, new PlayerSnapshotSharePacket.Serializer());

        getEntrance().getEventRegistry().getRegisterNetworkEvent().register(this::onRegisterNetwork);
    }

    public Schema getEntrance() {
        return entrance;
    }

    @Override
    public void receivePacket(Packet packet, Player player) {
        try {
            packet.handle(listener, player);
        } catch (Exception exception) {
            if (listener.shouldPropagateHandlingExceptions()) {
                throw exception;
            }
            Logger.getAnonymousLogger().severe("Failed to handle packet " + packet + ", suppressing error" + exception);
        }
    }

    @Override
    public int getCompatibilityVersion() {
        return COMPATIBILITY_VERSION;
    }

    private class ServerPacketListener implements AllPacketListener {

        @Override
        public void handle(PlayerCommandPacket packet, Player player) {
            getEntrance().getServer().execute(() -> {
                switch (packet.action()) {
                    case REDO -> getEntrance().getStructureBuilder().redo(player);
                    case UNDO -> getEntrance().getStructureBuilder().undo(player);
                }
            });
        }

        @Override
        public void handle(PlayerSettingsPacket packet, Player player) {
        }

        @Override
        public void handle(PlayerBuildPacket packet, Player player) {
            getEntrance().getServer().execute(() -> {
                getEntrance().getStructureBuilder().onContextReceived(player, packet.context());
            });

        }

        @Override
        public void handle(PlayerPermissionCheckPacket packet, Player player) {
            getEntrance().getServer().execute(() -> {
                var isSinglePlayerOwner = getEntrance().getServerManager().getRunningServer().isSinglePlayerOwner(player.getProfile());
                var isOperator = getEntrance().getServerManager().getRunningServer().isOperator(player.getProfile());
                getEntrance().getChannel().sendPacket(new PlayerPermissionCheckPacket(packet.responseId(), packet.playerId(), isSinglePlayerOwner || isOperator), player);
            });

        }

        @Override
        public void handle(PlayerBuildTooltipPacket packet, Player player) {

        }

        @Override
        public void handle(SessionPacket packet, Player player) {
            getEntrance().getServer().execute(() -> {
                getEntrance().getSessionManager().onSession(packet.session(), player);
            });
        }

        @Override
        public void handle(SessionConfigPacket packet, Player player) {
            getEntrance().getServer().execute(() -> {
                getEntrance().getSessionManager().onSessionConfig(packet.sessionConfig(), player);
            });
        }

        @Override
        public void handle(PlayerSnapshotCapturePacket packet, Player player) {

        }

        @Override
        public void handle(PlayerSnapshotSharePacket packet, Player player) {
            getEntrance().getServer().execute(() -> {
                var fromPlayer = getEntrance().getServer().getPlayerList().getPlayer(packet.from());
                var toPlayer = getEntrance().getServer().getPlayerList().getPlayer(packet.to());
                if (fromPlayer == null) {
                    return;
                }
                if (toPlayer == null) {
                    fromPlayer.sendMessage(Schema.getSystemMessage(Text.text("Cannot share this snapshot. Player is offline.")));
                } else {
                    fromPlayer.sendMessage(Schema.getSystemMessage(Text.text("Snapshot shared to player %s.".formatted(toPlayer.getProfile().getName()))));
                    toPlayer.sendMessage(Schema.getSystemMessage(Text.text("Player %s shared you a snapshot. Go to clipboard to import it.".formatted(fromPlayer.getProfile().getName()))));
                    getEntrance().getChannel().sendPacket(packet, toPlayer);
                }
            });


        }
    }

}
