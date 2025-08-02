package io.github.drag0n1zed.universal.api.plugin.openpac;

import io.github.drag0n1zed.universal.api.core.BlockPosition;
import io.github.drag0n1zed.universal.api.core.ChunkPosition;
import io.github.drag0n1zed.universal.api.core.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface OpenPacChunkClaimsManager {

    @Nullable
    OpenPacChunkClaim get(@Nonnull ResourceLocation dimension, int x, int z);

    @Nullable
    OpenPacChunkClaim get(@Nonnull ResourceLocation dimension, @Nonnull ChunkPosition chunkPosition);

    @Nullable
    OpenPacChunkClaim get(@Nonnull ResourceLocation dimension, @Nonnull BlockPosition blockPosition);
}
