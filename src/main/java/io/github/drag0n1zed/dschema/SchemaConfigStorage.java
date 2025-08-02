package io.github.drag0n1zed.dschema;

import io.github.drag0n1zed.universal.api.file.ConfigFileStorage;
import io.github.drag0n1zed.universal.api.file.FileType;
import io.github.drag0n1zed.dschema.session.config.SessionConfig;
import io.github.drag0n1zed.dschema.session.config.serializer.SessionConfigSerializer;

public final class SchemaConfigStorage extends ConfigFileStorage<SessionConfig> {

    public static final String CONFIG_NAME = "dschema.toml";

    public SchemaConfigStorage(Schema entrance) {
        super(CONFIG_NAME, FileType.TOML, new SessionConfigSerializer());
    }

}
