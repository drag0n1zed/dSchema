package io.github.drag0n1zed.universal.api.core;

import io.github.drag0n1zed.universal.api.platform.PlatformReference;

public interface StatType<T extends PlatformReference> extends PlatformReference {

    Stat<T> get(T value);

}
