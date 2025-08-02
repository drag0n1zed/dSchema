package io.github.drag0n1zed.universal.api.events;

@FunctionalInterface
public interface EventActor<T> {

    EventResult get(T t);

}
