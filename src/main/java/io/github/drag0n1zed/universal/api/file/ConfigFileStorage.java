package io.github.drag0n1zed.universal.api.file;

import java.io.File;
import java.io.IOException;

import com.electronwill.nightconfig.core.Config;

import io.github.drag0n1zed.universal.api.config.ConfigSerializer;

public abstract class ConfigFileStorage<T> extends FileStorage<T> {

    static {
        Config.setInsertionOrderPreserved(true);
    }

    private final ConfigSerializer<T> serializer;

    protected ConfigFileStorage(String fileName, FileType fileType, ConfigSerializer<T> serializer) {
        super(fileName, fileType);
        this.serializer = serializer;
    }

    @Override
    protected T read(File config) throws IOException {
        return getSerializer().deserialize((Config) getFileType().getAdapter().read(config));
    }

    @Override
    protected void write(File file, T t) throws IOException {
        getFileType().getAdapter().write(file, getSerializer().serialize(t));
    }

    @Override
    public T getDefault() {
        return getSerializer().getDefault();
    }

    public ConfigSerializer<T> getSerializer() {
        return serializer;
    }
}
