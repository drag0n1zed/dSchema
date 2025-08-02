package io.github.drag0n1zed.universal.api.gui.slot;

import io.github.drag0n1zed.universal.api.core.ItemStack;
import io.github.drag0n1zed.universal.api.text.Text;

public sealed interface SlotData permits SlotData.TextSymbol, SlotData.ItemStackSymbol {

    record TextSymbol(
            Text text, Text symbol
    ) implements SlotData {
        public TextSymbol(String text, String symbol) {
            this(Text.text(text), Text.text(symbol));
        }

        public TextSymbol(String text, Text symbol) {
            this(Text.text(text), symbol);
        }

        public TextSymbol(Text text, String symbol) {
            this(text, Text.text(symbol));
        }
    }

    record ItemStackSymbol(
            ItemStack itemStack, Text symbol
    ) implements SlotData {
    }
}
