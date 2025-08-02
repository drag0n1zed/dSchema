package io.github.drag0n1zed.dschema.building.pattern;

import io.github.drag0n1zed.dschema.building.Option;

public enum Patterns implements Option {
    DISABLED("pattern_disabled"),
    ENABLED("pattern_enabled")
    ;

    private final String name;

    Patterns(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getCategory() {
        return "pattern";
    }

}
