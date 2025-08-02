package io.github.drag0n1zed.universal.api.plugin.openpac;

import io.github.drag0n1zed.universal.api.platform.ClientPlugin;

public interface OpenPacClientPlugin extends ClientPlugin {

    OpenPacChunkClaimsManager getClaimManager();

}
