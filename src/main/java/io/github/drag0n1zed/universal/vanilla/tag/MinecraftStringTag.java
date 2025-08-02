package io.github.drag0n1zed.universal.vanilla.tag;

import io.github.drag0n1zed.universal.api.tag.StringTag;

public record MinecraftStringTag(net.minecraft.nbt.StringTag refs) implements StringTag {

    @Override
    public byte getId() {
        return new MinecraftTag(refs).getId();
    }

    @Override
    public String getAsString() {
        return new MinecraftTag(refs).getAsString();
    }
}
