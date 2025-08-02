package io.github.drag0n1zed.universal.api.platform;

import io.github.drag0n1zed.universal.api.core.BlockPosition;
import io.github.drag0n1zed.universal.api.core.BlockState;
import io.github.drag0n1zed.universal.api.core.Direction;

public interface ParticleEngine extends PlatformReference {

    void destroy(BlockPosition blockPosition, BlockState blockState);

    void crack(BlockPosition blockPosition, Direction direction);
}
