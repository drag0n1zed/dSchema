package io.github.drag0n1zed.dschema.building.interceptor;

import io.github.drag0n1zed.universal.api.core.BlockPosition;
import io.github.drag0n1zed.universal.api.core.Player;
import io.github.drag0n1zed.universal.api.core.World;

public interface BuildInterceptor {

    boolean isEnabled();

    boolean allowInteraction(Player player, World world, BlockPosition blockPosition);

}
