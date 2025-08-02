package io.github.drag0n1zed.universal.api.core;

import io.github.drag0n1zed.universal.api.platform.ContentFactory;
import io.github.drag0n1zed.universal.api.platform.PlatformReference;

public interface ResourceLocation extends PlatformReference {

    static ResourceLocation decompose(String value, String separator) {
        try {
            return of(value.split(separator)[0], value.split(separator)[1]);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException(e + "for value: " + value);
        }
    }

    static ResourceLocation decompose(String value) {
        return decompose(value, ":");
    }

    static ResourceLocation vanilla(String path) {
        return of("minecraft", path);
    }

    static ResourceLocation of(String namespace, String path) {
        return ContentFactory.getInstance().newResourceLocation(namespace, path);
    }

    String getNamespace();

    String getPath();

    default String getString() {
        return getNamespace() + ":" + getPath();
    }

}
