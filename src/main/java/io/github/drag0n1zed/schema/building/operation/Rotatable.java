package io.github.drag0n1zed.schema.building.operation;

import io.github.drag0n1zed.schema.building.pattern.RotateContext;

public interface Rotatable<O> extends Trait {

    O rotate(RotateContext rotateContext);

}
