package io.github.drag0n1zed.universal.api.plugin.openpac;

import io.github.drag0n1zed.universal.api.platform.PlatformReference;

import javax.annotation.Nonnull;

import java.util.UUID;

public interface OpenPacChunkClaim extends PlatformReference {

    @Nonnull
    UUID getPlayerId();

}
