package io.github.drag0n1zed.universal.api.plugin.ftbchunks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.drag0n1zed.universal.api.core.BlockPosition;
import io.github.drag0n1zed.universal.api.core.World;

public interface FtbChunkClaimsManager {

    @Nullable
    FtbClaimedChunk get(@Nonnull World world, @Nonnull BlockPosition blockPosition);

}
