package io.github.drag0n1zed.dschema.building.replace;

import io.github.drag0n1zed.dschema.building.SingleSelectFeature;

public enum ReplaceStrategy implements SingleSelectFeature {
    DISABLED("replace_disabled"),
    BLOCKS_AND_AIR("replace_blocks_and_air"),
    BLOCKS_ONLY("replace_blocks_only"),
    OFFHAND_ONLY("replace_offhand_only");
//    CUSTOM_LIST_ONLY("replace_custom");

    private final String name;

    ReplaceStrategy(String name) {
        this.name = name;
    }

    public ReplaceStrategy next() {
        return switch (this) {
            case DISABLED -> BLOCKS_AND_AIR;
            case BLOCKS_AND_AIR -> BLOCKS_ONLY;
            case BLOCKS_ONLY -> OFFHAND_ONLY;
            case OFFHAND_ONLY -> DISABLED;
        };
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getCategory() {
        return "replace";
    }

}
