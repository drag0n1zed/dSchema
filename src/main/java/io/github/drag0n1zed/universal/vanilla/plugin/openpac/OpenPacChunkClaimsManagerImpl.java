package io.github.drag0n1zed.universal.vanilla.plugin.openpac;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.drag0n1zed.universal.api.core.BlockPosition;
import io.github.drag0n1zed.universal.api.core.ChunkPosition;
import io.github.drag0n1zed.universal.api.core.ResourceLocation;
import io.github.drag0n1zed.universal.api.plugin.openpac.OpenPacChunkClaim;
import io.github.drag0n1zed.universal.api.plugin.openpac.OpenPacChunkClaimsManager;
import io.github.drag0n1zed.universal.vanilla.core.MinecraftConvertor;
import xaero.pac.common.claims.api.IClaimsManagerAPI;

public record OpenPacChunkClaimsManagerImpl(IClaimsManagerAPI refs) implements OpenPacChunkClaimsManager {

    public static OpenPacChunkClaimsManager ofNullable(IClaimsManagerAPI refs) {
        if (refs == null) return null;
        return new OpenPacChunkClaimsManagerImpl(refs);
    }

    @Override
    public @Nullable OpenPacChunkClaim get(@Nonnull ResourceLocation dimension, int x, int z) {
        var chunkClaim = refs.get(dimension.reference(), x, z);
        return OpenPacChunkClaimImpl.ofNullable(chunkClaim);
    }

    @Override
    public @Nullable OpenPacChunkClaim get(@Nonnull ResourceLocation dimension, @Nonnull ChunkPosition chunkPosition) {
        var chunkPos = MinecraftConvertor.toPlatformChunkPosition(chunkPosition);
        var chunkClaim = refs.get(dimension.reference(), chunkPos);
        return OpenPacChunkClaimImpl.ofNullable(chunkClaim);
    }

    @Override
    public @Nullable OpenPacChunkClaim get(@Nonnull ResourceLocation dimension, @Nonnull BlockPosition blockPosition) {
        var blockPos = MinecraftConvertor.toPlatformBlockPosition(blockPosition);
        var chunkClaim = refs.get(dimension.reference(), blockPos);
        return OpenPacChunkClaimImpl.ofNullable(chunkClaim);
    }

}
