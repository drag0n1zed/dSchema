package io.github.drag0n1zed.dschema.networking.serializer;

import io.github.drag0n1zed.universal.api.math.Vector3i;
import io.github.drag0n1zed.universal.api.networking.NetByteBuf;
import io.github.drag0n1zed.universal.api.networking.NetByteBufSerializer;

public class Vector3iSerializer implements NetByteBufSerializer<Vector3i> {

    @Override
    public Vector3i read(NetByteBuf byteBuf) {
        return Vector3i.at(
                byteBuf.readVarInt(),
                byteBuf.readVarInt(),
                byteBuf.readVarInt()
        );
    }


    @Override
    public void write(NetByteBuf byteBuf, Vector3i vector) {
        byteBuf.writeVarInt(vector.x());
        byteBuf.writeVarInt(vector.y());
        byteBuf.writeVarInt(vector.z());
    }

}
