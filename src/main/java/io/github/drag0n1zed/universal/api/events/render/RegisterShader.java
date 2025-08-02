package io.github.drag0n1zed.universal.api.events.render;


import java.io.IOException;
import java.util.function.Consumer;

import io.github.drag0n1zed.universal.api.core.ResourceLocation;
import io.github.drag0n1zed.universal.api.renderer.Shader;
import io.github.drag0n1zed.universal.api.renderer.VertexFormat;

public interface RegisterShader {

    void onRegisterShader(ShadersSink sink);

    @FunctionalInterface
    interface ShadersSink {
        void register(ResourceLocation location, VertexFormat format, Consumer<Shader> consumer) throws IOException;
    }

}
