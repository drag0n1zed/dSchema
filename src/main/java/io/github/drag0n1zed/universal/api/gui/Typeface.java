package io.github.drag0n1zed.universal.api.gui;

import io.github.drag0n1zed.universal.api.platform.PlatformReference;
import io.github.drag0n1zed.universal.api.text.Text;

public interface Typeface extends PlatformReference {

    int measureHeight(Text text);

    int measureWidth(Text text);

    int measureHeight(String text);

    int measureWidth(String text);

    int getLineHeight();

    String subtractByWidth(String text, int width, boolean tail);

}
