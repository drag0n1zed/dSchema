package io.github.drag0n1zed.universal.vanilla.plugin.openpac;

import java.util.UUID;

import javax.annotation.Nonnull;

import io.github.drag0n1zed.universal.api.plugin.openpac.OpenPacChunkClaim;
import xaero.pac.common.claims.player.api.IPlayerChunkClaimAPI;

public record OpenPacChunkClaimImpl(IPlayerChunkClaimAPI refs) implements OpenPacChunkClaim {

    public static OpenPacChunkClaim ofNullable(IPlayerChunkClaimAPI refs) {
        if (refs == null) return null;
        return new OpenPacChunkClaimImpl(refs);
    }

    @Override
    public @Nonnull UUID getPlayerId() {
        return refs.getPlayerId();
    }

}
