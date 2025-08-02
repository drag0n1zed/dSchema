package io.github.drag0n1zed.dschema.building.structure;

public enum CircleStart implements BuildFeature {
    CORNER("circle_start_corner"),
    CENTER("circle_start_center"),
    ;

    private final String name;

    CircleStart(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public BuildFeatures getType() {
        return BuildFeatures.CIRCLE_START;
    }
}
