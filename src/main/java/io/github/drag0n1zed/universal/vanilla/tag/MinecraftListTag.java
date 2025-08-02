package io.github.drag0n1zed.universal.vanilla.tag;

import java.util.stream.Stream;

import io.github.drag0n1zed.universal.api.tag.ListTag;
import io.github.drag0n1zed.universal.api.tag.Tag;

public record MinecraftListTag(net.minecraft.nbt.ListTag refs) implements ListTag {

    @Override
    public byte getId() {
        return new MinecraftTag(refs).getId();
    }

    @Override
    public String getAsString() {
        return new MinecraftTag(refs).getAsString();
    }

    @Override
    public boolean addTag(int index, Tag tag) {
        return refs.addTag(index, tag.reference());
    }

    @Override
    public boolean setTag(int index, Tag tag) {
        return refs.setTag(index, tag.reference());
    }

    @Override
    public Tag getTag(int index) {
        return MinecraftTag.ofNullable(refs.get(index));
    }

    @Override
    public int size() {
        return refs.size();
    }

    @Override
    public Stream<Tag> stream() {
        return refs.stream().map(MinecraftTag::ofNullable);
    }
}
