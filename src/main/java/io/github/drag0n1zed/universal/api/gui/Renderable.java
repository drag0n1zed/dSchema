package io.github.drag0n1zed.universal.api.gui;

import io.github.drag0n1zed.universal.api.renderer.Renderer;

public interface Renderable {

    void render(Renderer renderer, int mouseX, int mouseY, float deltaTick);

    void renderOverlay(Renderer renderer, int mouseX, int mouseY, float deltaTick);

}
