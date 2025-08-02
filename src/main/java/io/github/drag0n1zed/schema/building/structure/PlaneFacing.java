package io.github.drag0n1zed.schema.building.structure;

import java.util.Set;

import io.github.drag0n1zed.universal.api.core.Axis;

public enum PlaneFacing implements BuildFeature {
    BOTH("face_both"),
    VERTICAL("face_vertical"),
    HORIZONTAL("face_horizontal");

    private final String name;

    PlaneFacing(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public BuildFeatures getType() {
        return BuildFeatures.PLANE_FACING;
    }

    public Set<Axis> getAxes() {
        return switch (this) {
            case BOTH -> Set.of(Axis.X, Axis.Y, Axis.Z);
            case VERTICAL -> Set.of(Axis.X, Axis.Z);
            case HORIZONTAL -> Set.of(Axis.Y);
        };
    }
}
