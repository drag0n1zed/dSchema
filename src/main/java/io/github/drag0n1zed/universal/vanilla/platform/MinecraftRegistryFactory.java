package io.github.drag0n1zed.universal.vanilla.platform;

import com.google.auto.service.AutoService;

import io.github.drag0n1zed.universal.api.core.BlockState;
import io.github.drag0n1zed.universal.api.core.Item;
import io.github.drag0n1zed.universal.api.core.Registry;
import io.github.drag0n1zed.universal.api.platform.PlatformReference;
import io.github.drag0n1zed.universal.api.platform.RegistryFactory;
import io.github.drag0n1zed.universal.vanilla.core.MinecraftBlockState;
import io.github.drag0n1zed.universal.vanilla.core.MinecraftItem;
import io.github.drag0n1zed.universal.vanilla.core.MinecraftRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;

@AutoService(RegistryFactory.class)
public final class MinecraftRegistryFactory implements RegistryFactory {

    @SuppressWarnings("unchecked")
    @Override
    public <T extends PlatformReference> Registry<T> getRegistry(Class<T> clazz) {
        if (clazz == Item.class) return (Registry<T>) new MinecraftRegistry<>(BuiltInRegistries.ITEM, MinecraftItem::ofNullable);
        if (clazz == BlockState.class) return (Registry<T>) new MinecraftRegistry<>(Block.BLOCK_STATE_REGISTRY, MinecraftBlockState::ofNullable);
        return null;
    }


}
