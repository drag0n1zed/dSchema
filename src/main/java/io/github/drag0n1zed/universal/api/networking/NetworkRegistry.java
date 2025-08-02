package io.github.drag0n1zed.universal.api.networking;

import io.github.drag0n1zed.universal.api.core.ResourceLocation;

public interface NetworkRegistry {

    ByteBufSender register(ResourceLocation channelId, Side side, ByteBufReceiver receiver);

}
