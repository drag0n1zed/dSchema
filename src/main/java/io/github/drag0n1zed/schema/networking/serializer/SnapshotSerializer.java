package io.github.drag0n1zed.schema.networking.serializer;

import io.github.drag0n1zed.universal.api.networking.NetByteBuf;
import io.github.drag0n1zed.universal.api.networking.NetByteBufSerializer;
import io.github.drag0n1zed.schema.building.clipboard.Snapshot;

public class SnapshotSerializer implements NetByteBufSerializer<Snapshot> {

    @Override
    public Snapshot read(NetByteBuf byteBuf) {
        return new Snapshot(
                byteBuf.readString(),
                byteBuf.readLong(),
                byteBuf.readList(new BlockDataSerializer())
        );
    }

    @Override
    public void write(NetByteBuf byteBuf, Snapshot snapshot) {
        byteBuf.writeString(snapshot.name());
        byteBuf.writeLong(snapshot.createdTimestamp());
        byteBuf.writeList(snapshot.blockData(), new BlockDataSerializer());
    }

}
