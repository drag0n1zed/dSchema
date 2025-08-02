package io.github.drag0n1zed.dschema.screen.pattern;

import java.util.List;
import java.util.function.Consumer;

import io.github.drag0n1zed.dschema.SchemaClient;
import io.github.drag0n1zed.universal.api.gui.AbstractPanelScreen;
import io.github.drag0n1zed.universal.api.gui.button.Button;
import io.github.drag0n1zed.universal.api.gui.text.TextWidget;
import io.github.drag0n1zed.universal.api.platform.Entrance;
import io.github.drag0n1zed.universal.api.text.Text;
import io.github.drag0n1zed.dschema.building.config.PatternConfig;
import io.github.drag0n1zed.dschema.screen.settings.SettingOptionsList;
import io.github.drag0n1zed.dschema.screen.transformer.SchemaItemRandomizerPresetsScreen;

public class SchemaPatternSettingsScreen extends AbstractPanelScreen {

    private final Consumer<PatternConfig> consumer;
    private PatternConfig originalConfig;
    private PatternConfig config;

    public SchemaPatternSettingsScreen(Entrance entrance) {
        super(entrance, Text.translate("dschema.pattern_settings.title"), PANEL_WIDTH_60, PANEL_HEIGHT_FULL);
        this.consumer = newConfig -> {
            getEntrance().getConfigStorage().update(config -> config.withPatternConfig(newConfig));
        };
        this.config = getEntrance().getConfigStorage().get().patternConfig();
        this.originalConfig = config;
    }

    @Override
    public void onCreate() {
        addWidget(new TextWidget(getEntrance(), getLeft() + getWidth() / 2, getTop() + PANEL_TITLE_HEIGHT_1 - 10, getScreenTitle().withColor(AbstractPanelScreen.TITLE_COLOR), TextWidget.Gravity.CENTER));


        var entries = addWidget(new SettingOptionsList(getEntrance(), getLeft() + PADDINGS_H, getTop() + PANEL_TITLE_HEIGHT_1, getWidth() - PADDINGS_H * 2 - 8, getHeight() - PANEL_TITLE_HEIGHT_1 - PANEL_BUTTON_ROW_HEIGHT_1, false, false));

        entries.addTab(Text.translate("dschema.pattern_settings.item_randomizer_presets"), null, config.transformerPreset(), (value) -> {
            this.config = new PatternConfig(value);
        }, (entry, value) -> {
            entry.getButton().setOnPressListener(button1 -> {
                new SchemaItemRandomizerPresetsScreen(getEntrance(), transformers -> {
                    entry.setItem((List) transformers);
                }).attach();
            });
            entry.getButton().setMessage(Text.translate("dschema.pattern_settings.presets", value.size()));
        });

        addWidget(Button.builder(getEntrance(), Text.translate("dschema.button.cancel"), button -> {
            detach();
        }).setBoundsGrid(getLeft(), getTop(), getWidth(), getHeight(), 0f, 0f, 0.5f).build());

        addWidget(Button.builder(getEntrance(), Text.translate("dschema.button.save"), button -> {
            consumer.accept(config);
            detachAll();
        }).setBoundsGrid(getLeft(), getTop(), getWidth(), getHeight(), 0f, 0.5f, 0.5f).build());

    }

    @Override
    protected SchemaClient getEntrance() {
        return (SchemaClient) super.getEntrance();
    }
}
