package io.github.drag0n1zed.dschema.building.structure;

import io.github.drag0n1zed.dschema.building.SingleSelectFeature;

public interface BuildFeature extends SingleSelectFeature {

    @Override
    default String getCategory() {
        return getType().getName();
    }

    BuildFeatures getType();

}
