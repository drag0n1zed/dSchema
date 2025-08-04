package io.github.drag0n1zed.dschema.networking.serializer;

import io.github.drag0n1zed.universal.api.networking.NetByteBuf;
import io.github.drag0n1zed.universal.api.networking.NetByteBufSerializer;
import io.github.drag0n1zed.dschema.session.config.SessionConfig;

public class SessionConfigSerializer implements NetByteBufSerializer<SessionConfig> {

    @Override
    public SessionConfig read(NetByteBuf byteBuf) {
        // We need to read all three fields now to match the constructor
        return new SessionConfig(
                byteBuf.read(new ConstraintConfigSerializer()),
                byteBuf.readMap(NetByteBuf::readUUID, new ConstraintConfigSerializer()),
                byteBuf.readMap(NetByteBuf::readString, new ConstraintConfigSerializer()) // Read the new tiers map
        );
    }

    @Override
    public void write(NetByteBuf byteBuf, SessionConfig sessionConfig) {
        // We need to write all three fields
        byteBuf.write(sessionConfig.globalConfig(), new ConstraintConfigSerializer());
        byteBuf.writeMap(sessionConfig.playerConfigs(), NetByteBuf::writeUUID, new ConstraintConfigSerializer());
        byteBuf.writeMap(sessionConfig.tiers(), NetByteBuf::writeString, new ConstraintConfigSerializer()); // Write the new tiers map
    }
}