package io.github.drag0n1zed.universal.api.tag;

public interface TagSerializer<T> extends TagDecoder<T>, TagEncoder<T> {

    default T validate(T value) {
        return value;
    }

}
