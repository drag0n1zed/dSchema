package io.github.drag0n1zed.universal.vanilla.core;

import io.github.drag0n1zed.universal.api.core.BlockEntity;
import io.github.drag0n1zed.universal.api.core.BlockPosition;
import io.github.drag0n1zed.universal.api.core.BlockState;
import io.github.drag0n1zed.universal.api.core.World;
import io.github.drag0n1zed.universal.api.tag.RecordTag;
import io.github.drag0n1zed.universal.vanilla.tag.MinecraftRecordTag;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;

public record MinecraftBlockEntity(net.minecraft.world.level.block.entity.BlockEntity refs) implements BlockEntity {

    public static BlockEntity ofNullable(net.minecraft.world.level.block.entity.BlockEntity refs) {
        if (refs == null) return null;
        if (refs instanceof BaseContainerBlockEntity baseContainerBlockEntity) return new MinecraftContainerBlockEntity(baseContainerBlockEntity);
        return new MinecraftBlockEntity(refs);
    }

    @Override
    public BlockState getBlockState() {
        return MinecraftBlockState.ofNullable(refs.getBlockState());
    }

    @Override
    public BlockPosition getBlockPosition() {
        return MinecraftConvertor.toPlatformBlockPosition(refs.getBlockPos());
    }

    @Override
    public World getWorld() {
        return MinecraftWorld.ofNullable(refs.getLevel());
    }

    @Override
    public RecordTag getTag() {
        return MinecraftRecordTag.ofNullable(refs.saveWithoutMetadata());
    }

    @Override
    public void setTag(RecordTag recordTag) {
        refs.load(recordTag.reference());
    }
}
