package io.github.drag0n1zed.schema.screen.settings;

import io.github.drag0n1zed.universal.api.gui.container.EditableEntryList;
import io.github.drag0n1zed.universal.api.gui.text.TextWidget;
import io.github.drag0n1zed.universal.api.platform.Entrance;
import io.github.drag0n1zed.universal.api.text.Text;

public final class TextList extends EditableEntryList<Text> {

    public TextList(Entrance entrance, int x, int y, int width, int height) {
        super(entrance, x, y, width, height);
        setRenderSelection(false);
        setShowScrollBar(false);
    }

    @Override
    protected EditableEntryList.Entry<Text> createHolder(Text text) {
        return new Entry(getEntrance(), this, text);
    }

    public static class Entry extends EditableEntryList.Entry<Text> {

        public Entry(Entrance entrance, TextList itemChanceList, Text text) {
            super(entrance, itemChanceList, text);
        }

        @Override
        public void onCreate() {
            addWidget(new TextWidget(getEntrance(), getX() + getWidth() / 2, getY() , getWidth(), getHeight(), getItem(), TextWidget.Gravity.CENTER));
        }

        @Override
        public int getHeight() {
            return 10;
        }
    }
}
