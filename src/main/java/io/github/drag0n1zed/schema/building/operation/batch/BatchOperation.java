package io.github.drag0n1zed.schema.building.operation.batch;

import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import io.github.drag0n1zed.schema.building.Context;
import io.github.drag0n1zed.schema.building.operation.Operation;

public abstract class BatchOperation implements Operation {

    protected final Context context;

    protected BatchOperation(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    @Override
    public abstract BatchOperationResult commit();

    public abstract BatchOperation map(UnaryOperator<Operation> operator);

    public abstract BatchOperation mapEach(UnaryOperator<Operation> operator);

    public abstract BatchOperation flatten();

    public abstract BatchOperation filter(Predicate<Operation> predicate);

    public abstract Stream<? extends Operation> operations();

}
