package io.github.drag0n1zed.universal.api.core.fluid;

import io.github.drag0n1zed.universal.api.platform.ContentFactory;

public enum Fluids {
    EMPTY,
    FLOWING_WATER,
    WATER,
    FLOWING_LAVA,
    LAVA,
    ;

    public Fluid fluid() {
        return ContentFactory.getInstance().getFluid(this);
    }

}
