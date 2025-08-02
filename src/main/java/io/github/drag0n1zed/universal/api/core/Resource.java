package io.github.drag0n1zed.universal.api.core;

import java.io.IOException;

import io.github.drag0n1zed.universal.api.platform.PlatformReference;

public interface Resource extends PlatformReference {

//    ResourceLocation location();

    ResourceMetadata metadata() throws IOException;

}
