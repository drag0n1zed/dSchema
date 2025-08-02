package io.github.drag0n1zed.universal.api.file.adapters;

import java.io.File;
import java.io.IOException;

import io.github.drag0n1zed.universal.api.file.FileAdapter;
import io.github.drag0n1zed.universal.api.platform.ContentFactory;
import io.github.drag0n1zed.universal.api.tag.Tag;
import io.github.drag0n1zed.universal.api.tag.RecordTag;

public final class TagFileAdapter extends FileAdapter<Tag> {

    @Override
    public Tag read(File file) throws IOException {
        return ContentFactory.getInstance().getInputStreamTagReader().readCompressed(file);
    }

    @Override
    public void write(File file, Tag tag) throws IOException {
        ContentFactory.getInstance().getOutputStreamTagWriter().writeCompressed(file, (RecordTag) tag);
    }

}
