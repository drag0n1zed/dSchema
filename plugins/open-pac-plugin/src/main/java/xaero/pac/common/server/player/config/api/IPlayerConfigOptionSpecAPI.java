package xaero.pac.common.server.player.config.api;

import net.minecraft.network.chat.Component;
import xaero.pac.client.player.config.api.IPlayerConfigClientStorageAPI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A player config option instance used for player config option representation in various API features.
 *
 * @param <T>  the type of values of this option
 */
public interface IPlayerConfigOptionSpecAPI<T extends Comparable<T>> {

	/**
	 * Gets the ID of this option.
	 * <p>
	 * Use {@link #getPath()} if you need it separated into elements.
	 *
	 * @return the string ID, not null
	 */
	@Nonnull
	public String getId();

	/**
	 * Gets the shortened ID of this option, without the "playerConfig." prefix.
	 *
	 * @return the shortened string ID, not null
	 */
	@Nonnull
	public String getShortenedId();

	/**
	 * Gets the path of this option, which is just the ID from {@link #getId()} but separated into elements.
	 *
	 * @return the path, not null
	 */
	@Nonnull
	public List<String> getPath();

	/**
	 * Gets the type of values that this option can have.
	 *
	 * @return the type of values, not null
	 */
	@Nonnull
	public Class<T> getType();

	/**
	 * Gets the translation key for the name of this option.
	 *
	 * @return the translation key, not null
	 */
	@Nonnull
	public String getTranslation();

	/**
	 * Gets the translation key arguments for the name of this option.
	 *
	 * @return the translation key arguments, not null
	 */
	@Nonnull
	public String[] getTranslationArgs();

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
	public String[] getCommentTranslationArgs();

	/**
	 * Gets the default en_us comment for this option.
	 *
	 * @return the default comment, not null
	 */
	@Nonnull
	public String getComment();

	/**
	 * Gets the default value that this option is set to in configs.
	 *
	 * @return the default value, not null
	 */
	@Nonnull
	public T getDefaultValue();

	/**
	 * Gets the client-side validator for potential values of this option.
	 *
	 * @return the client-side value validator, not null
	 */
	@Nonnull
	public BiPredicate<IPlayerConfigClientStorageAPI, T> getClientSideValidator();

	/**
	 * Gets the server-side validator for potential values of this option.
	 *
	 * @return the server-side value validator, not null
	 */
	@Nonnull
	public BiPredicate<IPlayerConfigAPI, T> getServerSideValidator();

	/**
	 * Gets the prefix applied to the tooltip of this option on the UI.
	 *
	 * @return the tooltip prefix, null if no prefix
	 */
	@Nullable
	public String getTooltipPrefix();

	/**
	 * Gets the String->value parser of this option, mainly used for commands.
	 *
	 * @return the String->value parser, not null
	 */
	@Nonnull
	public Function<String, T> getCommandInputParser();

	/**
	 * Gets the value->String converter of this option, mainly used for commands.
	 *
	 * @return the value->String converter, not null
	 */
	@Nonnull
	public Function<T, Component> getCommandOutputWriter();

	/**
	 * Gets the config type filter of this option.
	 * <p>
	 * The filter allows this option to only appear and be configurable on some types of player configs
	 * (e.g. just the server claims config).
	 *
	 * @return the config type filter, not null
	 */
	@Nonnull
	Predicate<PlayerConfigType> getConfigTypeFilter();

}
