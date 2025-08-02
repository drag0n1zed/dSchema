package io.github.drag0n1zed.universal.vanilla.core;

import java.io.IOException;

import io.github.drag0n1zed.universal.api.core.Resource;
import io.github.drag0n1zed.universal.api.core.ResourceMetadata;

public record MinecraftResource(
        net.minecraft.server.packs.resources.Resource refs
) implements Resource {

    @Override
    public ResourceMetadata metadata() throws IOException {
        var metadata = refs.metadata();
        return () -> metadata;
    }

}
