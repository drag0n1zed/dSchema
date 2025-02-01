package dev.huskuraft.effortless.building.pattern;

import dev.huskuraft.effortless.api.core.BlockEntity;
import dev.huskuraft.effortless.api.core.BlockInteraction;
import dev.huskuraft.effortless.api.core.BlockItem;
import dev.huskuraft.effortless.api.core.BlockState;
import dev.huskuraft.effortless.api.core.Item;
import dev.huskuraft.effortless.api.core.Player;

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
            return blockItem.getPlacementState(player,  blockInteraction);
        }
        return null;
    }

    public BlockEntity refactor(BlockEntity blockEntity, BlockInteraction blockInteraction) {
        return blockEntity;
    }

}
