package io.github.drag0n1zed.dschema.renderer.opertaion.children;

import io.github.drag0n1zed.universal.api.renderer.Renderer;

public interface OperationRenderer {

    void render(Renderer renderer, RenderContext renderContext, float deltaTick);

    interface RenderContext {

        boolean showBlockPreview();

        int maxRenderVolume();

        int maxRenderDistance();

        record Default(boolean showBlockPreview, int maxRenderVolume, int maxRenderDistance) implements RenderContext {
        }


    }
}
