package io.github.drag0n1zed.schema.screen.settings;

import java.util.List;
import java.util.stream.Stream;

import io.github.drag0n1zed.universal.api.gui.AbstractPanelScreen;
import io.github.drag0n1zed.universal.api.gui.Dimens;
import io.github.drag0n1zed.universal.api.gui.button.Button;
import io.github.drag0n1zed.universal.api.gui.text.TextWidget;
import io.github.drag0n1zed.universal.api.gui.tooltip.TooltipHelper;
import io.github.drag0n1zed.universal.api.platform.Entrance;
import io.github.drag0n1zed.universal.api.text.Text;

public class EffortlessNotAnOperatorScreen extends AbstractPanelScreen {

    public EffortlessNotAnOperatorScreen(Entrance entrance) {
        super(entrance, Text.empty(), PANEL_WIDTH_60, 0);
    }

    @Override
    public void onCreate() {
        addWidget(new TextWidget(getEntrance(), getLeft() + getWidth() / 2, getTop() + PANEL_TITLE_HEIGHT_1 - 10, Text.translate("effortless.not_an_operator.title").withColor(AbstractPanelScreen.TITLE_COLOR), TextWidget.Gravity.CENTER));

        var entries = addWidget(new TextList(getEntrance(), getLeft() + PADDINGS_H, getTop() + PANEL_TITLE_HEIGHT_1, getWidth() - PADDINGS_H * 2, getHeight() - PANEL_TITLE_HEIGHT_1 - PANEL_BUTTON_ROW_HEIGHT_1));
        entries.reset(Stream.of(List.of(Text.empty()), getMessages(), List.of(Text.empty())).flatMap(List::stream).toList());

        addWidget(Button.builder(getEntrance(), Text.translate("effortless.button.done"), button -> {
            detach();
        }).setBoundsGrid(getLeft(), getTop(), getWidth(), getHeight(), 0f, 0f, 1f).build());

    }

    private List<Text> getMessages() {
        return TooltipHelper.wrapLines(getTypeface(), Text.translate("effortless.not_an_operator.message"), getWidth() - PADDINGS_H * 2);
    }

    @Override
    public int getHeight() {
        return PANEL_TITLE_HEIGHT_1 + (getMessages().size() + 2) * 10 + 4 + Dimens.Screen.BUTTON_ROW_1;
    }

}
