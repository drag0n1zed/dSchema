package io.github.drag0n1zed.dschema.screen.item;

import java.util.List;
import java.util.function.Consumer;

import io.github.drag0n1zed.universal.api.core.Item;
import io.github.drag0n1zed.universal.api.core.ItemStack;
import io.github.drag0n1zed.universal.api.core.Items;
import io.github.drag0n1zed.universal.api.gui.AbstractPanelScreen;
import io.github.drag0n1zed.universal.api.gui.button.Button;
import io.github.drag0n1zed.universal.api.gui.text.TextWidget;
import io.github.drag0n1zed.universal.api.platform.Entrance;
import io.github.drag0n1zed.universal.api.text.Text;

public class SchemaItemsScreen extends AbstractPanelScreen {

    private final Consumer<List<Item>> consumer;
    private TextWidget titleTextWidget;
    private ItemStackList entries;
    private Button deleteButton;
    private Button clearButton;
    private Button addButton;
    private Button cancelButton;
    private Button saveButton;

    private List<Item> originalItems;
    private List<Item> items;

    public SchemaItemsScreen(Entrance entrance, Text title, List<Item> items, Consumer<List<Item>> consumer) {
        super(entrance, title, PANEL_WIDTH_60, PANEL_HEIGHT_FULL);
        this.consumer = consumer;
        this.originalItems = items.stream().distinct().toList();
        this.items = items.stream().distinct().toList();
    }

    @Override
    public void onCreate() {

        this.titleTextWidget = addWidget(new TextWidget(getEntrance(), getLeft() + getWidth() / 2, getTop() + PANEL_TITLE_HEIGHT_1 - 10, getScreenTitle().withColor(AbstractPanelScreen.TITLE_COLOR), TextWidget.Gravity.CENTER));

        this.entries = addWidget(new ItemStackList(getEntrance(), getLeft() + PADDINGS_H, getTop() + PANEL_TITLE_HEIGHT_1, getWidth() - PADDINGS_H * 2 - 8, getHeight() - PANEL_TITLE_HEIGHT_1 - PANEL_BUTTON_ROW_HEIGHT_2));
        this.entries.setAlwaysShowScrollbar(true);

        this.deleteButton = addWidget(Button.builder(getEntrance(), Text.translate("dschema.button.delete"), button -> {
            if (entries.hasSelected()) {
                entries.deleteSelected();
            }
        }).setBoundsGrid(getLeft(), getTop(), getWidth(), getHeight(), 1f, 0f, 1 / 3f).build());

        this.clearButton = addWidget(Button.builder(getEntrance(), Text.translate("dschema.button.clear"), button -> {
            entries.clear();
        }).setBoundsGrid(getLeft(), getTop(), getWidth(), getHeight(), 1f, 1 / 3f, 1 / 3f).build());
        this.addButton = addWidget(Button.builder(getEntrance(), Text.translate("dschema.button.add"), button -> {
            new SchemaItemPickerScreen(getEntrance(), item -> item != Items.AIR.item(), (item) -> {
                if (item != null && !items.contains(item)) {
                    entries.insertSelected(item.getDefaultStack());
                    onReload();
                }
            }).attach();
        }).setBoundsGrid(getLeft(), getTop(), getWidth(), getHeight(), 1f, 2 / 3f, 1 / 3f).build());
        this.entries.reset(items.stream().map(Item::getDefaultStack).toList());

        this.cancelButton = addWidget(Button.builder(getEntrance(), Text.translate("dschema.button.cancel"), button -> {
            detach();
        }).setBoundsGrid(getLeft(), getTop(), getWidth(), getHeight(), 0f, 0f, 0.5f).build());

        this.saveButton = addWidget(Button.builder(getEntrance(), Text.translate("dschema.button.save"), button -> {
            consumer.accept(items);
            detach();
        }).setBoundsGrid(getLeft(), getTop(), getWidth(), getHeight(), 0f, 0.5f, 0.5f).build());

    }

    @Override
    public void onReload() {
        this.items = entries.items().stream().map(ItemStack::getItem).toList();
        this.deleteButton.setActive(entries.hasSelected());
        this.clearButton.setActive(!entries.items().isEmpty());
    }

}
