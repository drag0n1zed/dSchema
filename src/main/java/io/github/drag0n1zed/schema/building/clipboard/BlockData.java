package io.github.drag0n1zed.schema.building.clipboard;

import io.github.drag0n1zed.universal.api.core.BlockEntity;
import io.github.drag0n1zed.universal.api.core.BlockPosition;
import io.github.drag0n1zed.universal.api.core.BlockState;
import io.github.drag0n1zed.universal.api.tag.RecordTag;
import io.github.drag0n1zed.schema.building.operation.Mirrorable;
import io.github.drag0n1zed.schema.building.operation.Movable;
import io.github.drag0n1zed.schema.building.operation.Rotatable;
import io.github.drag0n1zed.schema.building.pattern.MirrorContext;
import io.github.drag0n1zed.schema.building.pattern.MoveContext;
import io.github.drag0n1zed.schema.building.pattern.RotateContext;

public record BlockData(
        BlockPosition blockPosition,
        BlockState blockState,
        RecordTag entityTag
) implements Rotatable<BlockData>, Movable<BlockData>, Mirrorable<BlockData> {

    @Override
    public BlockData rotate(RotateContext rotateContext) {
        return new BlockData(
                rotateContext.rotate(blockPosition),
                rotateContext.rotate(blockState),
                entityTag
        );
    }

    @Override
    public BlockData move(MoveContext moveContext) {
        return new BlockData(
                moveContext.move(blockPosition),
                blockState,
                entityTag
        );
    }

    @Override
    public BlockData mirror(MirrorContext mirrorContext) {
        return new BlockData(
                mirrorContext.mirror(blockPosition),
                mirrorContext.mirror(blockState),
                entityTag
        );
    }

    public BlockEntity blockEntity() {
        if (blockState == null) {
            return null;
        }
        var blockEntity = blockState.getEntity(blockPosition);
        if (blockEntity == null) {
            return null;
        }
        if (entityTag != null) {
            blockEntity.setTag(entityTag);
        }
        return blockEntity;
    }
}
