package io.github.drag0n1zed.universal.api.texture;

import java.util.Set;

import io.github.drag0n1zed.universal.api.core.ResourceLocation;

public interface Texture {

    ResourceLocation resource();

    Set<ResourceLocation> sprites();

    TextureSprite getSprite(ResourceLocation name);

}
