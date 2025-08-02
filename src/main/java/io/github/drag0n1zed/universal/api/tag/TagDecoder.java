package io.github.drag0n1zed.universal.api.tag;

public interface TagDecoder<T> {

    T decode(Tag tag);

    default T validate(T value) {
        return value;
    }

    default T decode(Tag tag, boolean validate) {
        return validate ? validate(decode(tag)) : decode(tag);
    }

}
