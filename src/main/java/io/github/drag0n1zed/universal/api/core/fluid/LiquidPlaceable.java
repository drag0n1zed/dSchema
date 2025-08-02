package io.github.drag0n1zed.universal.api.core.fluid;

import io.github.drag0n1zed.universal.api.core.BlockPosition;
import io.github.drag0n1zed.universal.api.core.BlockState;
import io.github.drag0n1zed.universal.api.core.FluidState;
import io.github.drag0n1zed.universal.api.core.Player;
import io.github.drag0n1zed.universal.api.core.World;

public interface LiquidPlaceable {

    boolean canPlaceLiquid(World world/*BlockGetter*/, Player player, BlockPosition blockPosition, BlockState blockState, Fluid fluid);

    boolean placeLiquid(World world, BlockPosition blockPosition, BlockState blockState, FluidState fluidState);

}
