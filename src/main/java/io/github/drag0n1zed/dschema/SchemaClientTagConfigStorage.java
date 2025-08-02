package io.github.drag0n1zed.dschema;

import io.github.drag0n1zed.universal.api.file.FileType;
import io.github.drag0n1zed.universal.api.file.TagElementFileStorage;
import io.github.drag0n1zed.dschema.building.config.ClientConfig;
import io.github.drag0n1zed.dschema.building.config.tag.ClientConfigTagSerializer;

public final class SchemaClientTagConfigStorage extends TagElementFileStorage<ClientConfig> {

    public SchemaClientTagConfigStorage(SchemaClient entrance) {
        super("dschema-client.dat", FileType.NBT, new ClientConfigTagSerializer());
    }

    @Override
    public ClientConfig getDefault() {
        return ClientConfig.DEFAULT;
    }

}
