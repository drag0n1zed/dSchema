package io.github.drag0n1zed.universal.api.command;

import java.util.Locale;
import java.util.stream.Collectors;

import io.github.drag0n1zed.universal.api.core.BlockPosition;
import io.github.drag0n1zed.universal.api.core.BlockState;
import io.github.drag0n1zed.universal.api.core.PropertyHolder;

public record SetBlockCommand(
        BlockState blockState,
        BlockPosition blockPosition,
        Mode mode
) implements Command {

    public static final String COMMAND = "setblock";

    @Override
    public String build() {
        return "%s %d %d %d %s %s".formatted(
                COMMAND,
                blockPosition.x(),
                blockPosition.y(),
                blockPosition.z(),
                getPropertiesString(blockState),
                mode.name().toLowerCase(Locale.ROOT)
        );
    }

    public String getPropertiesString(BlockState blockState) {
        return blockState.getItem().getId().getString() + "[" + blockState.getProperties().stream().map(PropertyHolder::getAsString).collect(Collectors.joining(",")) + "]";
    }

    public enum Mode {
        DESTROY,
        KEEP,
        REPLACE
    }

}
