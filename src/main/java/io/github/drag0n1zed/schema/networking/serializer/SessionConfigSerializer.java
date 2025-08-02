package io.github.drag0n1zed.schema.networking.serializer;

import io.github.drag0n1zed.universal.api.networking.NetByteBuf;
import io.github.drag0n1zed.universal.api.networking.NetByteBufSerializer;
import io.github.drag0n1zed.schema.session.config.SessionConfig;

public class SessionConfigSerializer implements NetByteBufSerializer<SessionConfig> {

    @Override
    public SessionConfig read(NetByteBuf byteBuf) {
        return new SessionConfig(
                byteBuf.read(new ConstraintConfigSerializer()),
                byteBuf.readMap(NetByteBuf::readUUID, new ConstraintConfigSerializer())
        );
    }

    @Override
    public void write(NetByteBuf byteBuf, SessionConfig sessionConfig) {
        byteBuf.write(sessionConfig.globalConfig(), new ConstraintConfigSerializer());
        byteBuf.writeMap(sessionConfig.playerConfigs(), NetByteBuf::writeUUID, new ConstraintConfigSerializer());
    }

}
