package io.github.drag0n1zed.dschema.building.structure.builder.standard;

import java.util.stream.Stream;

import io.github.drag0n1zed.universal.api.core.BlockInteraction;
import io.github.drag0n1zed.universal.api.core.BlockPosition;
import io.github.drag0n1zed.universal.api.core.Player;
import io.github.drag0n1zed.dschema.building.Context;
import io.github.drag0n1zed.dschema.building.structure.BuildMode;
import io.github.drag0n1zed.dschema.building.structure.builder.BlockStructure;

public record Dome(

) implements BlockStructure {

    public BlockInteraction trace(Player player, Context context, int index) {
        return null;
    }

    public Stream<BlockPosition> collect(Context context, int index) {
        return null;
    }

    @Override
    public int traceSize(Context context) {
        return 3;
    }

    @Override
    public BuildMode getMode() {
        return null;
    }
}
