package io.github.drag0n1zed.universal.api.networking;

public interface NetByteBufWriter<T> {

    void write(NetByteBuf byteBuf, T t);

}
