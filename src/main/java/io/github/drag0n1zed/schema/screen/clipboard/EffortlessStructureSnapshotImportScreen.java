package io.github.drag0n1zed.schema.screen.clipboard;

import java.util.function.Consumer;

import io.github.drag0n1zed.schema.SchemaClient;
import io.github.drag0n1zed.universal.api.gui.AbstractPanelScreen;
import io.github.drag0n1zed.universal.api.gui.button.Button;
import io.github.drag0n1zed.universal.api.gui.text.TextWidget;
import io.github.drag0n1zed.universal.api.platform.Entrance;
import io.github.drag0n1zed.universal.api.text.Text;
import io.github.drag0n1zed.schema.building.clipboard.Snapshot;

public class EffortlessStructureSnapshotImportScreen extends AbstractPanelScreen {

    private final Consumer<Snapshot> consumer;

    public EffortlessStructureSnapshotImportScreen(Entrance entrance, Consumer<Snapshot> consumer) {
        super(entrance, Text.translate("effortless.structure.import.title"), PANEL_WIDTH_42, PANEL_TITLE_HEIGHT_1 + PANEL_BUTTON_ROW_HEIGHT_3);
        this.consumer = consumer;
    }

    private Button importFromLitematicButton;
    private Button importFromEffortlessButton;

    @Override
    protected SchemaClient getEntrance() {
        return (SchemaClient) super.getEntrance();
    }

    @Override
    public void onCreate() {
        addWidget(new TextWidget(getEntrance(), getLeft() + getWidth() / 2, getTop() + PANEL_TITLE_HEIGHT_1 - 10, getScreenTitle().withColor(AbstractPanelScreen.TITLE_COLOR), TextWidget.Gravity.CENTER));

        this.importFromLitematicButton = addWidget(Button.builder(getEntrance(), Text.translate("effortless.structure.import.import_litematic", ".litematic"), button -> {
            detach();


        }).setBoundsGrid(getLeft(), getTop(), getWidth(), getHeight(), 2f, 0f, 1f).build());
        this.importFromEffortlessButton = addWidget(Button.builder(getEntrance(), Text.translate("effortless.structure.import.import_effortless", ".effortless"), button -> {
            detach();

        }).setBoundsGrid(getLeft(), getTop(), getWidth(), getHeight(), 1f, 0f, 1f).build());

        addWidget(Button.builder(getEntrance(), Text.translate("effortless.button.done"), button -> {
            detach();
        }).setBoundsGrid(getLeft(), getTop(), getWidth(), getHeight(), 0f, 0f, 1f).build());

    }

    @Override
    public void onReload() {

    }
}
