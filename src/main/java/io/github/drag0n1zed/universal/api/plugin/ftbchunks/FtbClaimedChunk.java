package io.github.drag0n1zed.universal.api.plugin.ftbchunks;

import io.github.drag0n1zed.universal.api.platform.PlatformReference;

import java.util.UUID;

public interface FtbClaimedChunk extends PlatformReference {

    boolean isTeamMember(UUID uuid);

}
