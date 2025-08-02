package io.github.drag0n1zed.universal.api.tag;

import java.util.Locale;

import io.github.drag0n1zed.universal.api.core.ResourceLocation;
import io.github.drag0n1zed.universal.api.platform.TagFactory;

public interface StringTag extends Tag {

    static StringTag of(String value) {
        return TagFactory.getInstance().newLiteral(value);
    }


    static  <T extends Enum<T>> StringTag of(Enum<T> value) {
        return of(ResourceLocation.of("effortless", value.name().toLowerCase(Locale.ROOT)).toString());
    }

    default String getString() {
        return getAsString();
    }


    default <T extends Enum<T>> T getAsEnum(Class<T> clazz) {
        try {
            var id = ResourceLocation.decompose(getString());
            return Enum.valueOf(clazz, id.getPath().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }


}
