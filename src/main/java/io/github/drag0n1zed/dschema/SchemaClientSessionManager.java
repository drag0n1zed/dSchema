package io.github.drag0n1zed.dschema;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nullable;

import io.github.drag0n1zed.universal.api.core.Player;
import io.github.drag0n1zed.universal.api.events.lifecycle.ClientTick;
import io.github.drag0n1zed.universal.api.platform.Client;
import io.github.drag0n1zed.universal.api.platform.Platform;
import io.github.drag0n1zed.universal.api.text.ChatFormatting;
import io.github.drag0n1zed.universal.api.text.Text;
import io.github.drag0n1zed.dschema.networking.packets.session.SessionConfigPacket;
import io.github.drag0n1zed.dschema.session.Session;
import io.github.drag0n1zed.dschema.session.SessionManager;
import io.github.drag0n1zed.dschema.session.config.ConstraintConfig;
import io.github.drag0n1zed.dschema.session.config.SessionConfig;

public final class SchemaClientSessionManager implements SessionManager {

    private final SchemaClient entrance;

    private final AtomicReference<Session> serverSession = new AtomicReference<>();
    private final AtomicReference<Session> clientSession = new AtomicReference<>();
    private final AtomicReference<Boolean> isPlayerNotified = new AtomicReference<>(false);

    private final AtomicReference<SessionConfig> serverSessionConfig = new AtomicReference<>();

    public SchemaClientSessionManager(SchemaClient entrance) {
        this.entrance = entrance;

        getEntrance().getEventRegistry().getClientTickEvent().register(this::onClientTick);
    }

    private SchemaClient getEntrance() {
        return entrance;
    }


    @Override
    public void onSession(Session session, Player player) {
        serverSession.set(session);
        isPlayerNotified.set(false);
    }

    @Override
    public void onSessionConfig(SessionConfig sessionConfig, Player player) {
        serverSessionConfig.set(sessionConfig);
        getEntrance().getStructureBuilder().onSessionConfig(sessionConfig);
    }

    public void updateSessionConfig(SessionConfig sessionConfig) {
        getEntrance().getChannel().sendPacket(new SessionConfigPacket(sessionConfig));
    }

    public boolean updateGlobalConfig(ConstraintConfig constraintConfig) {
        var serverSessionConfig = getServerSessionConfig();
        if (serverSessionConfig == null) return false;
        updateSessionConfig(serverSessionConfig.withGlobalConfig(constraintConfig));
        return true;
    }

    public boolean updatePlayerConfig(Map<UUID, ConstraintConfig> playerConfigs) {
        var serverSessionConfig = getServerSessionConfig();
        if (serverSessionConfig == null) return false;
        updateSessionConfig(serverSessionConfig.withPlayerConfig(playerConfigs));
        return true;
    }

    public boolean updatePlayerConfig(UUID id, ConstraintConfig constraintConfig) {
        var serverSessionConfig = getServerSessionConfig();
        if (serverSessionConfig == null) return false;
        updateSessionConfig(serverSessionConfig.withPlayerConfig(id, constraintConfig));
        return true;
    }

    @Override
    public boolean isSessionValid() {
        return getSessionStatus() == SessionStatus.SUCCESS;
    }

    @Override
    public SessionStatus getSessionStatus() {
        if (serverSession.get() == null && clientSession.get() == null) {
            return SessionStatus.MOD_MISSING;
        }
        if (serverSession.get() == null || serverSessionConfig.get() == null) {
            return SessionStatus.SERVER_MOD_MISSING;
        }
        if (clientSession.get() == null) {
            return SessionStatus.CLIENT_MOD_MISSING;
        }
        var serverMod = serverSession.get().mods().stream().filter(mod -> mod.getId().equals(Schema.MOD_ID)).findFirst().orElseThrow();
        var clientMod = clientSession.get().mods().stream().filter(mod -> mod.getId().equals(Schema.MOD_ID)).findFirst().orElseThrow();

        if (!serverMod.getVersionStr().equals(clientMod.getVersionStr())) {
            return SessionStatus.PROTOCOL_NOT_MATCH;
        }
        return SessionStatus.SUCCESS;
    }

    @Override
    public Session getLastSession() {
        var platform = Platform.getInstance();
        var protocolVersion = Schema.PROTOCOL_VERSION;
        return new Session(
                platform.getLoaderType(),
                platform.getLoaderVersion(),
                platform.getGameVersion(),
                platform.getRunningMods(),
                protocolVersion

        );
    }

    @Override
    public SessionConfig getLastSessionConfig() {
        return null;
    }

    public Session getServerSession() {
        return serverSession.get();
    }

    @Nullable
    public SessionConfig getServerSessionConfig() {
        return serverSessionConfig.get();
    }

    public SessionConfig getServerSessionConfigOrEmpty() {
        return serverSessionConfig.get() != null ? serverSessionConfig.get() : SessionConfig.EMPTY;
    }

    public void notifyPlayer() {
        var id = Text.text("[").append(Text.translate("dschema.name")).append(Text.text("] ")).withStyle(ChatFormatting.GRAY);
        var message = switch (getSessionStatus()) {
            case MOD_MISSING -> Text.translate("dschema.session_status.message.mod_missing");
            case SERVER_MOD_MISSING -> Text.translate("dschema.session_status.message.server_mod_missing");
            case CLIENT_MOD_MISSING -> Text.translate("dschema.session_status.message.client_mod_missing");
            case PROTOCOL_NOT_MATCH -> Text.translate("dschema.session_status.message.protocol_not_match", serverSession.get().protocolVersion(), clientSession.get().protocolVersion());
            case SUCCESS -> Text.translate("dschema.session_status.message.success", serverSession.get().loaderType().name());
        };
        if (getSessionStatus() != SessionStatus.SUCCESS) {
            getEntrance().getClientManager().getRunningClient().getPlayer().sendMessage(id.append(message));
        }
    }


    public void onClientTick(Client client, ClientTick.Phase phase) {
        if (phase == ClientTick.Phase.END) {
            return;
        }

        if (getEntrance().getClient() == null || getEntrance().getClient().getPlayer() == null) {
            serverSession.set(null);
            serverSessionConfig.set(null);
            isPlayerNotified.set(false);
            return;
        }

        var player = getEntrance().getClient().getPlayer();

        if (clientSession.get() == null) {
            clientSession.set(getLastSession());
        }

        if (getEntrance().getStructureBuilder().getContext(player).isDisabled()) {
            return;
        }

        if (!isPlayerNotified.compareAndSet(false, true)) {
            return;
        }

        notifyPlayer();

    }

}
