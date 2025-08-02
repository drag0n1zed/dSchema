package io.github.drag0n1zed.schema.networking.serializer;

import io.github.drag0n1zed.universal.api.networking.NetByteBuf;
import io.github.drag0n1zed.universal.api.networking.NetByteBufSerializer;
import io.github.drag0n1zed.schema.building.clipboard.BlockData;

public class BlockDataSerializer implements NetByteBufSerializer<BlockData> {
    @Override
    public BlockData read(NetByteBuf byteBuf) {
        return new BlockData(
                byteBuf.read(new BlockPositionSerializer()),
                byteBuf.readNullable(NetByteBuf::readBlockState),
                byteBuf.readNullable(NetByteBuf::readRecordTag)
        );
    }

    @Override
    public void write(NetByteBuf byteBuf, BlockData blockData) {
        byteBuf.write(blockData.blockPosition(), new BlockPositionSerializer());
        byteBuf.writeNullable(blockData.blockState(), NetByteBuf::writeBlockState);
        byteBuf.writeNullable(blockData.entityTag(), NetByteBuf::writeRecordTag);
    }
}
