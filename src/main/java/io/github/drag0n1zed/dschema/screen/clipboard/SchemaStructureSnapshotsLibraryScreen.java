package io.github.drag0n1zed.dschema.screen.clipboard;

import java.util.List;
import java.util.function.Consumer;

import io.github.drag0n1zed.dschema.SchemaClient;
import io.github.drag0n1zed.universal.api.core.Player;
import io.github.drag0n1zed.universal.api.gui.AbstractPanelScreen;
import io.github.drag0n1zed.universal.api.gui.AbstractWidget;
import io.github.drag0n1zed.universal.api.gui.button.Button;
import io.github.drag0n1zed.universal.api.gui.text.TextWidget;
import io.github.drag0n1zed.universal.api.gui.tooltip.TooltipHelper;
import io.github.drag0n1zed.universal.api.platform.Entrance;
import io.github.drag0n1zed.universal.api.text.Text;
import io.github.drag0n1zed.dschema.building.clipboard.Snapshot;
import io.github.drag0n1zed.dschema.networking.packets.player.PlayerSnapshotSharePacket;
import io.github.drag0n1zed.dschema.screen.player.SchemaOnlinePlayersScreen;

public class SchemaStructureSnapshotsLibraryScreen extends AbstractPanelScreen {

    private final Consumer<Snapshot> consumer;
    private List<Snapshot> history;
    private AbstractWidget titleTextWidget;
    private StructureSnapshotList entries;

    private Button cancelButton;
    private Button selectButton;
    private Button editButton;
    private Button shareButton;
    private Button deleteButton;
    private Button exportButton;
    private Button importButton;

    public SchemaStructureSnapshotsLibraryScreen(Entrance entrance, Consumer<Snapshot> consumer) {
        super(entrance, Text.translate("dschema.structure_snapshots_library.title"), AbstractPanelScreen.PANEL_WIDTH_60, PANEL_HEIGHT_FULL);
        this.consumer = consumer;
        this.history = getEntrance().getConfigStorage().get().clipboardConfig().collections();
    }

    @Override
    protected SchemaClient getEntrance() {
        return (SchemaClient) super.getEntrance();
    }

    protected Player getPlayer() {
        return getEntrance().getClient().getPlayer();
    }

    @Override
    public void onCreate() {

        this.titleTextWidget = addWidget(new TextWidget(getEntrance(), getLeft() + getWidth() / 2, getTop() + PANEL_TITLE_HEIGHT_1 - 10, getScreenTitle().withColor(AbstractPanelScreen.TITLE_COLOR), TextWidget.Gravity.CENTER));

        this.entries = addWidget(new StructureSnapshotList(getEntrance(), getLeft() + AbstractPanelScreen.PADDINGS_H, getTop() + PANEL_TITLE_HEIGHT_1, getWidth() - AbstractPanelScreen.PADDINGS_H * 2 - 8 /* scrollbar */, getHeight() - PANEL_TITLE_HEIGHT_1 - PANEL_BUTTON_ROW_HEIGHT_2));
        this.entries.setAlwaysShowScrollbar(true);
        this.entries.reset(history);
        this.editButton = addWidget(Button.builder(getEntrance(), Text.translate("dschema.button.edit"), button -> {

        }).setBoundsGrid(getLeft(), getTop(), getWidth(), getHeight(), 1f, 4 / 5f, 1 / 5f).build());

        this.shareButton = addWidget(Button.builder(getEntrance(), Text.translate("dschema.button.share"), button -> {
            new SchemaOnlinePlayersScreen(getEntrance(), playerInfo -> {
                getEntrance().getChannel().sendPacket(new PlayerSnapshotSharePacket(getPlayer().getId(), playerInfo.getId(), entries.getSelected().getItem()));
                detach();
            }).attach();
        }).setBoundsGrid(getLeft(), getTop(), getWidth(), getHeight(), 1f, 2 / 5f, 1 / 5f).build());

        this.deleteButton = addWidget(Button.builder(getEntrance(), Text.translate("dschema.button.delete"), button -> {
            if (this.entries.hasSelected()) {
                this.entries.deleteSelected();
            }
        }).setBoundsGrid(getLeft(), getTop(), getWidth(), getHeight(), 1f, 3 / 5f, 1 / 5f).build());

        this.exportButton = addWidget(Button.builder(getEntrance(), Text.translate("dschema.button.export"), button -> {

        }).setBoundsGrid(getLeft(), getTop(), getWidth(), getHeight(), 1f, 1 / 5f, 1 / 5f).build());

        this.importButton = addWidget(Button.builder(getEntrance(), Text.translate("dschema.button.import"), button -> {
            new SchemaStructureSnapshotImportScreen(getEntrance(), snapshot -> {
                entries.insertSelected(snapshot);
            }).attach();
        }).setBoundsGrid(getLeft(), getTop(), getWidth(), getHeight(), 1f, 0 / 5f, 1 / 5f).build());

        this.cancelButton = addWidget(Button.builder(getEntrance(), Text.translate("dschema.button.done"), button -> {
            detach();
        }).setBoundsGrid(getLeft(), getTop(), getWidth(), getHeight(), 0f, 0f, 1 / 2f).build());


        this.selectButton = addWidget(Button.builder(getEntrance(), Text.translate("dschema.button.use_in_clipboard"), button -> {
            if (this.entries.hasSelected()) {
                this.consumer.accept(this.entries.getSelected().getItem());
            }
            detach();
        }).setBoundsGrid(getLeft(), getTop(), getWidth(), getHeight(), 0f, 1 / 2f, 1 / 2f).build());

    }

    @Override
    public void onReload() {
        getEntrance().getConfigStorage().update(clientConfig -> clientConfig.withClipboardConfig(clientConfig.clipboardConfig().withCollections(this.entries.items())));
        this.selectButton.setActive(this.entries.hasSelected());
        if (entries.consumeDoubleClick() && entries.hasSelected()) {
            this.consumer.accept(this.entries.getSelected().getItem());
            detach();
        }
        this.editButton.setActive(this.entries.hasSelected());

        this.shareButton.setTooltip(TooltipHelper.makeSummary(getTypeface(), Text.translate("dschema.button.share"), Text.translate("Share this structure with an online player on this sever.")));
        this.shareButton.setActive(this.entries.hasSelected());

        this.exportButton.setTooltip(TooltipHelper.makeSummary(getTypeface(), Text.translate("dschema.button.export"), Text.translate("Export this structure to your structure collections or system clipboard.")));
        this.exportButton.setActive(this.entries.hasSelected());

        this.importButton.setTooltip(TooltipHelper.makeSummary(getTypeface(), Text.translate("dschema.button.import"), Text.translate("Import a structure from your structure collections or system clipboard.")));

        this.deleteButton.setActive(this.entries.hasSelected());


//        this.exportButton.setTooltip(
//                TooltipHelper.wrapLines(getTypeface(), Text.translate("Export this structure to your system clipboard. You can paste this structure text to share with anyone else."))
//        );
//        this.importButton.setTooltip(
//                TooltipHelper.wrapLines(getTypeface(), Text.translate("Import a structure from your system clipboard. You can copy a structure text and import here."))
//        );


    }

}
