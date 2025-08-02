package io.github.drag0n1zed.dschema.building.operation.empty;

import java.util.List;

import io.github.drag0n1zed.universal.api.core.ItemStack;
import io.github.drag0n1zed.dschema.building.operation.ItemSummary;
import io.github.drag0n1zed.dschema.building.operation.Operation;
import io.github.drag0n1zed.dschema.building.operation.OperationResult;

public class EmptyOperationResult extends OperationResult {

    private final EmptyOperation operation;

    public EmptyOperationResult(EmptyOperation operation) {

        this.operation = operation;
    }

    @Override
    public Operation getOperation() {
        return operation;
    }

    @Override
    public EmptyOperation getReverseOperation() {
        return operation;
    }

    @Override
    public int getAffectedBlockCount() {
        return 0;
    }

    @Override
    public List<ItemStack> getItemSummary(ItemSummary itemSummary) {
        return List.of();
    }

}
