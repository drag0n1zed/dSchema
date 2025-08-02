package io.github.drag0n1zed.schema;

import io.github.drag0n1zed.universal.api.file.ConfigFileStorage;
import io.github.drag0n1zed.universal.api.file.FileType;
import io.github.drag0n1zed.schema.building.config.ClientConfig;
import io.github.drag0n1zed.schema.building.config.universal.ClientConfigConfigSerializer;
import io.github.drag0n1zed.schema.building.structure.BuildMode;
import io.github.drag0n1zed.schema.building.structure.builder.Structure;

public final class SchemaClientConfigStorage extends ConfigFileStorage<ClientConfig> {

    public static final String CONFIG_NAME = "effortless-client.toml";

    public SchemaClientConfigStorage(SchemaClient entrance) {
        super(CONFIG_NAME, FileType.TOML, new ClientConfigConfigSerializer());
    }

    public void setStructure(Structure structure) {
        set(get().withStructure(structure));
    }

    public Structure getStructure(BuildMode buildMode) {
        return get().getStructure(buildMode);
    }

}
