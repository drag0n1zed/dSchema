package io.github.drag0n1zed.universal.api.networking;

import io.github.drag0n1zed.universal.api.core.Player;
import io.netty.buffer.ByteBuf;

public interface ByteBufReceiver {

    void receiveBuffer(ByteBuf byteBuf, Player player);

}
