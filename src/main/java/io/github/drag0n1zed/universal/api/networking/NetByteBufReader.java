package io.github.drag0n1zed.universal.api.networking;

public interface NetByteBufReader<T> {

    T read(NetByteBuf byteBuf);

}
