package io.github.drag0n1zed.universal.api.core.fluid;

import java.util.Optional;

import io.github.drag0n1zed.universal.api.core.BlockPosition;
import io.github.drag0n1zed.universal.api.core.BlockState;
import io.github.drag0n1zed.universal.api.core.ItemStack;
import io.github.drag0n1zed.universal.api.core.Player;
import io.github.drag0n1zed.universal.api.core.World;
import io.github.drag0n1zed.universal.api.sound.Sound;

public interface BucketCollectable {

    ItemStack pickupBlock(World world, Player player, BlockPosition blockPosition, BlockState blockState);

    Optional<Sound> getPickupSound();

}
