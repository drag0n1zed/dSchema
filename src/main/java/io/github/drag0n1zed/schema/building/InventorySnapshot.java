package io.github.drag0n1zed.schema.building;

import java.util.List;

import io.github.drag0n1zed.universal.api.core.Inventory;
import io.github.drag0n1zed.universal.api.core.ItemStack;

public record InventorySnapshot(
        List<ItemStack> items, int selected, int bagSize, int armorSize, int offhandSize, int hotbarSize
) implements Inventory {

    public InventorySnapshot(Inventory inventory) {
        this(inventory.getItems(), inventory.getSelected(), inventory.getBagSize(), inventory.getArmorSize(), inventory.getOffhandSize(), inventory.getHotbarSize());
    }

    @Override
    public ItemStack getItem(int index) {
        return items.get(index);
    }

    @Override
    public void setItem(int index, ItemStack itemStack) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getSelected() {
        return selected;
    }

    @Override
    public int getBagSize() {
        return bagSize;
    }

    @Override
    public int getArmorSize() {
        return armorSize;
    }

    @Override
    public int getOffhandSize() {
        return offhandSize;
    }

    @Override
    public int getHotbarSize() {
        return hotbarSize;
    }

    @Override
    public InventorySnapshot refs() {
        return this;
    }
}
