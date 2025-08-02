package io.github.drag0n1zed.schema.building.operation;

import io.github.drag0n1zed.schema.building.pattern.MirrorContext;

public interface Mirrorable<O> extends Trait {

    O mirror(MirrorContext mirrorContext);

}
