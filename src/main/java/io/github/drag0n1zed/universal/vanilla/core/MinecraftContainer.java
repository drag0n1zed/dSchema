package io.github.drag0n1zed.universal.vanilla.core;

import io.github.drag0n1zed.universal.api.core.Container;
import io.github.drag0n1zed.universal.api.core.ItemStack;

public record MinecraftContainer(net.minecraft.world.Container refs) implements Container {

    @Override
    public ItemStack getItem(int index) {
        try {
            return new MinecraftItemStack(refs.getItem(index));
        } catch (NullPointerException e) {
            return new MinecraftItemStack(net.minecraft.world.item.ItemStack.EMPTY);
        }
    }

    @Override
    public void setItem(int index, ItemStack itemStack) {
        refs.setItem(index, itemStack.reference());
    }

    @Override
    public int getContainerSize() {
        return refs.getContainerSize();
    }

}
