package io.github.drag0n1zed.universal.api.core;

import io.github.drag0n1zed.universal.api.platform.ContentFactory;
import io.github.drag0n1zed.universal.api.platform.PlatformReference;

public enum StatTypes {

    ITEM_USED,
    ITEM_BROKEN,
    ITEM_PICKED_UP,
    ITEM_DROPPED,
    ;

    public Stat<?> get(PlatformReference value) {
        return ContentFactory.getInstance().getStatType(this).get(value);
    }

}
