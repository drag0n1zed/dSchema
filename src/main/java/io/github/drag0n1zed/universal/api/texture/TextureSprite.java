package io.github.drag0n1zed.universal.api.texture;

import io.github.drag0n1zed.universal.api.core.ResourceLocation;

public interface TextureSprite {

    ResourceLocation name();

    ResourceLocation texture();

    int width();

    int height();

    int x();

    int y();

    float u0();

    float u1();

    float v0();

    float v1();

    default float u(float u) {
        return u0() + (u1() - u0()) * u;
    }

    default float v(float v) {
        return v0() + (v1() - v0()) * v;
    }

    default float uOffset(float offset) {
        return (offset - u0()) / (u1() - u0());
    }

    default float vOffset(float offset) {
        return (offset - v0()) / (v1() - v0());
    }

    SpriteScaling scaling();

}
