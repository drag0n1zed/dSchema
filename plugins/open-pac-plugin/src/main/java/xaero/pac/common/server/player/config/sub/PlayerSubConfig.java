package xaero.pac.common.server.player.config.sub;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.toml.TomlFormat;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import xaero.pac.common.list.SortedValueList;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.config.IPlayerConfig;
import xaero.pac.common.server.player.config.PlayerConfig;
import xaero.pac.common.server.player.config.PlayerConfigManager;
import xaero.pac.common.server.player.config.PlayerConfigOptionSpec;
import xaero.pac.common.server.player.config.api.IPlayerConfigOptionSpecAPI;
import xaero.pac.common.server.player.config.api.PlayerConfigType;
import xaero.pac.common.util.linked.ILinkedChainNode;
import xaero.pac.common.util.linked.LinkedChain;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;

import static xaero.pac.common.server.player.config.api.PlayerConfigOptions.*;

public class PlayerSubConfig
<
	P extends IServerParty<?, ?, ?>
> extends PlayerConfig<P> implements ILinkedChainNode<PlayerSubConfig<P>>, IPlayerConfig {

	public static final Set<IPlayerConfigOptionSpecAPI<?>> STATIC_OVERRIDABLE_OPTIONS;

	static {
		STATIC_OVERRIDABLE_OPTIONS = new HashSet<>();
		STATIC_OVERRIDABLE_OPTIONS.addAll(OPTIONS.values());
		STATIC_OVERRIDABLE_OPTIONS.remove(USED_SUBCLAIM);
		STATIC_OVERRIDABLE_OPTIONS.remove(USED_SERVER_SUBCLAIM);
		STATIC_OVERRIDABLE_OPTIONS.remove(PARTY_NAME);
		STATIC_OVERRIDABLE_OPTIONS.remove(BONUS_CHUNK_CLAIMS);
		STATIC_OVERRIDABLE_OPTIONS.remove(BONUS_CHUNK_FORCELOADS);
		STATIC_OVERRIDABLE_OPTIONS.remove(SHARE_LOCATION_WITH_PARTY);
		STATIC_OVERRIDABLE_OPTIONS.remove(SHARE_LOCATION_WITH_PARTY_MUTUAL_ALLIES);
		STATIC_OVERRIDABLE_OPTIONS.remove(RECEIVE_LOCATIONS_FROM_PARTY);
		STATIC_OVERRIDABLE_OPTIONS.remove(RECEIVE_LOCATIONS_FROM_PARTY_MUTUAL_ALLIES);
		STATIC_OVERRIDABLE_OPTIONS.remove(FORCELOAD);
		STATIC_OVERRIDABLE_OPTIONS.remove(OFFLINE_FORCELOAD);
	}

	private final PlayerConfig<P> mainConfig;
	private final String subId;
	private final int subIndex;
	private PlayerSubConfig<P> nextInChain;
	private PlayerSubConfig<P> previousInChain;
	private boolean destroyed;

	private PlayerSubConfig(PlayerConfig<P> mainConfig, String subId, PlayerConfigType type, UUID playerId, PlayerConfigManager<P, ?> manager, Map<PlayerConfigOptionSpec<?>, Object> automaticDefaultValues, LinkedChain<PlayerSubConfig<P>> linkedSubConfigs, Map<String, PlayerSubConfig<P>> subByID, Int2ObjectMap<String> subIndexToID, SortedValueList<String> subConfigIds, List<String> subConfigIdsUnmodifiable, int subIndex) {
		super(type, playerId, manager, automaticDefaultValues, linkedSubConfigs, subByID, subIndexToID, subConfigIds, subConfigIdsUnmodifiable);
		this.mainConfig = mainConfig;
		this.subId = subId;
		this.subIndex = subIndex;
	}

	@Override
	public Config getStorage() {
		if(storage == null) {
			setStorage(Config.of(LinkedHashMap::new, TomlFormat.instance()));
			setDirty(true);
		}
		return storage;
	}

	@Override
	public boolean isOptionAllowed(@Nonnull IPlayerConfigOptionSpecAPI<?> option) {
		return super.isOptionAllowed(option) && manager.getOverridableOptions().contains(option);
	}

	private <T extends Comparable<T>> T getInner(IPlayerConfigOptionSpecAPI<T> o, boolean inherit){
		PlayerConfigOptionSpec<T> option = (PlayerConfigOptionSpec<T>) o;
		if(!manager.getOverridableOptions().contains(option))
			return inherit ? mainConfig.getFromEffectiveConfig(option) : null;
		if(isOptionDefaulted(option))
			return inherit ? manager.getDefaultConfig().getFromEffectiveConfig(option) : null;
		Config storage = getStorage();
		T overrideValue = storage.get(option.getPath());
		if(overrideValue == null && inherit)
			return mainConfig.getFromEffectiveConfig(option);
		return overrideValue;
	}

	@Nonnull
	@Override
	public <T extends Comparable<T>> T getFromEffectiveConfig(@Nonnull IPlayerConfigOptionSpecAPI<T> o) {
		return getInner(o, true);
	}

	public boolean isInherited(IPlayerConfigOptionSpecAPI<?> o){
		return getInner(o, false) == null;
	}

	@Override
	protected <T extends Comparable<T>> boolean isValidSetValue(@Nonnull PlayerConfigOptionSpec<T> option, @Nullable T value) {
		return value == null || super.isValidSetValue(option, value);
	}

	@Override
	protected <T extends Comparable<T>> T getValueForDefaultConfigMatch(T actualEffective, T value) {
		return null;
	}

	@Nullable
	@Override
	public <T extends Comparable<T>> T getDefaultRawValue(@Nonnull IPlayerConfigOptionSpecAPI<T> option) {
		return null;
	}

	public PlayerConfig<P> getMainConfig() {
		return mainConfig;
	}

	@Nullable
	@Override
	public String getSubId() {
		return subId;
	}

	@Override
	public int getSubIndex() {
		return subIndex;
	}

	@Nullable
	@Override
	public PlayerSubConfig<P> createSubConfig(@Nonnull String id) {
		throw new RuntimeException(new IllegalAccessException());
	}

	@Nonnull
	@Override
	public PlayerConfig<P> getUsedSubConfig() {
		throw new RuntimeException(new IllegalAccessException());
	}

	@Nullable
	@Override
	public PlayerConfig<P> getSubConfig(@Nonnull String id) {
		throw new RuntimeException(new IllegalAccessException());
	}

	@Nonnull
	@Override
	public PlayerConfig<P> getEffectiveSubConfig(int subIndex) {
		throw new RuntimeException(new IllegalAccessException());
	}

	@Override
	public boolean subConfigExists(@Nonnull String id) {
		throw new RuntimeException(new IllegalAccessException());
	}

	@Override
	public boolean subConfigExists(int subIndex) {
		throw new RuntimeException(new IllegalAccessException());
	}

	@Override
	public PlayerSubConfig<P> removeSubConfig(String id) {
		throw new RuntimeException(new IllegalAccessException());
	}

	@Override
	public PlayerSubConfig<P> removeSubConfig(int index) {
		throw new RuntimeException(new IllegalAccessException());
	}

	@Override
	public PlayerSubConfig<P> createSubConfig(String id, int index) {
		throw new RuntimeException(new IllegalAccessException());
	}

	@Nonnull
	@Override
	public List<String> getSubConfigIds() {
		throw new RuntimeException(new IllegalAccessException());
	}

	@Override
	public Stream<PlayerSubConfig<P>> getSubConfigStream() {
		throw new RuntimeException(new IllegalAccessException());
	}

	@Override
	public int getSubCount() {
		return 0;
	}

	@Override
	public int getSubConfigLimit() {
		return 0;
	}

	@Override
	public void setNext(PlayerSubConfig<P> element) {
		this.nextInChain = element;
	}

	@Override
	public void setPrevious(PlayerSubConfig<P> element) {
		this.previousInChain = element;
	}

	@Override
	public PlayerSubConfig<P> getNext() {
		return this.nextInChain;
	}

	@Override
	public PlayerSubConfig<P> getPrevious() {
		return this.previousInChain;
	}

	@Override
	public boolean isDestroyed() {
		return this.destroyed;
	}

	@Override
	public void onDestroyed() {
		this.destroyed = true;
	}

	public static final class Builder
	<
		P extends IServerParty<?, ?, ?>
	> extends PlayerConfig.Builder<P, Builder<P>> {

		private PlayerConfig<P> mainConfig;
		private String subId;
		private int subIndex;

		@Override
		public Builder<P> setDefault() {
			super.setDefault();
			setMainConfig(null);
			setSubId(null);
			setSubIndex(-1);
			return self;
		}

		public Builder<P> setMainConfig(PlayerConfig<P> mainConfig) {
			this.mainConfig = mainConfig;
			return self;
		}

		public Builder<P> setSubId(String subId) {
			this.subId = subId;
			return self;
		}

		public Builder<P> setSubIndex(int subIndex) {
			this.subIndex = subIndex;
			return self;
		}

		@Override
		public PlayerSubConfig<P> build() {
			if(mainConfig == null || subId == null || subIndex == -1)
				throw new IllegalStateException();
			if(!Objects.equals(mainConfig.getPlayerId(), playerId))
				throw new IllegalArgumentException("Mismatching player config UUIDs!");
			return (PlayerSubConfig<P>)super.build();
		}

		@Override
		protected PlayerSubConfig<P> buildInternally() {
			return new PlayerSubConfig<>(mainConfig, subId, type, playerId, manager, automaticDefaultValues, null, null, null, null, null, subIndex);
		}

		public static <P extends IServerParty<?, ?, ?>> Builder<P> begin(){
			return new Builder<P>().setDefault();
		}

	}

}
