package io.github.drag0n1zed.dschema.building;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import io.github.drag0n1zed.dschema.building.structure.BuildFeature;
import io.github.drag0n1zed.dschema.building.structure.BuildFeatures;
import io.github.drag0n1zed.dschema.building.structure.BuildMode;

record FeatureBuildMode(
        BuildMode buildMode,
        Set<BuildFeature> features
) {

    public FeatureBuildMode {

    }

    public static FeatureBuildMode createDefault(BuildMode buildMode) {
        return new FeatureBuildMode(buildMode, Arrays.stream(buildMode.getSupportedFeatures()).map(BuildFeatures::getDefaultFeature).collect(Collectors.toSet()));
    }


    public FeatureBuildMode withBuildFeature(BuildFeature feature) {
        return this;
    }


}
