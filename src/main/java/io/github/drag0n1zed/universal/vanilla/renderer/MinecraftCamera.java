package io.github.drag0n1zed.universal.vanilla.renderer;

import io.github.drag0n1zed.universal.api.math.Quaternionf;
import io.github.drag0n1zed.universal.api.math.Vector3d;
import io.github.drag0n1zed.universal.api.renderer.Camera;
import io.github.drag0n1zed.universal.vanilla.core.MinecraftConvertor;

public record MinecraftCamera(
        net.minecraft.client.Camera refs
) implements Camera {

    @Override
    public Vector3d position() {
        return MinecraftConvertor.fromPlatformVector3d(refs.getPosition());
    }

    @Override
    public Quaternionf rotation() {
        return MinecraftConvertor.fromPlatformQuaternion(refs.rotation());
    }

    @Override
    public float eyeHeight() {
        return refs.getEntity().getEyeHeight();
    }

}
