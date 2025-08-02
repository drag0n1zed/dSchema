package io.github.drag0n1zed.universal.api.events.render;

import io.github.drag0n1zed.universal.api.renderer.Renderer;

@FunctionalInterface
public interface RenderWorld {
    void onRenderWorld(Renderer renderer, float deltaTick);
}
