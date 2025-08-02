package io.github.drag0n1zed.schema.networking.serializer;

import io.github.drag0n1zed.universal.api.networking.NetByteBuf;
import io.github.drag0n1zed.universal.api.networking.NetByteBufSerializer;
import io.github.drag0n1zed.universal.api.platform.Mod;

public class ModSerializer implements NetByteBufSerializer<Mod> {

    @Override
    public Mod read(NetByteBuf byteBuf) {
        return Mod.create(
                byteBuf.readNullable(NetByteBuf::readString),
                byteBuf.readNullable(NetByteBuf::readString),
                byteBuf.readNullable(NetByteBuf::readString),
                byteBuf.readNullable(NetByteBuf::readString));
    }

    @Override
    public void write(NetByteBuf byteBuf, Mod mod) {
        byteBuf.writeNullable(mod.getId(), NetByteBuf::writeString);
        byteBuf.writeNullable(mod.getVersionStr(), NetByteBuf::writeString);
        byteBuf.writeNullable(null, NetByteBuf::writeString);
        byteBuf.writeNullable(mod.getName(), NetByteBuf::writeString);
    }
}
