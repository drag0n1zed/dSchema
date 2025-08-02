package io.github.drag0n1zed.dschema.screen.test;

import java.util.logging.Logger;

import io.github.drag0n1zed.dschema.SchemaClient;
import io.github.drag0n1zed.universal.api.gui.AbstractPanelScreen;
import io.github.drag0n1zed.universal.api.gui.button.Button;
import io.github.drag0n1zed.universal.api.gui.container.SimpleEntryList;
import io.github.drag0n1zed.universal.api.gui.input.EditBox;
import io.github.drag0n1zed.universal.api.gui.text.TextWidget;
import io.github.drag0n1zed.universal.api.platform.Entrance;
import io.github.drag0n1zed.universal.api.text.Text;
import io.github.drag0n1zed.dschema.building.config.ClientConfig;
import io.github.drag0n1zed.dschema.building.config.ClipboardConfig;
import io.github.drag0n1zed.dschema.building.config.PatternConfig;
import io.github.drag0n1zed.dschema.building.config.RenderConfig;
import io.github.drag0n1zed.dschema.screen.general.SchemaGeneralSettingsScreen;
import io.github.drag0n1zed.dschema.screen.pattern.SchemaPatternScreen;
import io.github.drag0n1zed.dschema.screen.player.SchemaOnlinePlayersScreen;
import io.github.drag0n1zed.dschema.screen.settings.SchemaSettingsScreen;

public class SchemaTestScreen extends AbstractPanelScreen {

    public SchemaTestScreen(Entrance entrance) {
        super(entrance, Text.text("Test"), PANEL_WIDTH_60, PANEL_HEIGHT_FULL);
    }

    @Override
    public void onCreate() {

        addWidget(new TextWidget(getEntrance(), getLeft() + getWidth() / 2, getTop() + PANEL_TITLE_HEIGHT_1 - 10, getScreenTitle().withColor(AbstractPanelScreen.TITLE_COLOR), TextWidget.Gravity.CENTER));

        addWidget(Button.builder(getEntrance(), Text.translate("dschema.test.cancel"), button -> {
            detach();
        }).setBoundsGrid(getLeft(), getTop(), getWidth(), getHeight(), 0f, 0f, 1f).build());

        var entries = addWidget(new SimpleEntryList(getEntrance(), getLeft() + PADDINGS_H, getTop() + PANEL_TITLE_HEIGHT_1, getWidth() - PADDINGS_H * 2, getHeight() - PANEL_TITLE_HEIGHT_1 - PANEL_BUTTON_ROW_HEIGHT_1));

        entries.addSimpleEntry(entry -> {
            var editBox = entry.addWidget(new EditBox(getEntrance(), entry.getLeft(), entry.getY(), entry.getWidth() - 64, 20, Text.empty()));
            entry.addWidget(new Button(getEntrance(), entry.getRight() - 64, entry.getTop(), 64, 20, Text.text("Execute"), button -> {
                getEntrance().getClient().sendCommand(editBox.getValue());
            }));
        });
        entries.addSimpleEntry(entry -> {
            entry.addWidget(new Button(getEntrance(), entry.getLeft(), entry.getTop(), entry.getWidth() / 2, 20, Text.text("Load Toml Config"), button -> {
                Logger.getAnonymousLogger().info("" + getEntrance().getConfigStorage().get());
            }));
            entry.addWidget(new Button(getEntrance(), entry.getLeft() + entry.getWidth() / 2, entry.getTop(), entry.getWidth() / 2, 20, Text.text("Save Toml Config"), button -> {
                getEntrance().getConfigStorage().set(
                        new ClientConfig(
                                new RenderConfig(),
                                new PatternConfig(),
                                new ClipboardConfig()
                        )
                );
            }));
        });
        entries.addSimpleEntry(entry -> {
            entry.addWidget(new Button(getEntrance(), entry.getLeft(), entry.getTop(), entry.getWidth(), 20, Text.text("Open dschemaSettingsScreen"), button -> {
                new SchemaSettingsScreen(getEntrance()).attach();
            }));
            entry.addWidget(new Button(getEntrance(), entry.getLeft(), entry.getTop() + 20, entry.getWidth(), 20, Text.text("Open dschemaConstraintSettingsScreen"), button -> {
                new SchemaGeneralSettingsScreen(getEntrance()).attach();
            }));
            entry.addWidget(new Button(getEntrance(), entry.getLeft(), entry.getTop() + 80, entry.getWidth(), 20, Text.text("Open dschemaOnlinePlayersScreen"), button -> {
                new SchemaOnlinePlayersScreen(getEntrance(), playerInfo -> {

                }).attach();
            }));
        });
        entries.addSimpleEntry(entry -> {
            entry.addWidget(new Button(getEntrance(), entry.getLeft(), entry.getTop(), entry.getWidth(), 20, Text.text("Open dschemaPatternScreen"), button -> {
                new SchemaPatternScreen(getEntrance()).attach();
            }));
        });
    }

    @Override
    protected SchemaClient getEntrance() {
        return (SchemaClient) super.getEntrance();
    }
}
