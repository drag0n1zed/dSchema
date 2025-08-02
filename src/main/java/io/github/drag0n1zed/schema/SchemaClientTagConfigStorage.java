package io.github.drag0n1zed.schema;

import io.github.drag0n1zed.universal.api.file.FileType;
import io.github.drag0n1zed.universal.api.file.TagElementFileStorage;
import io.github.drag0n1zed.schema.building.config.ClientConfig;
import io.github.drag0n1zed.schema.building.config.tag.ClientConfigTagSerializer;

public final class SchemaClientTagConfigStorage extends TagElementFileStorage<ClientConfig> {

    public SchemaClientTagConfigStorage(SchemaClient entrance) {
        super("effortless-client.dat", FileType.NBT, new ClientConfigTagSerializer());
    }

    @Override
    public ClientConfig getDefault() {
        return ClientConfig.DEFAULT;
    }

}
