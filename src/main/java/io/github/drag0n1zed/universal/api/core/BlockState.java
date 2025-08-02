package io.github.drag0n1zed.universal.api.core;

import io.github.drag0n1zed.universal.api.core.fluid.Fluid;
import io.github.drag0n1zed.universal.api.platform.RegistryFactory;
import io.github.drag0n1zed.universal.api.sound.SoundSet;

public interface BlockState extends StateHolder {

    Registry<BlockState> REGISTRY = RegistryFactory.getInstance().getRegistry();

    BlockState mirror(Axis axis);

    BlockState rotate(Revolve revolve);

    boolean isAir();

    boolean canBeReplaced(Player player, BlockInteraction interaction);

    boolean isReplaceable();

    boolean hasTagFeatureCannotReplace();

    SoundSet getSoundSet();

    Block getBlock();

    boolean canBeReplaced(Fluid fluid);

    InteractionResult use(Player player, BlockInteraction blockInteraction);

    boolean requiresCorrectToolForDrops();

    default Item getItem() {
        return getBlock().asItem();
    }

    default BlockEntity getEntity(BlockPosition blockPosition) {
        return getBlock().getEntity(blockPosition, this);
    }

    @Deprecated
    int getRequiredItemCount(); // this is a temp solution for slab blocks drop count, will replace with loot table in the future.

}
