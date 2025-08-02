package io.github.drag0n1zed.dschema.screen.transformer;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import io.github.drag0n1zed.dschema.SchemaClient;
import io.github.drag0n1zed.universal.api.gui.AbstractPanelScreen;
import io.github.drag0n1zed.universal.api.gui.button.Button;
import io.github.drag0n1zed.universal.api.gui.text.TextWidget;
import io.github.drag0n1zed.universal.api.gui.tooltip.TooltipHelper;
import io.github.drag0n1zed.universal.api.platform.Entrance;
import io.github.drag0n1zed.universal.api.text.ChatFormatting;
import io.github.drag0n1zed.universal.api.text.Text;
import io.github.drag0n1zed.dschema.building.config.PatternConfig;
import io.github.drag0n1zed.dschema.building.pattern.Transformer;
import io.github.drag0n1zed.dschema.building.pattern.randomize.ItemRandomizer;

public class SchemaItemRandomizerPresetsScreen extends AbstractPanelScreen {

    private Consumer<List<? extends Transformer>> consumer;
    private List<? extends Transformer> config;
    private TransformerList entries;
    private TextWidget titleTextWidget;
    private Button editButton;
    private Button deleteButton;
    private Button clearButton;
    private Button addButton;
    private Button cancelButton;
    private Button saveButton;

    public SchemaItemRandomizerPresetsScreen(Entrance entrance) {
        super(entrance, Text.translate("dschema.transformer_presets.title").withStyle(ChatFormatting.DARK_GRAY), PANEL_WIDTH_60, PANEL_HEIGHT_FULL);
        this.consumer = transformers -> {
            getEntrance().getConfigStorage().update(config -> config.withPatternConfig(new PatternConfig(transformers)));
        };
        this.config = getEntrance().getConfigStorage().get().patternConfig().itemRandomizers();
    }

    public SchemaItemRandomizerPresetsScreen(Entrance entrance, Consumer<List<? extends Transformer>> consumer) {
        this(entrance);
        this.consumer = consumer;
    }

    @Override
    protected SchemaClient getEntrance() {
        return (SchemaClient) super.getEntrance();
    }

    @Override
    public void onCreate() {

        this.titleTextWidget = addWidget(new TextWidget(getEntrance(), getLeft() + getWidth() / 2, getTop() + PANEL_TITLE_HEIGHT_1 - 10, getScreenTitle().withColor(AbstractPanelScreen.TITLE_COLOR), TextWidget.Gravity.CENTER));

        this.editButton = addWidget(Button.builder(getEntrance(), Text.translate("dschema.button.edit"), button -> {
            if (entries.getSelected() != null && !entries.getSelected().getItem().isBuiltIn()) {
                editTransformer((ItemRandomizer) entries.getSelected().getItem());
            }
        }).setBoundsGrid(getLeft(), getTop(), getWidth(), getHeight(), 1f, 0f, 0.25f).build());

        this.deleteButton = addWidget(Button.builder(getEntrance(), Text.translate("dschema.button.delete"), button -> {
            if (entries.getSelected() != null && !entries.getSelected().getItem().isBuiltIn()) {
                entries.deleteSelected();
            }
        }).setBoundsGrid(getLeft(), getTop(), getWidth(), getHeight(), 1f, 0.25f, 0.25f).build());


        this.clearButton = addWidget(Button.builder(getEntrance(), Text.translate("dschema.button.clear"), button -> {
            this.entries.reset(this.entries.items().stream().filter(transformer1 -> transformer1.isBuiltIn()).toList());
        }).setBoundsGrid(getLeft(), getTop(), getWidth(), getHeight(), 1f, 0.5f, 0.25f).build());


        this.addButton = addWidget(Button.builder(getEntrance(), Text.translate("dschema.button.add"), button -> {
            editTransformer(ItemRandomizer.EMPTY.withName(Text.empty()).withRandomId());
        }).setBoundsGrid(getLeft(), getTop(), getWidth(), getHeight(), 1f, 0.75f, 0.25f).build());

        this.cancelButton = addWidget(Button.builder(getEntrance(), Text.translate("dschema.button.cancel"), button -> {
            detach();
        }).setBoundsGrid(getLeft(), getTop(), getWidth(), getHeight(), 0f, 0f, 0.5f).build());
        this.saveButton = addWidget(Button.builder(getEntrance(), Text.translate("dschema.button.save"), button -> {
            detach();
            consumer.accept(config);

        }).setBoundsGrid(getLeft(), getTop(), getWidth(), getHeight(), 0f, 0.5f, 0.5f).build());


        this.entries = addWidget(new TransformerList(getEntrance(), getLeft() + AbstractPanelScreen.PADDINGS_H, getTop() + PANEL_TITLE_HEIGHT_1, getWidth() - AbstractPanelScreen.PADDINGS_H * 2 - 8 /* scrollbar */, getHeight() - PANEL_TITLE_HEIGHT_1 - PANEL_BUTTON_ROW_HEIGHT_2));
        this.entries.setAlwaysShowScrollbar(true);
        this.entries.reset(this.config);

    }

    @Override
    public void onReload() {
        this.editButton.setActive(entries.getSelected() != null && !entries.getSelected().getItem().isBuiltIn());
        this.deleteButton.setActive(entries.getSelected() != null && !entries.getSelected().getItem().isBuiltIn());

        if (entries.getSelected() != null && entries.getSelected().getItem().isBuiltIn()) {
            this.editButton.setTooltip(
                    Stream.concat(
                            Stream.of(Text.translate("dschema.transformer.edit.tooltip.cannot_edit_built_in.title")),
                            TooltipHelper.wrapLines(getTypeface(), Text.translate("dschema.transformer.edit.tooltip.cannot_edit_built_in.message").withStyle(ChatFormatting.GRAY)).stream()
                    ).toList()
            );
        } else {
            this.editButton.clearTooltip();
        }
        if (entries.getSelected() != null && entries.getSelected().getItem().isBuiltIn()) {
            this.deleteButton.setTooltip(
                    Stream.concat(
                            Stream.of(Text.translate("dschema.transformer.delete.tooltip.cannot_delete_built_in.title")),
                            TooltipHelper.wrapLines(getTypeface(), Text.translate("dschema.transformer.delete.tooltip.cannot_delete_built_in.message").withStyle(ChatFormatting.GRAY)).stream()
                    ).toList()
            );
        } else {
            this.deleteButton.clearTooltip();
        }
        this.addButton.setTooltip(
                Stream.concat(
                        Stream.of(Text.translate("dschema.transformer.add.tooltip.random.title")),
                        TooltipHelper.wrapLines(getTypeface(), Text.translate("dschema.transformer.add.tooltip.random.message").withStyle(ChatFormatting.GRAY)).stream()
                ).toList()
        );

        if (entries.consumeDoubleClick() && entries.hasSelected()) {
            if (!entries.getSelected().getItem().isBuiltIn()) {
                editTransformer((ItemRandomizer) entries.getSelected().getItem());
            }
        }
        this.config = entries.items();

    }

    private void editTransformer(ItemRandomizer transformer) {
        new SchemaItemRandomizerEditScreen(
                getEntrance(),
                result -> {
                    if (entries.hasSelected()) {
                        entries.replaceSelect(result.withName(Text.empty()));
                    } else {
                        entries.insertSelected(result.withName(Text.empty()));
                    }
                    onReload();
                },
                transformer
        ).attach();
    }

}

