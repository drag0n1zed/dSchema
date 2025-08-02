package io.github.drag0n1zed.schema.building.settings;

import java.util.Collections;
import java.util.List;

import io.github.drag0n1zed.schema.building.pattern.randomize.ItemRandomizer;

public record RandomizerSettings(
        List<ItemRandomizer> randomizers
) {

    public RandomizerSettings() {
        this(Collections.emptyList());
    }

}
