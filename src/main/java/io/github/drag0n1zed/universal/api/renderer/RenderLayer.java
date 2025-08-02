package io.github.drag0n1zed.universal.api.renderer;

import io.github.drag0n1zed.universal.api.platform.PlatformReference;
import io.github.drag0n1zed.universal.api.renderer.programs.CompositeRenderState;

public interface RenderLayer extends PlatformReference {

    static RenderLayer createComposite(String name, VertexFormat vertexFormat, VertexFormat.Mode vertexMode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, CompositeRenderState state) {
        return RenderStateFactory.getInstance().createCompositeRenderLayer(name, vertexFormat, vertexMode, bufferSize, affectsCrumbling, sortOnUpload, state);
    }

    static RenderLayer createComposite(String name, VertexFormat vertexFormat, VertexFormat.Mode vertexMode, int bufferSize, CompositeRenderState state) {
        return RenderStateFactory.getInstance().createCompositeRenderLayer(name, vertexFormat, vertexMode, bufferSize, false, false, state);
    }
}
