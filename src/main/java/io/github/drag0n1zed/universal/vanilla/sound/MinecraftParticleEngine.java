package io.github.drag0n1zed.universal.vanilla.sound;

import io.github.drag0n1zed.universal.api.core.BlockPosition;
import io.github.drag0n1zed.universal.api.core.BlockState;
import io.github.drag0n1zed.universal.api.core.Direction;
import io.github.drag0n1zed.universal.api.platform.ParticleEngine;
import io.github.drag0n1zed.universal.vanilla.core.MinecraftConvertor;

public record MinecraftParticleEngine(
        net.minecraft.client.particle.ParticleEngine refs
) implements ParticleEngine {

    @Override
    public void destroy(BlockPosition blockPosition, BlockState blockState) {
        refs.destroy(MinecraftConvertor.toPlatformBlockPosition(blockPosition), blockState.reference());
    }

    @Override
    public void crack(BlockPosition blockPosition, Direction direction) {
        refs.crack(MinecraftConvertor.toPlatformBlockPosition(blockPosition), MinecraftConvertor.toPlatformDirection(direction));
    }

}
