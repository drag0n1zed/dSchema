package io.github.drag0n1zed.dschema.screen.general;

import java.util.ArrayList;

import io.github.drag0n1zed.dschema.SchemaClient;
import io.github.drag0n1zed.dschema.SchemaConfigStorage;
import io.github.drag0n1zed.universal.api.gui.AbstractPanelScreen;
import io.github.drag0n1zed.universal.api.gui.button.Button;
import io.github.drag0n1zed.universal.api.gui.text.TextWidget;
import io.github.drag0n1zed.universal.api.gui.tooltip.TooltipHelper;
import io.github.drag0n1zed.universal.api.platform.Entrance;
import io.github.drag0n1zed.universal.api.text.ChatFormatting;
import io.github.drag0n1zed.universal.api.text.Text;
import io.github.drag0n1zed.dschema.networking.packets.player.PlayerPermissionCheckPacket;
import io.github.drag0n1zed.dschema.screen.settings.SchemaNotAnOperatorScreen;
import io.github.drag0n1zed.dschema.screen.settings.SchemaSessionStatusScreen;

public class SchemaGeneralSettingsScreen extends AbstractPanelScreen {

    public SchemaGeneralSettingsScreen(Entrance entrance) {
        super(entrance, Text.translate("dschema.general_settings.title"), PANEL_WIDTH_42, PANEL_TITLE_HEIGHT_1 + PANEL_BUTTON_ROW_HEIGHT_3);
    }

    private Button globalButton;
    private Button playerButton;

    @Override
    public void onCreate() {
        addWidget(new TextWidget(getEntrance(), getLeft() + getWidth() / 2, getTop() + PANEL_TITLE_HEIGHT_1 - 10, getScreenTitle().withColor(AbstractPanelScreen.TITLE_COLOR), TextWidget.Gravity.CENTER));

        this.globalButton = addWidget(Button.builder(getEntrance(), Text.translate("dschema.global_general_settings.title"), button -> {
            if (!getEntrance().getSessionManager().isSessionValid()) {
                getEntrance().getClient().execute(() -> {
                    new SchemaSessionStatusScreen(getEntrance()).attach();
                });
            } else {
                getEntrance().getChannel().sendPacket(new PlayerPermissionCheckPacket(getEntrance().getClient().getPlayer().getId()), (packet) -> {
                    if (packet.granted()) {
                        getEntrance().getClient().execute(() -> {
                            new SchemaGlobalGeneralSettingsScreen(getEntrance(), getEntrance().getSessionManager().getServerSessionConfigOrEmpty().getGlobalConfig(), config -> {
                                getEntrance().getSessionManager().updateGlobalConfig(config);
                            }).attach();
                        });
                    } else {
                        getEntrance().getClient().execute(() -> {
                            new SchemaNotAnOperatorScreen(getEntrance()).attach();
                        });
                    }
                });
            }

        }).setBoundsGrid(getLeft(), getTop(), getWidth(), getHeight(), 2f, 0f, 1f).build());
        this.playerButton = addWidget(Button.builder(getEntrance(), Text.translate("dschema.player_general_settings.title"), button -> {
            getEntrance().getChannel().sendPacket(new PlayerPermissionCheckPacket(getEntrance().getClient().getPlayer().getId()), (packet) -> {

                if (!getEntrance().getSessionManager().isSessionValid()) {
                    getEntrance().getClient().execute(() -> {
                        new SchemaSessionStatusScreen(getEntrance()).attach();
                    });
                } else {
                    if (packet.granted()) {
                        getEntrance().getClient().execute(() -> {
                            new SchemaPlayerGeneralSettingsListScreen(getEntrance(), getEntrance().getSessionManager().getServerSessionConfigOrEmpty().playerConfigs(), playerConfigs -> {
                                getEntrance().getSessionManager().updatePlayerConfig(playerConfigs);
                            }).attach();
                        });
                    } else {
                        getEntrance().getClient().execute(() -> {
                            new SchemaNotAnOperatorScreen(getEntrance()).attach();
                        });
                    }
                }
            });
        }).setBoundsGrid(getLeft(), getTop(), getWidth(), getHeight(), 1f, 0f, 1f).build());

        addWidget(Button.builder(getEntrance(), Text.translate("dschema.button.done"), button -> {
            detach();
        }).setBoundsGrid(getLeft(), getTop(), getWidth(), getHeight(), 0f, 0f, 1f).build());

    }

    @Override
    protected SchemaClient getEntrance() {
        return (SchemaClient) super.getEntrance();
    }

    @Override
    public void onReload() {
        var globalTooltip = new ArrayList<Text>();
        globalTooltip.add(Text.translate("dschema.global_general_settings.title").withStyle(ChatFormatting.WHITE));
        globalTooltip.add(TooltipHelper.holdShiftForSummary());
        if (TooltipHelper.isSummaryButtonDown()) {
            globalTooltip.add(Text.empty());
            globalTooltip.addAll(
                    TooltipHelper.wrapLines(getTypeface(), Text.translate("dschema.global_general_settings.tooltip", Text.text("[%s]".formatted(SchemaConfigStorage.CONFIG_NAME)).withStyle(ChatFormatting.GOLD)).withStyle(ChatFormatting.GRAY))
            );
        }
        this.globalButton.setTooltip(globalTooltip);


        var playerTooltip = new ArrayList<Text>();
        playerTooltip.add(Text.translate("dschema.player_general_settings.title").withStyle(ChatFormatting.WHITE));
        playerTooltip.add(TooltipHelper.holdShiftForSummary());
        if (TooltipHelper.isSummaryButtonDown()) {
            playerTooltip.add(Text.empty());
            playerTooltip.addAll(
                    TooltipHelper.wrapLines(getTypeface(), Text.translate("dschema.player_general_settings.tooltip", Text.text("[%s]".formatted(SchemaConfigStorage.CONFIG_NAME)).withStyle(ChatFormatting.GOLD)).withStyle(ChatFormatting.GRAY))
            );
        }
        this.playerButton.setTooltip(playerTooltip);
    }
}
