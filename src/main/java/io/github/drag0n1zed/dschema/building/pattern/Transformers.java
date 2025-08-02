package io.github.drag0n1zed.dschema.building.pattern;

import io.github.drag0n1zed.universal.api.text.Text;

public enum Transformers {
    ARRAY("array"),
    MIRROR("mirror"),
    RADIAL("radial"),
    RANDOMIZER("randomizer");

    private final String name;

    Transformers(String name) {
        this.name = name;
    }

    public Text getDisplayName() {
        return Text.translate("dschema.transformer.%s".formatted(name));
    }

    public Text getTitleText() {
        return Text.translate("dschema.transformer.%s.title".formatted(name));
    }

}
