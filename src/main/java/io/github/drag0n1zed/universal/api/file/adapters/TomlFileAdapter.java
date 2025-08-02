package io.github.drag0n1zed.universal.api.file.adapters;

import java.io.File;
import java.io.IOException;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.toml.TomlParser;
import com.electronwill.nightconfig.toml.TomlWriter;

import io.github.drag0n1zed.universal.api.file.FileAdapter;

public final class TomlFileAdapter extends FileAdapter<Config> {

    @Override
    public Config read(File file) throws IOException {
        return new TomlParser().parse(file, (file1, configFormat) -> false);
    }

    @Override
    public void write(File file, Config config) throws IOException {
        new TomlWriter().write(config, file, WritingMode.REPLACE);
    }

}
