package io.github.drag0n1zed.schema.building.settings;

import io.github.drag0n1zed.schema.building.Option;

public enum Misc implements Option {
    SETTINGS("settings"),
    PATTERN("pattern"),
    GO_BACK("go_back"),
    ;

    private final String name;

    Misc(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getCategory() {
        return "misc";
    }

}
