package io.github.drag0n1zed.universal.api.networking;

import io.github.drag0n1zed.universal.api.core.Player;
import io.netty.buffer.ByteBuf;

public interface ByteBufSender {

    void sendBuffer(ByteBuf byteBuf, Player player);

}
