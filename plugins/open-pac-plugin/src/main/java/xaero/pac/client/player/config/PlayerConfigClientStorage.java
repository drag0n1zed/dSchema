package xaero.pac.client.player.config;

import com.google.common.collect.Lists;
import xaero.pac.client.player.config.api.IPlayerConfigClientStorageAPI;
import xaero.pac.client.player.config.sub.PlayerSubConfigClientStorage;
import xaero.pac.common.list.SortedValueList;
import xaero.pac.common.misc.MapFactory;
import xaero.pac.common.server.player.config.PlayerConfig;
import xaero.pac.common.server.player.config.PlayerConfigOptionSpec;
import xaero.pac.common.server.player.config.api.IPlayerConfigOptionSpecAPI;
import xaero.pac.common.server.player.config.api.PlayerConfigType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public class PlayerConfigClientStorage implements IPlayerConfigClientStorage<PlayerConfigStringableOptionClientStorage<?>> {

	private final PlayerConfigClientStorageManager manager;
	private final PlayerConfigType type;
	private final UUID owner;
	private final Map<PlayerConfigOptionSpec<?>, PlayerConfigStringableOptionClientStorage<?>> options;
	private final List<String> subConfigIdsUnmodifiable;
	private final SortedValueList<String> subConfigIds;
	private String selectedSubConfig;
	private final Map<String, PlayerSubConfigClientStorage> subConfigs;
	private boolean syncInProgress;
	private boolean beingDeleted;
	private int subConfigLimit;

	protected PlayerConfigClientStorage(PlayerConfigClientStorageManager manager, PlayerConfigType type, UUID owner, Map<PlayerConfigOptionSpec<?>, PlayerConfigStringableOptionClientStorage<?>> options, List<String> subConfigIdsUnmodifiable, SortedValueList<String> subConfigIds, Map<String, PlayerSubConfigClientStorage> subConfigs) {
		super();
		this.manager = manager;
		this.type = type;
		this.owner = owner;
		this.options = options;
		this.subConfigIdsUnmodifiable = subConfigIdsUnmodifiable;
		this.subConfigIds = subConfigIds;
		this.subConfigs = subConfigs;
	}

	protected <T extends Comparable<T>> T getDefaultValue(PlayerConfigOptionSpec<T> option) {
		return option.getDefaultValue();
	}

	@Nonnull
	@Override
	@SuppressWarnings("unchecked")
	public <T extends Comparable<T>> PlayerConfigStringableOptionClientStorage<T> getOptionStorage(@Nonnull IPlayerConfigOptionSpecAPI<T> o){
		PlayerConfigOptionSpec<T> option = (PlayerConfigOptionSpec<T>) o;
		PlayerConfigStringableOptionClientStorage<T> result = (PlayerConfigStringableOptionClientStorage<T>) options.get(option);
		if(result == null){
			PlayerConfigStringableOptionClientStorage.Builder<T> builder = PlayerConfigStringableOptionClientStorage.Builder.begin();
			builder.setOption(option).setValue(getDefaultValue(option));
			options.put(option, result = builder.build());
		}
		return result;
	}

	@Nonnull
	@Override
	public PlayerConfigType getType() {
		return type;
	}

	@Nullable
	@Override
	public UUID getOwner() {
		return owner;
	}

	@Nonnull
	@Override
	public Stream<PlayerConfigStringableOptionClientStorage<?>> typedOptionStream(){
		return manager.getAllOptionsStream().map(this::getOptionStorage);
	}

	@Override
	public PlayerSubConfigClientStorage getOrCreateSubConfig(String subId){
		PlayerSubConfigClientStorage result = subConfigs.get(subId);
		if(result == null){
			result = PlayerSubConfigClientStorage.Builder.begin(LinkedHashMap::new)
					.setSubID(subId)
					.setOwner(owner)
					.setManager(manager)
					.setType(type).build();
			subConfigs.put(subId, result);
			subConfigIds.add(subId);
		}
		return result;
	}

	@Override
	public void removeSubConfig(String subId){
		PlayerSubConfigClientStorage removed = subConfigs.remove(subId);
		if(removed != null) {
			removed.setBeingDeleted(false);
			subConfigIds.remove(subId);
		}
	}

	@Nonnull
	public List<String> getSubConfigIds() {
		return subConfigIdsUnmodifiable;
	}

	@Nullable
	@Override
	public PlayerSubConfigClientStorage getSubConfig(@Nonnull String id) {
		return subConfigs.get(id);
	}

	@Nonnull
	@Override
	public PlayerConfigClientStorage getEffectiveSubConfig(@Nonnull String id) {
		if(PlayerConfig.MAIN_SUB_ID.equals(id))
			return this;
		PlayerSubConfigClientStorage sub = getSubConfig(id);
		return sub == null ? this : sub;
	}

	@Override
	public boolean subConfigExists(@Nonnull String id) {
		return subConfigs.containsKey(id);
	}

	@Override
	public int getSubCount() {
		return subConfigs.size();
	}

	@Nonnull
	public Stream<IPlayerConfigClientStorageAPI> getSubConfigAPIStream(){
		return getSubConfigStream().map(Function.identity());
	}

	@Override
	public Stream<IPlayerConfigClientStorage<PlayerConfigStringableOptionClientStorage<?>>> getSubConfigStream() {
		return subConfigs.values().stream().map(Function.identity());
	}

	@Override
	public void setSelectedSubConfig(String selectedSubConfig) {
		this.selectedSubConfig = selectedSubConfig;
	}

	@Override
	public String getSelectedSubConfig() {
		return selectedSubConfig;
	}

	public boolean isSubConfigSelected(){
		return selectedSubConfig != null && !selectedSubConfig.equals(PlayerConfig.MAIN_SUB_ID) && subConfigExists(selectedSubConfig);
	}

	@Override
	public void reset() {
		options.clear();
		selectedSubConfig = null;
		if(subConfigs != null) {
			subConfigIds.clear();
			subConfigIds.add(PlayerConfig.MAIN_SUB_ID);
			subConfigs.clear();
			syncInProgress = true;
		}
	}

	public String getSubId(){
		return null;
	}

	@Override
	public boolean isSyncInProgress() {
		return syncInProgress;
	}

	@Override
	public void setSyncInProgress(boolean syncInProgress) {
		this.syncInProgress = syncInProgress;
	}

	@Override
	public void setGeneralState(boolean beingDeleted, int subConfigLimit) {
		this.beingDeleted = beingDeleted;
		this.subConfigLimit = subConfigLimit;
	}

	@Override
	public boolean isBeingDeleted() {
		return beingDeleted;
	}

	public void setBeingDeleted(boolean beingDeleted) {
		this.beingDeleted = beingDeleted;
	}

	@Override
	public int getSubConfigLimit() {
		if(type == PlayerConfigType.SERVER)
			return Integer.MAX_VALUE;
		return subConfigLimit;
	}

	public static abstract class Builder<B extends Builder<B>> implements IBuilder<PlayerConfigClientStorage> {

		protected final B self;
		protected PlayerConfigType type;
		protected UUID owner;
		protected PlayerConfigClientStorageManager manager;
		protected final MapFactory mapFactory;

		@SuppressWarnings("unchecked")
		protected Builder(MapFactory mapFactory) {
			super();
			this.self = (B) this;
			this.mapFactory = mapFactory;
		}

		@Override
		public B setDefault() {
			setOwner(null);
			setType(null);
			setManager(null);
			return self;
		}

		@Override
		public B setType(PlayerConfigType type) {
			this.type = type;
			return self;
		}

		@Override
		public B setOwner(UUID owner) {
			this.owner = owner;
			return self;
		}

		public B setManager(PlayerConfigClientStorageManager manager) {
			this.manager = manager;
			return self;
		}

		public PlayerConfigClientStorage build() {
			if(type == null || manager == null)
				throw new IllegalStateException();
			Map<PlayerConfigOptionSpec<?>, PlayerConfigStringableOptionClientStorage<?>> options = mapFactory.get();
			return buildInternally(options);
		}

		protected abstract PlayerConfigClientStorage buildInternally(Map<PlayerConfigOptionSpec<?>, PlayerConfigStringableOptionClientStorage<?>> options);

	}

	public static final class FinalBuilder extends Builder<FinalBuilder> {

		private FinalBuilder(MapFactory mapFactory) {
			super(mapFactory);
		}

		@Override
		protected PlayerConfigClientStorage buildInternally(Map<PlayerConfigOptionSpec<?>, PlayerConfigStringableOptionClientStorage<?>> options) {
			List<String> subConfigIdsStorage = Lists.newArrayList(PlayerConfig.MAIN_SUB_ID);
			List<String> subConfigIdsUnmodifiable = Collections.unmodifiableList(subConfigIdsStorage);
			SortedValueList<String> subConfigIds = SortedValueList.Builder.<String>begin()
					.setContent(subConfigIdsStorage)
					.build();
			return new PlayerConfigClientStorage(manager, type, owner, options, subConfigIdsUnmodifiable, subConfigIds, mapFactory.get());
		}

		public static FinalBuilder begin(MapFactory mapFactory) {
			return new FinalBuilder(mapFactory).setDefault();
		}

	}

}
