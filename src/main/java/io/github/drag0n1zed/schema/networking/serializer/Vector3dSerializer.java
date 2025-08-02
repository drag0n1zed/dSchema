package io.github.drag0n1zed.schema.networking.serializer;

import io.github.drag0n1zed.universal.api.math.Vector3d;
import io.github.drag0n1zed.universal.api.networking.NetByteBuf;
import io.github.drag0n1zed.universal.api.networking.NetByteBufSerializer;

public class Vector3dSerializer implements NetByteBufSerializer<Vector3d> {

    @Override
    public Vector3d read(NetByteBuf byteBuf) {
        return Vector3d.at(
                byteBuf.readDouble(),
                byteBuf.readDouble(),
                byteBuf.readDouble()
        );
    }


    @Override
    public void write(NetByteBuf byteBuf, Vector3d vector) {
        byteBuf.writeDouble(vector.x());
        byteBuf.writeDouble(vector.y());
        byteBuf.writeDouble(vector.z());
    }

}
