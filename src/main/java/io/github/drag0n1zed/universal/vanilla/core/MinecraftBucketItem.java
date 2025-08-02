package io.github.drag0n1zed.universal.vanilla.core;

import io.github.drag0n1zed.universal.api.core.Block;
import io.github.drag0n1zed.universal.api.core.BlockInteraction;
import io.github.drag0n1zed.universal.api.core.BlockPosition;
import io.github.drag0n1zed.universal.api.core.BlockState;
import io.github.drag0n1zed.universal.api.core.BucketItem;
import io.github.drag0n1zed.universal.api.core.ItemStack;
import io.github.drag0n1zed.universal.api.core.Player;
import io.github.drag0n1zed.universal.api.core.ResourceLocation;
import io.github.drag0n1zed.universal.api.core.World;
import io.github.drag0n1zed.universal.api.core.fluid.Fluid;
import io.github.drag0n1zed.universal.api.text.Text;

public record MinecraftBucketItem(net.minecraft.world.item.BucketItem refs) implements BucketItem {

    @Override
    public Fluid getContent() {
        return MinecraftFluid.ofNullable(refs.getFluid());
    }

    @Override
    public boolean useContent(World world, Player player, BlockPosition blockPosition, BlockInteraction blockInteraction) {
        return refs.emptyContents(player.reference(), world.reference(), MinecraftConvertor.toPlatformBlockPosition(blockPosition), MinecraftConvertor.toPlatformBlockInteraction(blockInteraction));
    }

    @Override
    public void useExtraContent(World world, Player player, BlockPosition blockPosition, ItemStack itemStack) {
        refs.checkExtraContent(player.reference(), world.reference(), itemStack.reference(), MinecraftConvertor.toPlatformBlockPosition(blockPosition));
    }

    @Override
    public ItemStack getDefaultStack() {
        return new MinecraftItem(refs).getDefaultStack();
    }

    @Override
    public Block getBlock() {
        return new MinecraftItem(refs).getBlock();
    }

    @Override
    public ResourceLocation getId() {
        return new MinecraftItem(refs).getId();
    }

    @Override
    public boolean isCorrectToolForDrops(BlockState blockState) {
        return new MinecraftItem(refs).isCorrectToolForDrops(blockState);
    }

    @Override
    public int getMaxStackSize() {
        return new MinecraftItem(refs).getMaxStackSize();
    }

    @Override
    public int getMaxDamage() {
        return new MinecraftItem(refs).getMaxDamage();
    }

    @Override
    public boolean mineBlock(World world, Player player, BlockPosition blockPosition, BlockState blockState, ItemStack itemStack) {
        return new MinecraftItem(refs).mineBlock(world, player, blockPosition, blockState, itemStack);
    }

    @Override
    public Text getName(ItemStack itemStack) {
        return new MinecraftItem(refs).getName(itemStack);
    }

}
