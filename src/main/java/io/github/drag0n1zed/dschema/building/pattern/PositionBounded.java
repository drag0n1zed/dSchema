package io.github.drag0n1zed.dschema.building.pattern;

import io.github.drag0n1zed.universal.api.math.Range1d;
import io.github.drag0n1zed.universal.api.math.Vector3d;

public interface PositionBounded {

    Range1d POSITION_RANGE = new Range1d(-30000000, 30000000);

    boolean isInBounds(Vector3d position);

}
