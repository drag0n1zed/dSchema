package io.github.drag0n1zed.universal.vanilla.renderer;

import io.github.drag0n1zed.universal.api.core.ResourceLocation;
import io.github.drag0n1zed.universal.api.renderer.Shader;
import io.github.drag0n1zed.universal.api.renderer.Uniform;
import io.github.drag0n1zed.universal.api.renderer.VertexFormat;

public record MinecraftShader(
        net.minecraft.client.renderer.ShaderInstance refs
) implements Shader {

    @Override
    public ResourceLocation getResource() {
        return ResourceLocation.vanilla(refs.getName());
    }

    public VertexFormat getVertexFormat() {
        return () -> refs.getVertexFormat();
    }

    public Uniform getUniform(String param) {
        return new MinecraftUniform(refs.getUniform(param));
    }
}
