package xaero.pac.common.server.parties.party;

import net.minecraft.world.entity.player.Player;
import xaero.pac.common.server.parties.party.api.IPartyManagerAPI;
import xaero.pac.common.server.parties.party.api.IServerPartyAPI;
import xaero.pac.common.server.parties.party.sync.IPartySynchronizer;
import xaero.pac.common.server.player.config.IPlayerConfigManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;
import java.util.stream.Stream;

public interface IPartyManager<
	P extends IServerParty<?, ?, ?>
> extends IPartyManagerAPI {

	//internal API
	@Nullable
	@Override
	public P getPartyByOwner(@Nonnull UUID owner);

	@Nullable
	@Override
	public P getPartyById(@Nonnull UUID id);

	@Nullable
	@Override
	public P getPartyByMember(@Nonnull UUID member);

	@Nullable
	@Override
	P createPartyForOwner(@Nonnull Player owner);

	public IPartySynchronizer<P> getPartySynchronizer();
	public IPlayerConfigManager getPlayerConfigs();

	public void removeTypedParty(@Nonnull P party);
	@Nonnull
	public Stream<P> getTypedAllStream();
	@Nonnull
	public Stream<P> getTypedPartiesThatAlly(@Nonnull UUID allyId);

	@Override
	@SuppressWarnings("unchecked")
	default void removeParty(@Nonnull IServerPartyAPI party) {
		removeTypedParty((P)party);
	}

	@Nonnull
	@Override
	@SuppressWarnings("unchecked")
	default Stream<IServerPartyAPI> getAllStream() {
		return (Stream<IServerPartyAPI>)(Object)getTypedAllStream();
	}

	@Nonnull
	@Override
	@SuppressWarnings("unchecked")
	default Stream<IServerPartyAPI> getPartiesThatAlly(@Nonnull UUID allyId) {
		return (Stream<IServerPartyAPI>)(Object)getTypedPartiesThatAlly(allyId);
	}

}
