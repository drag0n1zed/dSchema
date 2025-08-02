package io.github.drag0n1zed.schema.building.history;

import io.github.drag0n1zed.schema.building.Option;

public enum UndoRedo implements Option {
    UNDO("undo"),
    REDO("redo"),
    ;

    private final String name;

    UndoRedo(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getCategory() {
        return "history";
    }

}
