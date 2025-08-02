package io.github.drag0n1zed.schema.networking.serializer;

import io.github.drag0n1zed.universal.api.core.BlockPosition;
import io.github.drag0n1zed.universal.api.networking.NetByteBuf;
import io.github.drag0n1zed.universal.api.networking.NetByteBufSerializer;

public class BlockPositionSerializer implements NetByteBufSerializer<BlockPosition> {

    @Override
    public BlockPosition read(NetByteBuf byteBuf) {
        return BlockPosition.at(
                byteBuf.readVarInt(),
                byteBuf.readVarInt(),
                byteBuf.readVarInt()
        );
    }


    @Override
    public void write(NetByteBuf byteBuf, BlockPosition blockPosition) {
        byteBuf.writeVarInt(blockPosition.x());
        byteBuf.writeVarInt(blockPosition.y());
        byteBuf.writeVarInt(blockPosition.z());
    }

}
