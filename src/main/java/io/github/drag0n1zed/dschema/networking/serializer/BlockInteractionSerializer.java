package io.github.drag0n1zed.dschema.networking.serializer;

import io.github.drag0n1zed.universal.api.core.BlockInteraction;
import io.github.drag0n1zed.universal.api.core.Direction;
import io.github.drag0n1zed.universal.api.networking.NetByteBuf;
import io.github.drag0n1zed.universal.api.networking.NetByteBufSerializer;

public class BlockInteractionSerializer implements NetByteBufSerializer<BlockInteraction> {

    @Override
    public BlockInteraction read(NetByteBuf byteBuf) {
        return new BlockInteraction(
                byteBuf.read(new Vector3dSerializer()),
                byteBuf.readEnum(Direction.class),
                byteBuf.read(new BlockPositionSerializer()),
                byteBuf.readBoolean(),
                byteBuf.readBoolean()
        );
    }

    @Override
    public void write(NetByteBuf byteBuf, BlockInteraction blockInteraction) {
        byteBuf.write(blockInteraction.getPosition(), new Vector3dSerializer());
        byteBuf.writeEnum(blockInteraction.getDirection());
        byteBuf.write(blockInteraction.getBlockPosition(), new BlockPositionSerializer());
        byteBuf.writeBoolean(blockInteraction.isInside());
        byteBuf.writeBoolean(blockInteraction.isMiss());
    }

}
