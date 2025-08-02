package io.github.drag0n1zed.universal.api.core;

import io.github.drag0n1zed.universal.api.text.Text;

public enum Position {

    DISABLED("disabled"),
    LEFT("left"),
    RIGHT("right");

    private final String name;

    Position(String name) {
        this.name = name;
    }

    public Text getDisplayName() {
        return Text.translate("dschema.position.%s".formatted(name));
    }

    public AxisDirection getAxis() {
        return switch (this) {
            case LEFT -> AxisDirection.NEGATIVE;
            case RIGHT -> AxisDirection.POSITIVE;
            default -> null;
        };
    }
}
