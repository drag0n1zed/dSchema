package io.github.drag0n1zed.universal.api.networking;

import io.github.drag0n1zed.universal.api.core.ResourceLocation;

public interface PacketChannel extends PacketSender, PacketReceiver {

    int getCompatibilityVersion();

    ResourceLocation getChannelId();


}
