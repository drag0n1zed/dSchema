package io.github.drag0n1zed.schema.networking.serializer;

import io.github.drag0n1zed.universal.api.networking.NetByteBuf;
import io.github.drag0n1zed.universal.api.networking.NetByteBufSerializer;
import io.github.drag0n1zed.schema.session.config.ConstraintConfig;

public class ConstraintConfigSerializer implements NetByteBufSerializer<ConstraintConfig> {

    @Override
    public ConstraintConfig read(NetByteBuf byteBuf) {
        return new ConstraintConfig(
                byteBuf.readNullable(NetByteBuf::readBoolean),
                byteBuf.readNullable(NetByteBuf::readBoolean),
                byteBuf.readNullable(NetByteBuf::readBoolean),
                byteBuf.readNullable(NetByteBuf::readBoolean),
                byteBuf.readNullable(NetByteBuf::readBoolean),
                byteBuf.readNullable(NetByteBuf::readBoolean),
                byteBuf.readNullable(NetByteBuf::readBoolean),
                byteBuf.readNullable(NetByteBuf::readVarInt),
                byteBuf.readNullable(NetByteBuf::readVarInt),
                byteBuf.readNullable(NetByteBuf::readVarInt),
                byteBuf.readNullable(NetByteBuf::readVarInt),
                byteBuf.readNullable(NetByteBuf::readVarInt),
                byteBuf.readNullable(buffer1 -> buffer1.readList(NetByteBuf::readResourceLocation)),
                byteBuf.readNullable(buffer1 -> buffer1.readList(NetByteBuf::readResourceLocation)));
    }

    @Override
    public void write(NetByteBuf byteBuf, ConstraintConfig constraintConfig) {
        byteBuf.writeNullable(constraintConfig.useCommands(), NetByteBuf::writeBoolean);
        byteBuf.writeNullable(constraintConfig.allowUseMod(), NetByteBuf::writeBoolean);
        byteBuf.writeNullable(constraintConfig.allowBreakBlocks(), NetByteBuf::writeBoolean);
        byteBuf.writeNullable(constraintConfig.allowPlaceBlocks(), NetByteBuf::writeBoolean);
        byteBuf.writeNullable(constraintConfig.allowInteractBlocks(), NetByteBuf::writeBoolean);
        byteBuf.writeNullable(constraintConfig.allowCopyPasteStructures(), NetByteBuf::writeBoolean);
        byteBuf.writeNullable(constraintConfig.useProperToolsOnly(), NetByteBuf::writeBoolean);
        byteBuf.writeNullable(constraintConfig.maxReachDistance(), NetByteBuf::writeVarInt);
        byteBuf.writeNullable(constraintConfig.maxBlockBreakVolume(), NetByteBuf::writeVarInt);
        byteBuf.writeNullable(constraintConfig.maxBlockPlaceVolume(), NetByteBuf::writeVarInt);
        byteBuf.writeNullable(constraintConfig.maxBlockInteractVolume(), NetByteBuf::writeVarInt);
        byteBuf.writeNullable(constraintConfig.maxStructureCopyPasteVolume(), NetByteBuf::writeVarInt);
        byteBuf.writeNullable(constraintConfig.whitelistedItems(), (buffer1, list) -> buffer1.writeList(list, NetByteBuf::writeResourceLocation));
        byteBuf.writeNullable(constraintConfig.blacklistedItems(), (buffer1, list) -> buffer1.writeList(list, NetByteBuf::writeResourceLocation));

    }
}
