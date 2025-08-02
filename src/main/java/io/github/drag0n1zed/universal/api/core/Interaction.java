package io.github.drag0n1zed.universal.api.core;

import io.github.drag0n1zed.universal.api.math.Vector3d;

public interface Interaction {
    Target getTarget();

    Vector3d getPosition();

    default InteractionHand getHand() {
        return InteractionHand.MAIN;
    }

    enum Target {
        MISS,
        BLOCK,
        ENTITY
    }
}
