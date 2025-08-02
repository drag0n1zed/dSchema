package io.github.drag0n1zed.dschema.renderer;

import io.github.drag0n1zed.universal.api.core.ResourceLocation;
import io.github.drag0n1zed.universal.api.renderer.Shader;
import io.github.drag0n1zed.universal.api.renderer.VertexFormats;

public interface BlockShaders {

    Shader TINTED_OUTLINE = Shader.lazy(ResourceLocation.vanilla("rendertype_tinted_solid"), VertexFormats.BLOCK);

}
