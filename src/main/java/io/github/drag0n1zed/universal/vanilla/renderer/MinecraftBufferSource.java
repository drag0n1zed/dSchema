package io.github.drag0n1zed.universal.vanilla.renderer;

import io.github.drag0n1zed.universal.api.renderer.BufferSource;
import io.github.drag0n1zed.universal.api.renderer.RenderLayer;
import io.github.drag0n1zed.universal.api.renderer.VertexBuffer;
import net.minecraft.client.renderer.MultiBufferSource;

public record MinecraftBufferSource(
        MultiBufferSource.BufferSource refs
) implements BufferSource {

    @Override
    public VertexBuffer getBuffer(RenderLayer renderLayer) {
        return new MinecraftVertexBuffer(refs.getBuffer(renderLayer.reference()));
    }

    @Override
    public void end() {
        refs.endBatch();
    }

}
