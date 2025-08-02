package io.github.drag0n1zed.universal.api.renderer;

import io.github.drag0n1zed.universal.api.math.Quaternionf;
import io.github.drag0n1zed.universal.api.math.Vector3d;

public interface Camera {

    Vector3d position();

    Quaternionf rotation();

    float eyeHeight();

}
