package io.github.drag0n1zed.schema.building.operation;

import io.github.drag0n1zed.schema.building.pattern.RefactorContext;

public interface Refactorable<O> extends Trait {

    O refactor(RefactorContext source);

}
