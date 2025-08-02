package io.github.drag0n1zed.universal.api.gui.slot;

import io.github.drag0n1zed.universal.api.core.Item;
import io.github.drag0n1zed.universal.api.core.ItemStack;
import io.github.drag0n1zed.universal.api.platform.Entrance;
import io.github.drag0n1zed.universal.api.renderer.Renderer;
import io.github.drag0n1zed.universal.api.text.Text;

public class ItemSlot extends Slot {

    private ItemStack itemStack;

    public ItemSlot(Entrance entrance, int x, int y, int width, int height, Item item, Text message) {
        super(entrance, x, y, width, height, message);
        this.itemStack = item.getDefaultStack();
    }

    public ItemSlot(Entrance entrance, int x, int y, int width, int height, ItemStack itemStack, Text message) {
        super(entrance, x, y, width, height, message);
        this.itemStack = itemStack;
    }

    @Override
    public int getFullWidth() {
        return getWidth();
    }

    @Override
    public void renderWidget(Renderer renderer, int mouseX, int mouseY, float deltaTick) {
        super.renderWidget(renderer, mouseX, mouseY, deltaTick);

//        renderer.drawItemSlotBackgroundTexture(getX() + 1, getY() + 1);
        renderer.renderRect(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x9f6c6c6c);
        renderer.renderItem(getTypeface(), itemStack, getX(), getY(), getMessage());
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public void setDescription(Text description) {
        this.setMessage(description);
    }

    private int getBlitOffset() {
        return 0;
    }

}
