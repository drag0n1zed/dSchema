package io.github.drag0n1zed.dschema.building.operation;

import io.github.drag0n1zed.dschema.building.Context;

public interface Operation extends Mirrorable<Operation>, Movable<Operation>, Rotatable<Operation>, Refactorable<Operation> {

    Context getContext();

    OperationResult commit();

}
