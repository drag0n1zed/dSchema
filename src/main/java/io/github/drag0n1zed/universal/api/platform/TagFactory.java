package io.github.drag0n1zed.universal.api.platform;

import io.github.drag0n1zed.universal.api.tag.ListTag;
import io.github.drag0n1zed.universal.api.tag.StringTag;
import io.github.drag0n1zed.universal.api.tag.NumericTag;
import io.github.drag0n1zed.universal.api.tag.RecordTag;

public interface TagFactory {

    static TagFactory getInstance() {
        return PlatformLoader.getSingleton();
    }

    RecordTag newRecord();

    ListTag newList();

    StringTag newLiteral(String value);

    NumericTag newPrimitive(boolean value);

    NumericTag newPrimitive(byte value);

    NumericTag newPrimitive(short value);

    NumericTag newPrimitive(int value);

    NumericTag newPrimitive(long value);

    NumericTag newPrimitive(float value);

    NumericTag newPrimitive(double value);


}
