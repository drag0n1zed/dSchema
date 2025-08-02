package io.github.drag0n1zed.dschema.building.replace;

import io.github.drag0n1zed.dschema.building.SingleSelectFeature;

public enum ReplaceMode implements SingleSelectFeature {
    NORMAL("replace_mode_normal"),
    QUICK("replace_mode_quick");

    private final String name;

    ReplaceMode(String name) {
        this.name = name;
    }

    public ReplaceMode next() {
        return switch (this) {
            case NORMAL -> QUICK;
            case QUICK -> NORMAL;
        };
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getCategory() {
        return "replace_mode";
    }

}
