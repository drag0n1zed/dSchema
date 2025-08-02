package io.github.drag0n1zed.dschema.screen.transformer;

import java.util.ArrayList;
import java.util.List;

import io.github.drag0n1zed.universal.api.core.Item;
import io.github.drag0n1zed.universal.api.core.ItemStack;
import io.github.drag0n1zed.universal.api.core.Player;
import io.github.drag0n1zed.universal.api.gui.Dimens;
import io.github.drag0n1zed.universal.api.gui.container.EditableEntryList;
import io.github.drag0n1zed.universal.api.gui.input.NumberField;
import io.github.drag0n1zed.universal.api.gui.slot.ItemSlot;
import io.github.drag0n1zed.universal.api.gui.text.TextWidget;
import io.github.drag0n1zed.universal.api.platform.Entrance;
import io.github.drag0n1zed.universal.api.text.ChatFormatting;
import io.github.drag0n1zed.universal.api.text.Text;
import io.github.drag0n1zed.dschema.building.pattern.randomize.Chance;

public final class ItemChanceList extends EditableEntryList<Chance<Item>> {

    public ItemChanceList(Entrance entrance, int x, int y, int width, int height) {
        super(entrance, x, y, width, height);
    }

    @Override
    protected EditableEntryList.Entry<Chance<Item>> createHolder(Chance<Item> item) {
        return new Entry(getEntrance(), this, item);
    }

    public static class Entry extends EditableEntryList.Entry<Chance<Item>> {

        private ItemSlot itemSlot;
        private TextWidget nameTextWidget;
        private TextWidget chanceTextWidget;
        private NumberField numberField;

        public Entry(Entrance entrance, ItemChanceList itemChanceList, Chance<Item> chance) {
            super(entrance, itemChanceList, chance);
        }

        public static List<Text> getRandomizerEntryTooltip(Player player, Chance<Item> chance, int totalCount) {
            var components = new ArrayList<>(chance.content().getDefaultStack().getTooltips(player, ItemStack.TooltipType.ADVANCED_CREATIVE));
            var percentage = String.format("%.2f%%", 100.0 * chance.chance() / totalCount);
            components.add(
                    Text.empty()
            );
            components.add(
                    Text.translate("dschema.transformer.randomizer.edit.total_probability", ChatFormatting.GOLD + percentage + ChatFormatting.DARK_GRAY + " (" + chance.chance() + "/" + totalCount + ")").withStyle(ChatFormatting.GRAY)
            );
            return components;
        }

        public int totalCount() {
            return getEntryList().items().stream().mapToInt(Chance::chance).sum();
        }

        @Override
        public ItemChanceList getEntryList() {
            return (ItemChanceList) super.getEntryList();
        }

        @Override
        public List<Text> getTooltip() {
            if (getEntrance().getClient().getWindow().isAltDown()) {
                return getRandomizerEntryTooltip(getEntrance().getClient().getPlayer(), getItem(), totalCount());
            } else {
                return super.getTooltip();
            }
        }

        @Override
        public void onCreate() {
            this.numberField = addWidget(new NumberField(getEntrance(), getX() + getWidth() - 48, getY() + 1, 48, 18, NumberField.TYPE_INTEGER));
            this.numberField.setValueRange(Chance.MIN_ITEM_COUNT, Chance.MAX_ITEM_COUNT);
            this.numberField.setValue(getItem().chance());
            this.numberField.setValueChangeListener(value -> {
                this.setItem(Chance.of(getItem().content(), value.intValue()));
            });
            this.itemSlot = addWidget(new ItemSlot(getEntrance(), getX() + 1, getY() + 1, Dimens.SLOT_WIDTH, Dimens.SLOT_HEIGHT, getItem().content(), Text.text(String.valueOf(getItem().chance()))));
            this.chanceTextWidget = addWidget(new TextWidget(getEntrance(), getX() + getWidth() - 48 - 4, getY() + 6, Text.empty(), TextWidget.Gravity.END));
            this.nameTextWidget = addWidget(new TextWidget(getEntrance(), getX() + 24, getY() + 6, getDisplayName(getItem()), TextWidget.Gravity.START));
        }

        @Override
        public void onReload() {
            this.chanceTextWidget.setMessage(String.format("%.2f%%", 100.0 * getItem().chance() / totalCount()));
            this.itemSlot.setItemStack(getItem().content().getDefaultStack());
            this.itemSlot.setDescription(Text.text(String.valueOf(getItem().chance())));
            this.nameTextWidget.setMessage(getDisplayName(getItem()));
            this.nameTextWidget.setWidth(getWidth() - 48 - 4 - Dimens.SLOT_WIDTH - 4 - chanceTextWidget.getWidth() - 8);
        }

        // TODO: 8/2/23
        @Override
        public Text getNarration() {
            return Text.translate("narrator.select", getDisplayName(getItem()));
        }

        private Text getDisplayName(Chance<Item> chance) {
            return chance.content().getDefaultStack().getHoverName();
        }

        @Override
        public int getHeight() {
            return 24;
        }
    }
}
