package io.github.drag0n1zed.dschema.screen.pattern;

import java.util.stream.Collectors;

import io.github.drag0n1zed.universal.api.gui.Dimens;
import io.github.drag0n1zed.universal.api.gui.container.EditableEntryList;
import io.github.drag0n1zed.universal.api.gui.slot.SlotContainer;
import io.github.drag0n1zed.universal.api.gui.slot.SlotData;
import io.github.drag0n1zed.universal.api.gui.text.TextWidget;
import io.github.drag0n1zed.universal.api.platform.Entrance;
import io.github.drag0n1zed.universal.api.text.ChatFormatting;
import io.github.drag0n1zed.universal.api.text.Text;
import io.github.drag0n1zed.dschema.building.pattern.Pattern;
import io.github.drag0n1zed.dschema.screen.transformer.TransformerList;

public final class PatternList extends EditableEntryList<Pattern> {

    public PatternList(Entrance entrance, int x, int y, int width, int height) {
        super(entrance, x, y, width, height);
    }

    @Override
    protected Entry createHolder(Pattern item) {
        return new Entry(getEntrance(), item);
    }

    public static class Entry extends EditableEntryList.Entry<Pattern> {

        private RadialTextIcon radialTextIcon;
        private TextWidget nameTextWidget;
        private SlotContainer slotContainer;

        protected Entry(Entrance entrance, Pattern pattern) {
            super(entrance, pattern);
        }

        @Override
        public void onCreate() {
            this.radialTextIcon = addWidget(new RadialTextIcon(getEntrance(), getX(), getY(), Dimens.ICON_WIDTH, Dimens.ICON_HEIGHT, 0, Text.empty()));
            this.nameTextWidget = addWidget(new TextWidget(getEntrance(), getX() + Dimens.ICON_WIDTH + 2, getY() + 2, getDisplayName(getItem())));
            this.slotContainer = addWidget(new SlotContainer(getEntrance(), getX() + Dimens.ICON_WIDTH + 2, getY() + 12, 0, 0));
        }

        @Override
        public void onPositionChange(int from, int to) {
            radialTextIcon.setIndex(to);
            radialTextIcon.setMessage(Text.text(String.valueOf(to + 1)));
        }

        @Override
        public void onReload() {
            nameTextWidget.setMessage(getDisplayName(getItem()));
            slotContainer.setEntries(getItem().transformers().stream().map(transformer -> new SlotData.TextSymbol(TransformerList.Entry.getSymbol(transformer), Text.empty())).collect(Collectors.toList()));
        }

        @Override
        public Text getNarration() {
            return Text.translate("narrator.select", Text.empty());
        }

        @Override
        public int getHeight() {
            return Dimens.ICON_HEIGHT + 4;
        }

        private Text getDisplayName(Pattern pattern) {
            return Text.translate("dschema.pattern.no_name").withStyle(ChatFormatting.GRAY);
        }


    }
}
