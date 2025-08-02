package io.github.drag0n1zed.universal.api.file;

import io.github.drag0n1zed.universal.api.file.adapters.JsonFileAdapter;
import io.github.drag0n1zed.universal.api.file.adapters.TagFileAdapter;
import io.github.drag0n1zed.universal.api.file.adapters.TomlFileAdapter;

public enum FileType {
    JSON("json", new JsonFileAdapter()),
    TOML("toml", new TomlFileAdapter()),
    NBT("dat", new TagFileAdapter());

    private final String extension;
    private final FileAdapter adapter;

    <T> FileType(String extension, FileAdapter<T> adapter) {
        this.extension = extension;
        this.adapter = adapter;
    }

    public String getExtension() {
        return extension;
    }

    public <T> FileAdapter<T> getAdapter() {
        return adapter;
    }

}
