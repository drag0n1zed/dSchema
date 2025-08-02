package io.github.drag0n1zed.schema;

import io.github.drag0n1zed.universal.api.platform.PlatformLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.auto.service.AutoService;

import io.github.drag0n1zed.universal.api.events.impl.EventRegistry;
import io.github.drag0n1zed.universal.api.platform.Entrance;
import io.github.drag0n1zed.universal.api.text.ChatFormatting;
import io.github.drag0n1zed.universal.api.text.Text;

@AutoService(Entrance.class)
public class Schema implements Entrance {

    public static final String MOD_ID = "dschema";
    public static final String DEFAULT_CHANNEL = "default";
    public static final int PROTOCOL_VERSION = 13;
    public static final Logger LOGGER = LoggerFactory.getLogger(Schema.class.getName());

    private final EventRegistry eventRegistry = PlatformLoader.getSingleton(EventRegistry.class);
    private final SchemaNetworkChannel networkChannel = new SchemaNetworkChannel(this);
    private final SchemaStructureBuilder structureBuilder = new SchemaStructureBuilder(this);
    private final SchemaConfigStorage sessionConfigStorage = new SchemaConfigStorage(this);
    private final SchemaSessionManager sessionManager = new SchemaSessionManager(this);
    private final SchemaServerManager serverManager = new SchemaServerManager(this);

    public static Schema getInstance() {
        return (Schema) Entrance.getInstance();
    }

    public static Text getSystemMessage(Text msg) {
        return Text.text("[").append(Text.translate("effortless.symbol")).append("] ").withStyle(ChatFormatting.GRAY).append(msg.withStyle(ChatFormatting.WHITE));
    }

    public static Text getMessage(Text msg) {
        return msg;
    }

    public EventRegistry getEventRegistry() {
        return eventRegistry;
    }

    public SchemaNetworkChannel getChannel() {
        return networkChannel;
    }

    public SchemaStructureBuilder getStructureBuilder() {
        return structureBuilder;
    }

    public SchemaConfigStorage getSessionConfigStorage() {
        return sessionConfigStorage;
    }

    public SchemaSessionManager getSessionManager() {
        return sessionManager;
    }

    public SchemaServerManager getServerManager() {
        return serverManager;
    }

    @Override
    public String getId() {
        return MOD_ID;
    }

}
