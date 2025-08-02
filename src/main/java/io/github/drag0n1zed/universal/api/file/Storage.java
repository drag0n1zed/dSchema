package io.github.drag0n1zed.universal.api.file;

import java.util.function.UnaryOperator;

interface Storage<T> {

    default void update(UnaryOperator<T> operator) {
        set(operator.apply(get()));
    }

    T get();

    void set(T config);

}
