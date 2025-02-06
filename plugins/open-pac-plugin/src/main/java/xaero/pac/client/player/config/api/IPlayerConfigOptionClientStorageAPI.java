package xaero.pac.client.player.config.api;

import xaero.pac.common.server.player.config.api.IPlayerConfigOptionSpecAPI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.BiPredicate;

/**
 * API for a player config option value storage on the client side
 */
public interface IPlayerConfigOptionClientStorageAPI<T extends Comparable<T>> {

	/**
	 * Gets the option spec that this storage holds the value for.
	 *
	 * @return the option spec of this storage, not null
	 */
	@Nonnull
	public IPlayerConfigOptionSpecAPI<T> getOption();

	/**
	 * Gets the option string ID.
	 *
	 * @return the option string ID, not null
	 */
	@Nonnull
	public String getId();

	/**
	 * Gets the default comment text for the option.
	 *
	 * @return the default comment text, not null
	 */
	@Nonnull
	public String getComment();

	/**
	 * Gets the translation key for the name of the option.
	 *
	 * @return translation key for the name of the option, not null
	 */
	@Nonnull
	public String getTranslation();

	/**
	 * Gets the translation key arguments for the name of this option.
	 *
	 * @return the translation key arguments, not null
	 */
	public Object[] getTranslationArgs();

	/**
	 * Gets the translation key for the comment of this option.
	 *
	 * @return the comment translation key, not null
	 */
	@Nonnull
	public String getCommentTranslation();

	/**
	 * Gets the translation key arguments for the comment of this option.
	 *
	 * @return the comment translation key arguments, not null
	 */
	@Nonnull
	public Object[] getCommentTranslationArgs();

	/**
	 * Gets the type of the option value that this storage holds.
	 *
	 * @return the type of the option value, not null
	 */
	@Nonnull
	public Class<T> getType();

	/**
	 * Gets the stored option value.
	 *
	 * @return the stored value
	 */
	@Nullable
	public T getValue();

	/**
	 * Gets the option value validator that checks whether a certain value is valid for the option.
	 *
	 * @return the option value validator, not null
	 */
	@Nonnull
	public BiPredicate<IPlayerConfigClientStorageAPI, T> getValidator();

	/**
	 * Gets the text prefix for the option tooltip on the UI screens.
	 *
	 * @return the tooltip prefix, null if there is none
	 */
	@Nullable
	public String getTooltipPrefix();

	/**
	 * Checks whether this option is forced to its default player config value.
	 *
	 * @return true if the option value is defaulted, otherwise false
	 */
	public boolean isDefaulted();

	/**
	 * Checks whether the local client player can edit this option's value.
	 *
	 * @return true if the option value is mutable, otherwise false
	 */
	public boolean isMutable();

}
