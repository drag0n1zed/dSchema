package io.github.drag0n1zed.schema.building.structure;

import io.github.drag0n1zed.schema.building.SingleSelectFeature;

public interface BuildFeature extends SingleSelectFeature {

    @Override
    default String getCategory() {
        return getType().getName();
    }

    BuildFeatures getType();

}
