package io.github.drag0n1zed.dschema.building.pattern;

import io.github.drag0n1zed.universal.api.core.BlockEntity;
import io.github.drag0n1zed.universal.api.core.BlockInteraction;
import io.github.drag0n1zed.universal.api.core.BlockItem;
import io.github.drag0n1zed.universal.api.core.BlockState;
import io.github.drag0n1zed.universal.api.core.Item;
import io.github.drag0n1zed.universal.api.core.Player;

public class RefactorContext {

    private final Item item;

    public RefactorContext(Item item) {
        this.item = item;
    }

    public static RefactorContext of(Item item) {
        return new RefactorContext(item);
    }

    public BlockState refactor(Player player, BlockInteraction blockInteraction) {
        if (item == null) {
            return null;
        }
        if (item instanceof BlockItem blockItem) {
            return blockItem.getPlacementState(player, blockInteraction);
        }
        return null;
    }

    public BlockEntity refactor(BlockEntity blockEntity, BlockInteraction blockInteraction) {
        return blockEntity;
    }

}
