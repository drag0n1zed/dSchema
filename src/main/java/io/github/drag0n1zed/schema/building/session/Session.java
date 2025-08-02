package io.github.drag0n1zed.schema.building.session;

import io.github.drag0n1zed.universal.api.core.Player;
import io.github.drag0n1zed.universal.api.core.World;
import io.github.drag0n1zed.schema.building.interceptor.BuildInterceptor;
import io.github.drag0n1zed.schema.building.operation.OperationResult;

import java.util.List;

public interface Session {

    World getWorld();

    Player getPlayer();

    OperationResult commit();

    List<BuildInterceptor> getInterceptors();

}
