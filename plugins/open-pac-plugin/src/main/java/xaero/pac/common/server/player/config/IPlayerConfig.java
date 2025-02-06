package xaero.pac.common.server.player.config;

import xaero.pac.common.server.player.config.api.IPlayerConfigAPI;
import xaero.pac.common.server.player.config.api.IPlayerConfigOptionSpecAPI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public interface IPlayerConfig extends IPlayerConfigAPI {
	//internal API

	public <T extends Comparable<T>> T applyDefaultReplacer(IPlayerConfigOptionSpecAPI<T> o, T value);

	@Nullable
	@Override
	public IPlayerConfig getSubConfig(@Nonnull String id);

	@Nonnull
	@Override
	public IPlayerConfig getEffectiveSubConfig(@Nonnull String id);

	@Override
	@Nonnull
	public IPlayerConfig getEffectiveSubConfig(int subIndex);

	@Override
	public boolean subConfigExists(@Nonnull String id);

	@Override
	public boolean subConfigExists(int subIndex);

	@Override
	@Nonnull
	public IPlayerConfig getUsedSubConfig();

	@Nonnull
	@Override
	IPlayerConfig getUsedServerSubConfig();

	@Nullable
	@Override
	public IPlayerConfig createSubConfig(@Nonnull String id);

	@Nullable
	@Override
	public String getSubId();

	@Override
	public int getSubIndex();

	@Override
	public int getSubCount();

	@Nonnull
	@Override
	public List<String> getSubConfigIds();

	@Nonnull
	@Override
	public Stream<IPlayerConfigAPI> getSubConfigAPIStream();

	@Nullable
	@Override
	public <T extends Comparable<T>> T getDefaultRawValue(@Nonnull IPlayerConfigOptionSpecAPI<T> option);

	@Override
	public boolean isOptionAllowed(@Nonnull IPlayerConfigOptionSpecAPI<?> option);

	@Override
	public boolean isBeingDeleted();

	public void setBeingDeleted();

	public IPlayerConfig removeSubConfig(String id);

	public IPlayerConfig removeSubConfig(int index);

	public Iterator<IPlayerConfig> getSubConfigIterator();


}
