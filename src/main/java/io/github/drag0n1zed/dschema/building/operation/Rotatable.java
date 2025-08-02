package io.github.drag0n1zed.dschema.building.operation;

import io.github.drag0n1zed.dschema.building.pattern.RotateContext;

public interface Rotatable<O> extends Trait {

    O rotate(RotateContext rotateContext);

}
