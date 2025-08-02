package io.github.drag0n1zed.dschema.building.config.universal;

import io.github.drag0n1zed.dschema.building.clipboard.Snapshot;
import io.github.drag0n1zed.universal.api.config.ConfigSerializer;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigSpec;

public class SnapshotConfigSerializer implements ConfigSerializer<Snapshot> {

    public static final SnapshotConfigSerializer INSTANCE = new SnapshotConfigSerializer();

    private SnapshotConfigSerializer() {
    }


    @Override
    public ConfigSpec getSpec(Config config) {
        return new ConfigSpec();
    }

    @Override
    public Snapshot getDefault() {
        return Snapshot.EMPTY;
    }

    @Override
    public Snapshot deserialize(Config config) {
        return Snapshot.EMPTY;
    }

    @Override
    public Config serialize(Snapshot snapshot) {
        return Config.inMemory();
    }
}
