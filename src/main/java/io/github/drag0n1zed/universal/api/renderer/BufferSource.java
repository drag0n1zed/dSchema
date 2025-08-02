package io.github.drag0n1zed.universal.api.renderer;

import io.github.drag0n1zed.universal.api.platform.PlatformReference;

public interface BufferSource extends PlatformReference {

    VertexBuffer getBuffer(RenderLayer renderLayer);

    void end();

}
