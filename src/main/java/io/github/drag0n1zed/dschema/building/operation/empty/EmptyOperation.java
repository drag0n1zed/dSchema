package io.github.drag0n1zed.dschema.building.operation.empty;

import io.github.drag0n1zed.dschema.building.Context;
import io.github.drag0n1zed.dschema.building.operation.Operation;
import io.github.drag0n1zed.dschema.building.operation.OperationResult;
import io.github.drag0n1zed.dschema.building.pattern.MirrorContext;
import io.github.drag0n1zed.dschema.building.pattern.MoveContext;
import io.github.drag0n1zed.dschema.building.pattern.RefactorContext;
import io.github.drag0n1zed.dschema.building.pattern.RotateContext;

public final class EmptyOperation implements Operation {

    private final Context context;

    public EmptyOperation(Context context) {
        this.context = context;
    }

    @Override
    public Operation mirror(MirrorContext mirrorContext) {
        return this;
    }

    @Override
    public Operation move(MoveContext moveContext) {
        return this;
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public OperationResult commit() {
        return new EmptyOperationResult(this);
    }

    @Override
    public Operation refactor(RefactorContext source) {
        return this;
    }

    @Override
    public Operation rotate(RotateContext rotateContext) {
        return this;
    }
}
