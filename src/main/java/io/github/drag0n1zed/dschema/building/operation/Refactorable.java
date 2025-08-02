package io.github.drag0n1zed.dschema.building.operation;

import io.github.drag0n1zed.dschema.building.pattern.RefactorContext;

public interface Refactorable<O> extends Trait {

    O refactor(RefactorContext source);

}
