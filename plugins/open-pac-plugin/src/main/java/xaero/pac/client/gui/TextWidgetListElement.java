package xaero.pac.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import xaero.pac.client.gui.widget.TextWidgetEditBox;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public final class TextWidgetListElement extends SimpleValueWidgetListElement<String, TextWidgetListElement> {

	private final Component title;
	private EditBox editBox;
	private Button confirmButton;
	private Button cancelButton;
	private String confirmedText;
	private final BiConsumer<TextWidgetListElement, String> valueConfirmResponder;
	private final Predicate<String> validator;

	private TextWidgetListElement(int w, int h, boolean mutable, Component title, BiFunction<TextWidgetListElement, Vec3i, AbstractWidget> widgetSupplier, List<FormattedCharSequence> tooltip, BiConsumer<TextWidgetListElement, String> valueConfirmResponder, Predicate<String> validator, String startValue) {
		super(startValue, w, h, mutable, widgetSupplier, tooltip);
		this.confirmedText = startValue;
		this.title = title;
		this.valueConfirmResponder = valueConfirmResponder;
		this.validator = validator;
	}

	@Override
	public AbstractWidget screenInit(int x, int y, WidgetListScreen screen, List<EditBox> tickableBoxes) {
		editBox = (EditBox) super.screenInit(x, y, screen, tickableBoxes);
		screen.addRenderableWidget(confirmButton = Button.builder(Component.literal("✔"), this::onConfirmButton).bounds(x + w - 40, y, 20, 20).build());
		screen.addRenderableWidget(cancelButton = Button.builder(Component.literal("❌"), this::onCancelButton).bounds(x + w - 20, y, 20, 20).build());
		confirmButton.active = !confirmedText.equals(draftValue);
		cancelButton.active = !confirmedText.equals(draftValue);
		return editBox;
	}

	public boolean onEnterPressed() {
		if(confirmButton.active) {
			confirmButton.onPress();
			return true;
		}
		return false;
	}

	private boolean updateBoxColor() {
		boolean validInput = validator.test(draftValue);
		editBox.setTextColor(validInput ? 14737632/*copied from editbox class*/ : 0xFFFF5555);
		return validInput;
	}

	private void onTextTyped(String value) {
		if(!draftValue.equals(value)) {
			draftValue = value;
			confirmButton.active = updateBoxColor();
			cancelButton.active = true;
		}
	}

	private void onConfirmButton(Button b) {
		if(validator.test(draftValue)) {
			valueConfirmResponder.accept(this, draftValue);
			confirmedText = draftValue;
			confirmButton.active = false;
			cancelButton.active = false;
		}
	}

	public void clearBox(){
		editBox.setValue("");
	}

	private void onCancelButton(Button b) {
		editBox.setValue(draftValue = confirmedText);
		confirmButton.active = false;
		cancelButton.active = false;
		updateBoxColor();
	}

	@Override
	public final void render(GuiGraphics guiGraphics) {
		super.render(guiGraphics);
		guiGraphics.drawString(Minecraft.getInstance().font, title, x + 2, y + 6, mutable ? -1 : 14737632/*copied from editbox class*/);
	}

	public static final class Builder extends SimpleValueWidgetListElement.Builder<String, TextWidgetListElement, Builder> {

		private Component title;
		private BiConsumer<TextWidgetListElement, String> responder;
		private Predicate<String> filter;
		private Predicate<String> validator;
		private int boxWidth;
		private int maxLength;

		@Override
		public Builder setDefault() {
			super.setDefault();
			setBoxWidth(50);
			setTitle(null);
			setResponder(null);
			setFilter(Objects::nonNull);
			setValidator(Objects::nonNull);
			setMaxLength(32);
			return this;
		}

		public Builder setMaxLength(int maxLength) {
			this.maxLength = maxLength;
			return this;
		}

		public Builder setBoxWidth(int boxWidth) {
			this.boxWidth = boxWidth;
			return this;
		}

		public Builder setTitle(Component title) {
			this.title = title;
			return this;
		}

		public Builder setResponder(BiConsumer<TextWidgetListElement, String> responder) {
			this.responder = responder;
			return this;
		}

		public Builder setFilter(Predicate<String> filter) {
			this.filter = filter;
			return this;
		}

		public Builder setValidator(Predicate<String> validator) {
			this.validator = validator;
			return this;
		}

		public TextWidgetListElement build() {
			if(title == null || filter == null)
				throw new IllegalStateException();
			return (TextWidgetListElement) super.build();
		}

		@Override
		protected TextWidgetListElement buildInternal() {
			BiFunction<TextWidgetListElement, Vec3i, AbstractWidget> widgetSupplier = (el, xy) -> {
				TextWidgetEditBox box = new TextWidgetEditBox(el, Minecraft.getInstance().font, xy.getX() + w - boxWidth - 42, xy.getY(), boxWidth, h, title);
				box.setMaxLength(maxLength);
				box.setValue(el.getDraftValue());
				box.setResponder(s -> el.onTextTyped(s));
				box.setFilter(filter);
				return box;
			};
			return new TextWidgetListElement(w, h, mutable, title, widgetSupplier, tooltip, responder, validator, startValue);
		}

		public static Builder begin() {
			return new Builder().setDefault();
		}

	}

}
