package io.github.drag0n1zed.universal.api.renderer;

import java.io.IOException;

import io.github.drag0n1zed.universal.api.core.ResourceLocation;
import io.github.drag0n1zed.universal.api.events.render.RegisterShader;
import io.github.drag0n1zed.universal.api.platform.PlatformReference;

public interface Shader extends PlatformReference {

    static Shader lazy(ResourceLocation location, VertexFormat format) {
        return new LazyShader(location, format);
    }

    ResourceLocation getResource();

    VertexFormat getVertexFormat();

    default void register(RegisterShader.ShadersSink sink) {
        try {
            sink.register(getResource(), getVertexFormat(), shader -> {
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    Uniform getUniform(String param);

}
