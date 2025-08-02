package io.github.drag0n1zed.schema.building.operation;

import io.github.drag0n1zed.schema.building.Context;

public interface Operation extends Mirrorable<Operation>, Movable<Operation>, Rotatable<Operation>, Refactorable<Operation> {

    Context getContext();

    OperationResult commit();

}
