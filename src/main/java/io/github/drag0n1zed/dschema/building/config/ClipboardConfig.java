package io.github.drag0n1zed.dschema.building.config;

import io.github.drag0n1zed.dschema.building.clipboard.Snapshot;

import java.util.List;
import java.util.stream.Stream;

public record ClipboardConfig(
        List<Snapshot> collections,
        List<Snapshot> history
) {

    public static ClipboardConfig DEFAULT = new ClipboardConfig();

    public ClipboardConfig() {
        this(List.of(), List.of());
    }

    public ClipboardConfig withCollections(List<Snapshot> collections) {
        return new ClipboardConfig(collections, history);
    }

    public ClipboardConfig withHistory(List<Snapshot> history) {
        return new ClipboardConfig(collections, history);
    }

    public ClipboardConfig appendHistory(Snapshot snapshot) {
        return new ClipboardConfig(collections, Stream.of(List.of(snapshot), history).flatMap(List::stream).toList());
    }

    public ClipboardConfig appendCollection(Snapshot snapshot) {
        return new ClipboardConfig(Stream.of(List.of(snapshot), collections).flatMap(List::stream).toList(), history);
    }

}
