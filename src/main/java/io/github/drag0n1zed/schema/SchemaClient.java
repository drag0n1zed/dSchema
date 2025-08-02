package io.github.drag0n1zed.schema;

import com.google.auto.service.AutoService;

import io.github.drag0n1zed.universal.api.events.impl.ClientEventRegistry;
import io.github.drag0n1zed.universal.api.platform.ClientEntrance;
import io.github.drag0n1zed.universal.api.platform.PlatformLoader;

@AutoService(ClientEntrance.class)
public class SchemaClient implements ClientEntrance {

    private final ClientEventRegistry eventRegistry = PlatformLoader.getSingleton(ClientEventRegistry.class);
    private final SchemaClientNetworkChannel channel = new SchemaClientNetworkChannel(this);
    private final SchemaClientStructureBuilder structureBuilder = new SchemaClientStructureBuilder(this);
    private final SchemaClientManager clientManager = new SchemaClientManager(this);
    private final SchemaClientTagConfigStorage tagConfigStorage = new SchemaClientTagConfigStorage(this);
    private final SchemaClientConfigStorage configStorage = new SchemaClientConfigStorage(this);
    private final SchemaClientSessionManager sessionManager = new SchemaClientSessionManager(this);

    public static SchemaClient getInstance() {
        return (SchemaClient) ClientEntrance.getInstance();
    }

    public ClientEventRegistry getEventRegistry() {
        return eventRegistry;
    }

    public SchemaClientNetworkChannel getChannel() {
        return channel;
    }

    public SchemaClientStructureBuilder getStructureBuilder() {
        return structureBuilder;
    }

    @Override
    public SchemaClientManager getClientManager() {
        return clientManager;
    }

    @Deprecated
    public SchemaClientTagConfigStorage getTagConfigStorage() {
        return tagConfigStorage;
    }

    public SchemaClientConfigStorage getConfigStorage() {
        return configStorage;
    }

    public SchemaClientSessionManager getSessionManager() {
        return sessionManager;
    }

    @Override
    public String getId() {
        return Schema.MOD_ID;
    }

}
