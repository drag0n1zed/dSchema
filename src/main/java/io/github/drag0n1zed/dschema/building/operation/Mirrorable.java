package io.github.drag0n1zed.dschema.building.operation;

import io.github.drag0n1zed.dschema.building.pattern.MirrorContext;

public interface Mirrorable<O> extends Trait {

    O mirror(MirrorContext mirrorContext);

}
