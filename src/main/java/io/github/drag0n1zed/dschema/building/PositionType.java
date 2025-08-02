package io.github.drag0n1zed.dschema.building;

import io.github.drag0n1zed.universal.api.text.Text;

public enum PositionType {
    ABSOLUTE("absolute"),
    RELATIVE("relative");

    private final String name;

    PositionType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Text getDisplayName() {
        return Text.translate("dschema.position.%s".formatted(name));
    }

}
