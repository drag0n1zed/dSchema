package io.github.drag0n1zed.universal.api.core;

import io.github.drag0n1zed.universal.api.platform.PlatformReference;

public interface DimensionType extends PlatformReference {

    boolean hasSkyLight();

    boolean hasCeiling();

    double coordinateScale();

    int minY();

    int height();

    int logicalHeight();


}
