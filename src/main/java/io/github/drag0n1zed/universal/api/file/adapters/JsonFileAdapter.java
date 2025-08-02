package io.github.drag0n1zed.universal.api.file.adapters;

import java.io.File;
import java.io.IOException;

import com.electronwill.nightconfig.core.Config;

import io.github.drag0n1zed.universal.api.file.FileAdapter;

public final class JsonFileAdapter extends FileAdapter<Config> {

    @Override
    public Config read(File file) throws IOException {
//        return new JsonParser().parse(file, (file1, configFormat) -> false);
        throw new UnsupportedOperationException("JsonParser is not supported");
    }

    @Override
    public void write(File file, Config config) throws IOException {
//        new FancyJsonWriter().write(config, file, WritingMode.REPLACE);
        throw new UnsupportedOperationException("JsonWriter is not supported");
    }

}
