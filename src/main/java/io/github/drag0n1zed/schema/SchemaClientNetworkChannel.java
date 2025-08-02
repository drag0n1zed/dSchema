package io.github.drag0n1zed.schema;

import io.github.drag0n1zed.universal.api.core.Player;
import io.github.drag0n1zed.universal.api.networking.NetworkChannel;
import io.github.drag0n1zed.universal.api.networking.Packet;
import io.github.drag0n1zed.universal.api.networking.Side;
import io.github.drag0n1zed.schema.building.config.ClientConfig;
import io.github.drag0n1zed.schema.networking.packets.AllPacketListener;
import io.github.drag0n1zed.schema.networking.packets.player.PlayerBuildPacket;
import io.github.drag0n1zed.schema.networking.packets.player.PlayerBuildTooltipPacket;
import io.github.drag0n1zed.schema.networking.packets.player.PlayerCommandPacket;
import io.github.drag0n1zed.schema.networking.packets.player.PlayerPermissionCheckPacket;
import io.github.drag0n1zed.schema.networking.packets.player.PlayerSettingsPacket;
import io.github.drag0n1zed.schema.networking.packets.player.PlayerSnapshotCapturePacket;
import io.github.drag0n1zed.schema.networking.packets.player.PlayerSnapshotSharePacket;
import io.github.drag0n1zed.schema.networking.packets.session.SessionConfigPacket;
import io.github.drag0n1zed.schema.networking.packets.session.SessionPacket;

public final class SchemaClientNetworkChannel extends NetworkChannel<AllPacketListener> {

    private static final int COMPATIBILITY_VERSION = Schema.PROTOCOL_VERSION;
    private final SchemaClient entrance;
    private final AllPacketListener listener;

    public SchemaClientNetworkChannel(SchemaClient entrance) {
        this(entrance, Schema.DEFAULT_CHANNEL);
    }

    public SchemaClientNetworkChannel(SchemaClient entrance, String name) {
        super(entrance, name, Side.CLIENT);
        this.entrance = entrance;
        this.listener = new ClientPacketListener();

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

    private SchemaClient getEntrance() {
        return entrance;
    }

    @Override
    public void receivePacket(Packet packet, Player player) {
        packet.handle(listener, player);
    }

    @Override
    public int getCompatibilityVersion() {
        return COMPATIBILITY_VERSION;
    }

    private class ClientPacketListener implements AllPacketListener {

        private boolean isValidReceiver() {
            if (getEntrance().getClient() == null) {
                return false;
            }
            if (getEntrance().getClient().getWorld() == null) {
                return false;
            }
            return true;
        }

        @Override
        public void handle(PlayerCommandPacket packet, Player player) {
            switch (packet.action()) {
                case REDO, UNDO -> {
                    // noop
                }
            }
        }

        @Override
        public void handle(PlayerSettingsPacket packet, Player player) {
        }

        @Override
        public void handle(PlayerBuildPacket packet, Player player) {
            if (!isValidReceiver()) {
                return;
            }
            var owner = getEntrance().getClient().getWorld().getPlayer(packet.playerId());
            if (owner == null) {
                return;
            }
            getEntrance().getClient().execute(() -> getEntrance().getStructureBuilder().onContextReceived(owner, packet.context()));
        }

        @Override
        public void handle(PlayerPermissionCheckPacket packet, Player player) {
        }

        @Override
        public void handle(PlayerBuildTooltipPacket packet, Player player) {
            if (!isValidReceiver()) {
                return;
            }
            getEntrance().getClient().execute(() -> getEntrance().getStructureBuilder().onTooltipReceived(player, packet.operationTooltip()));

        }

        @Override
        public void handle(SessionPacket packet, Player player) {
            getEntrance().getClient().execute(() -> getEntrance().getSessionManager().onSession(packet.session(), player));
        }

        @Override
        public void handle(SessionConfigPacket packet, Player player) {
            getEntrance().getClient().execute(() -> getEntrance().getSessionManager().onSessionConfig(packet.sessionConfig(), player));

        }

        @Override
        public void handle(PlayerSnapshotCapturePacket packet, Player player) {
            getEntrance().getConfigStorage().update(config -> new ClientConfig(config.renderConfig(), config.patternConfig(), config.clipboardConfig().appendHistory(packet.snapshot())));
            getEntrance().getClient().execute(() -> getEntrance().getStructureBuilder().onSnapshotCaptured(player, packet.snapshot()));
        }

        @Override
        public void handle(PlayerSnapshotSharePacket packet, Player player) {
            getEntrance().getClient().execute(() -> {


            });
        }
    }

}
