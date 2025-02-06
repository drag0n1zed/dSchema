package xaero.pac.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.Vec3i;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;
import java.util.function.BiFunction;

public abstract class WidgetListElement<E extends WidgetListElement<E>> {

	protected final E self;
	private final BiFunction<E, Vec3i, AbstractWidget> widgetSupplier;
	private final List<FormattedCharSequence> tooltip;
	protected int x;
	protected int y;
	protected final int w;
	protected final int h;
	protected final boolean mutable;

	@SuppressWarnings("unchecked")
	protected WidgetListElement(int w, int h, boolean mutable, BiFunction<E, Vec3i, AbstractWidget> widgetSupplier, List<FormattedCharSequence> tooltip) {
		super();
		this.self = (E) this;
		this.w = w;
		this.h = h;
		this.mutable = mutable;
		this.widgetSupplier = widgetSupplier;
		this.tooltip = tooltip;
	}

	public AbstractWidget screenInit(int x, int y, WidgetListScreen screen, List<EditBox> tickableBoxes) {
		this.x = x;
		this.y = y;
		AbstractWidget widget = widgetSupplier.apply(self, new Vec3i(x, y, 0));
		screen.addRenderableWidget(widget);
		widget.active = mutable;
		if(widget instanceof EditBox) {
			tickableBoxes.add((EditBox) widget);
			((EditBox) widget).setEditable(mutable);
		}
		return widget;
	}

	public boolean isOver(int mouseX, int mouseY) {
		return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
	}

	public List<FormattedCharSequence> getTooltip() {
		return tooltip;
	}

	public void render(GuiGraphics guiGraphics) {
	}

	public static abstract class Builder<E extends WidgetListElement<E>, B extends Builder<E, B>> {

		protected final B self;
		protected BiFunction<E, Vec3i, AbstractWidget> widgetSupplier;
		protected int w;
		protected int h;
		protected List<FormattedCharSequence> tooltip;
		protected boolean mutable;

		@SuppressWarnings("unchecked")
		public Builder() {
			super();
			this.self = (B) this;
		}

		public B setDefault() {
			setW(0);
			setH(0);
			setTooltip(null);
			setMutable(true);
			setWidgetSupplier(null);
			return self;
		}

		public B setWidgetSupplier(BiFunction<E, Vec3i, AbstractWidget> widgetSupplier) {
			this.widgetSupplier = widgetSupplier;
			return self;
		}

		public B setW(int w) {
			this.w = w;
			return self;
		}

		public B setH(int h) {
			this.h = h;
			return self;
		}

		public B setTooltip(List<FormattedCharSequence> tooltip) {
			this.tooltip = tooltip;
			return self;
		}

		public B setMutable(boolean mutable) {
			this.mutable = mutable;
			return self;
		}

		public E build() {
			return buildInternal();
		}

		protected abstract E buildInternal();

	}

}
