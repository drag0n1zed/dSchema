package io.github.drag0n1zed.universal.api.texture;

import io.github.drag0n1zed.universal.api.platform.PlatformLoader;

public interface TextureFactory {

    static TextureFactory getInstance() {
        return PlatformLoader.getSingleton();
    }

    Texture getBlockAtlasTexture();

    TextureSprite getBackgroundTextureSprite();

    TextureSprite getButtonTextureSprite(boolean enabled, boolean focused);

    TextureSprite getDemoBackgroundTextureSprite();

}
