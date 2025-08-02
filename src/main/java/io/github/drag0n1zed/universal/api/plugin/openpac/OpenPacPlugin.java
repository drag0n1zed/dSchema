package io.github.drag0n1zed.universal.api.plugin.openpac;

import javax.annotation.Nonnull;

import io.github.drag0n1zed.universal.api.platform.Plugin;
import io.github.drag0n1zed.universal.api.platform.Server;

public interface OpenPacPlugin extends Plugin {

    OpenPacChunkClaimsManager getServerClaimManager(@Nonnull Server server);

}
