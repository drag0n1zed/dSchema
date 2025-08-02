package io.github.drag0n1zed.schema;

import io.github.drag0n1zed.universal.api.file.ConfigFileStorage;
import io.github.drag0n1zed.universal.api.file.FileType;
import io.github.drag0n1zed.schema.session.config.SessionConfig;
import io.github.drag0n1zed.schema.session.config.serializer.SessionConfigSerializer;

public final class SchemaConfigStorage extends ConfigFileStorage<SessionConfig> {

    public static final String CONFIG_NAME = "effortless.toml";

    public SchemaConfigStorage(Schema entrance) {
        super(CONFIG_NAME, FileType.TOML, new SessionConfigSerializer());
    }

}
