package io.github.drag0n1zed.dschema.screen.settings;

import java.util.ArrayList;

import io.github.drag0n1zed.dschema.SchemaClient;
import io.github.drag0n1zed.dschema.SchemaClientConfigStorage;
import io.github.drag0n1zed.dschema.SchemaConfigStorage;
import io.github.drag0n1zed.universal.api.gui.AbstractPanelScreen;
import io.github.drag0n1zed.universal.api.gui.button.Button;
import io.github.drag0n1zed.universal.api.gui.text.TextWidget;
import io.github.drag0n1zed.universal.api.gui.tooltip.TooltipHelper;
import io.github.drag0n1zed.universal.api.platform.Entrance;
import io.github.drag0n1zed.universal.api.text.ChatFormatting;
import io.github.drag0n1zed.universal.api.text.Text;
import io.github.drag0n1zed.dschema.networking.packets.player.PlayerPermissionCheckPacket;
import io.github.drag0n1zed.dschema.screen.builer.SchemaBuilderSettingsScreen;
import io.github.drag0n1zed.dschema.screen.general.SchemaGlobalGeneralSettingsScreen;

public class SchemaSettingsScreen extends AbstractPanelScreen {

    public SchemaSettingsScreen(Entrance entrance) {
        super(entrance, Text.translate("dschema.settings.title"), PANEL_WIDTH_42, PANEL_TITLE_HEIGHT_1 + PANEL_BUTTON_ROW_HEIGHT_3);
    }

    private Button constraintButton;
    private Button buildertButton;
//    private Button patternButton;
//    private Button clipboardButton;
//    private Button renderButton;

    @Override
    protected SchemaClient getEntrance() {
        return (SchemaClient) super.getEntrance();
    }

    @Override
    public void onCreate() {
        addWidget(new TextWidget(getEntrance(), getLeft() + getWidth() / 2, getTop() + PANEL_TITLE_HEIGHT_1 - 10, getScreenTitle().withColor(AbstractPanelScreen.TITLE_COLOR), TextWidget.Gravity.CENTER));

        this.constraintButton = addWidget(Button.builder(getEntrance(), Text.translate("dschema.general_settings.title"), button -> {
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
        }).setBoundsGrid(getLeft(), getTop(), getWidth(), getHeight(), 2f   , 0f, 1f).build());
        this.buildertButton = addWidget(Button.builder(getEntrance(), Text.translate("dschema.builder_settings.title"), button -> {
            new SchemaBuilderSettingsScreen(getEntrance()).attach();
        }).setBoundsGrid(getLeft(), getTop(), getWidth(), getHeight(), 1f, 0f, 1f).build());
//        this.renderButton = addWidget(Button.builder(getEntrance(), Text.translate("dschema.render_settings.title"), button -> {
//            new dschemaRenderSettingsScreen(getEntrance()).attach();
//        }).setBoundsGrid(getLeft(), getTop(), getWidth(), getHeight(), 1f, 0f, 1f).build());
//        this.patterntButton = addWidget(Button.builder(getEntrance(), Text.translate("dschema.pattern_settings.title"), button -> {
//            new dschemaPatternSettingsScreen(getEntrance()).attach();
//        }).setBoundsGrid(getLeft(), getTop(), getWidth(), getHeight(), 2f, 0f, 1f).build());
//        this.clipboardButton = addWidget(Button.builder(getEntrance(), Text.translate("dschema.clipboard_settings.title"), button -> {
//            new dschemaClipboardSettingsScreen(getEntrance()).attach();
//        }).setBoundsGrid(getLeft(), getTop(), getWidth(), getHeight(), 1f, 0f, 1f).build());

        addWidget(Button.builder(getEntrance(), Text.translate("dschema.button.done"), button -> {
            detach();
        }).setBoundsGrid(getLeft(), getTop(), getWidth(), getHeight(), 0f, 0f, 1f).build());

    }

    @Override
    public void onReload() {

        var constraintTooltip = new ArrayList<Text>();
        constraintTooltip.add(Text.translate("dschema.general_settings.title").withStyle(ChatFormatting.WHITE));
        constraintTooltip.add(TooltipHelper.holdShiftForSummary());
        if (TooltipHelper.isSummaryButtonDown()) {
            constraintTooltip.add(Text.empty());
            constraintTooltip.addAll(
                    TooltipHelper.wrapLines(getTypeface(), Text.translate("dschema.general_settings.tooltip", Text.text("[%s]".formatted(SchemaConfigStorage.CONFIG_NAME)).withStyle(ChatFormatting.GOLD)).withStyle(ChatFormatting.GRAY))
            );
        }
        this.constraintButton.setTooltip(constraintTooltip);

        var builderTooltip = new ArrayList<Text>();
        builderTooltip.add(Text.translate("dschema.builder_settings.title").withStyle(ChatFormatting.WHITE));
        builderTooltip.add(TooltipHelper.holdShiftForSummary());
        if (TooltipHelper.isSummaryButtonDown()) {
            builderTooltip.add(Text.empty());
            builderTooltip.addAll(
                    TooltipHelper.wrapLines(getTypeface(), Text.translate("dschema.builder_settings.tooltip", Text.text("[%s]".formatted(SchemaClientConfigStorage.CONFIG_NAME)).withStyle(ChatFormatting.GOLD)).withStyle(ChatFormatting.GRAY))
            );
        }
        this.buildertButton.setTooltip(builderTooltip);

//        var patternTooltip = new ArrayList<Text>();
//        patternTooltip.add(Text.translate("dschema.pattern_settings.title").withStyle(ChatFormatting.WHITE));
//        patternTooltip.add(TooltipHelper.holdShiftForSummary());
//        if (TooltipHelper.isSummaryButtonDown()) {
//            patternTooltip.add(Text.empty());
//            patternTooltip.addAll(
//                    TooltipHelper.wrapLines(getTypeface(), Text.translate("dschema.pattern_settings.tooltip", Text.text("[%s]".formatted(dschemaClientConfigStorage.CONFIG_NAME)).withStyle(ChatFormatting.GOLD)).withStyle(ChatFormatting.GRAY))
//            );
//        }
//        this.patterntButton.setTooltip(patternTooltip);
//
//        var renderTooltip = new ArrayList<Text>();
//        renderTooltip.add(Text.translate("dschema.render_settings.title").withStyle(ChatFormatting.WHITE));
//        renderTooltip.add(TooltipHelper.holdShiftForSummary());
//        if (TooltipHelper.isSummaryButtonDown()) {
//            renderTooltip.add(Text.empty());
//            renderTooltip.addAll(
//                    TooltipHelper.wrapLines(getTypeface(), Text.translate("dschema.render_settings.tooltip", Text.text("[%s]".formatted(dschemaClientConfigStorage.CONFIG_NAME)).withStyle(ChatFormatting.GOLD)).withStyle(ChatFormatting.GRAY))
//            );
//        }
//        this.renderButton.setTooltip(renderTooltip);
    }
}
