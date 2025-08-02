package io.github.drag0n1zed.schema.building.clipboard;

import io.github.drag0n1zed.schema.building.Option;

public enum Clipboards implements Option {
    DISABLED("clipboard_disabled"),
    ENABLED("clipboard_enabled")
    ;

    private final String name;

    Clipboards(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getCategory() {
        return "clipboard";
    }

}
