package io.github.drag0n1zed.universal.api.core;

import io.github.drag0n1zed.universal.api.platform.PlatformReference;
import io.github.drag0n1zed.universal.api.tag.RecordTag;

public interface BlockEntity extends PlatformReference {

    BlockState getBlockState();

    BlockPosition getBlockPosition();

    World getWorld();

    RecordTag getTag();

    void setTag(RecordTag recordTag);

    default BlockEntity copy() {
        var tag = getTag();
        var newBlockEntity = getBlockState().getEntity(getBlockPosition());
        if (newBlockEntity != null) {
            newBlockEntity.setTag(tag);
        }
        return newBlockEntity;
    }

}
