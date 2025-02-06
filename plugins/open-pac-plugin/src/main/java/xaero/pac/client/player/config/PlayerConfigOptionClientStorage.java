package xaero.pac.client.player.config;

import xaero.pac.client.player.config.api.IPlayerConfigClientStorageAPI;
import xaero.pac.common.server.player.config.PlayerConfigOptionSpec;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.BiPredicate;

public class PlayerConfigOptionClientStorage<T extends Comparable<T>> implements IPlayerConfigOptionClientStorage<T> {

	protected final PlayerConfigOptionSpec<T> option;
	private T value;
	private boolean defaulted;
	private boolean mutable;

	public PlayerConfigOptionClientStorage(PlayerConfigOptionSpec<T> option, T value) {
		super();
		if(option == null)
			throw new IllegalArgumentException();
		this.option = option;
		this.value = value;
		this.defaulted = true;
	}

	@SuppressWarnings("unchecked")
	public static <T extends Comparable<T>> PlayerConfigOptionClientStorage<T> createCast(PlayerConfigOptionSpec<T> option, Object value){
		return new PlayerConfigOptionClientStorage<>(option, (T)value);
	}

	@Nonnull
	@Override
	public PlayerConfigOptionSpec<T> getOption() {
		return option;
	}

	@Nonnull
	@Override
	public String getId() {
		return option.getId();
	}

	@Nonnull
	@Override
	public String getComment() {
		return option.getComment();
	}

	@Nonnull
	@Override
	public String getTranslation() {
		return option.getTranslation();
	}

	@Nonnull
	@Override
	public Object[] getTranslationArgs(){
		return option.getTranslationArgs();
	}

	@Nonnull
	@Override
	public String getCommentTranslation() {
		return option.getCommentTranslation();
	}

	@Nonnull
	@Override
	public Object[] getCommentTranslationArgs(){
		return option.getCommentTranslationArgs();
	}

	@Nonnull
	@Override
	public Class<T> getType() {
		return option.getType();
	}

	@Nullable
	@Override
	public T getValue() {
		return value;
	}

	@Nonnull
	@Override
	public BiPredicate<IPlayerConfigClientStorageAPI, T> getValidator(){
		return option.getClientSideValidator();
	}

	@Nullable
	@Override
	public String getTooltipPrefix() {
		return option.getTooltipPrefix();
	}

	@Override
	public void setValue(T value) {
		this.value = value;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void setCastValue(Object value) {
		if(value != null && getType() != value.getClass())
			throw new IllegalArgumentException();
		setValue((T)value);
	}

	@Override
	public void setDefaulted(boolean defaulted) {
		this.defaulted = defaulted;
	}

	@Override
	public boolean isDefaulted() {
		return defaulted;
	}

	@Override
	public void setMutable(boolean mutable) {
		this.mutable = mutable;
	}

	@Override
	public boolean isMutable() {
		return mutable;
	}

	public boolean isDynamic() {
		return option.isDynamic();
	}

	public static abstract class Builder<T extends Comparable<T>, B extends Builder<T, B>> {

		protected final B self;
		protected PlayerConfigOptionSpec<T> option;
		protected T value;

		@SuppressWarnings("unchecked")
		protected Builder() {
			self = (B)this;
		}

		public B setDefault(){
			setOption(null);
			setValue(null);
			return self;
		}

		public B setOption(PlayerConfigOptionSpec<T> option) {
			this.option = option;
			return self;
		}

		public B setValue(T value) {
			this.value = value;
			return self;
		}

		public PlayerConfigOptionClientStorage<T> build(){
			if(option == null)
				throw new IllegalStateException();
			return buildInternally();
		}

		protected abstract PlayerConfigOptionClientStorage<T> buildInternally();

	}

}
