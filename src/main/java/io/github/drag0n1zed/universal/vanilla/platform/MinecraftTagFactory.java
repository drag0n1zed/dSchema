package io.github.drag0n1zed.universal.vanilla.platform;

import com.google.auto.service.AutoService;

import io.github.drag0n1zed.universal.api.platform.TagFactory;
import io.github.drag0n1zed.universal.api.tag.ListTag;
import io.github.drag0n1zed.universal.api.tag.StringTag;
import io.github.drag0n1zed.universal.api.tag.NumericTag;
import io.github.drag0n1zed.universal.api.tag.RecordTag;
import io.github.drag0n1zed.universal.vanilla.tag.MinecraftListTag;
import io.github.drag0n1zed.universal.vanilla.tag.MinecraftStringTag;
import io.github.drag0n1zed.universal.vanilla.tag.MinecraftNumericTag;
import io.github.drag0n1zed.universal.vanilla.tag.MinecraftRecordTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;

@AutoService(TagFactory.class)
public final class MinecraftTagFactory implements TagFactory {

    @Override
    public RecordTag newRecord() {
        return new MinecraftRecordTag(new CompoundTag());
    }

    @Override
    public ListTag newList() {
        return new MinecraftListTag(new net.minecraft.nbt.ListTag());
    }

    @Override
    public StringTag newLiteral(String value) {
        return new MinecraftStringTag(net.minecraft.nbt.StringTag.valueOf(value));
    }

    @Override
    public NumericTag newPrimitive(boolean value) {
        return new MinecraftNumericTag(ByteTag.valueOf(value));
    }

    @Override
    public NumericTag newPrimitive(byte value) {
        return new MinecraftNumericTag(ByteTag.valueOf(value));
    }

    @Override
    public NumericTag newPrimitive(short value) {
        return new MinecraftNumericTag(ShortTag.valueOf(value));
    }

    @Override
    public NumericTag newPrimitive(int value) {
        return new MinecraftNumericTag(IntTag.valueOf(value));
    }

    @Override
    public NumericTag newPrimitive(long value) {
        return new MinecraftNumericTag(LongTag.valueOf(value));
    }

    @Override
    public NumericTag newPrimitive(float value) {
        return new MinecraftNumericTag(FloatTag.valueOf(value));
    }

    @Override
    public NumericTag newPrimitive(double value) {
        return new MinecraftNumericTag(DoubleTag.valueOf(value));
    }
}
