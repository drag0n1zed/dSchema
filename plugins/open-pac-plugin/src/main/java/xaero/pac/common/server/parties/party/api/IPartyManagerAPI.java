package xaero.pac.common.server.parties.party.api;

import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * API for the party manager on the server side
 */
public interface IPartyManagerAPI {

	/**
	 * Gets the owned party from the UUID of a party's owner.
	 *
	 * @param owner  the player UUID of the party owner, not null
	 * @return the party, null if doesn't exist
	 */
	@Nullable
	public IServerPartyAPI getPartyByOwner(@Nonnull UUID owner);

	/**
	 * Gets the party of a specified UUID.
	 *
	 * @param id  the party UUID, not null
	 * @return the party, null if doesn't exist
	 */
	@Nullable
	public IServerPartyAPI getPartyById(@Nonnull UUID id);

	/**
	 * Gets the party from the UUID of a party member.
	 *
	 * @param member  the player UUID of the party member, not null
	 * @return the party, null if doesn't exist
	 */
	@Nullable
	public IServerPartyAPI getPartyByMember(@Nonnull UUID member);

	/**
	 * Checks whether the player with a specified UUID owns a party.
	 *
	 * @param owner  the player UUID, not null
	 * @return true if the player owns a party, otherwise false
	 */
	public boolean partyExistsForOwner(@Nonnull UUID owner);

	/**
	 * Creates a new party to be owned by a specified logged in player.
	 *
	 * @param owner  the player to own the created party, not null
	 * @return the created party, null if the player already owns a party, the parties feature is disabled
	 *         or the party wasn't created for another reason
	 */
	@Nullable
	public IServerPartyAPI createPartyForOwner(@Nonnull Player owner);

	/**
	 * Removes the party owned by the player with a specified UUID.
	 *
	 * @param owner  the player UUID of the party owner, not null
	 */
	public void removePartyByOwner(@Nonnull UUID owner);

	/**
	 * Removes the party with a specified UUID.
	 *
	 * @param id  the party UUID, not null
	 */
	public void removePartyById(@Nonnull UUID id);

	/**
	 * Removes a specified party.
	 *
	 * @param party  the party instance, not null
	 */
	public void removeParty(@Nonnull IServerPartyAPI party);

	/**
	 * Gets a stream of all parties.
	 *
	 * @return the stream of all parties, not null
	 */
	@Nonnull
	public Stream<IServerPartyAPI> getAllStream();

	/**
	 * Gets a stream of all parties that ally a party with a specified UUID.
	 *
	 * @param allyId  the ally party ID, not null
	 * @return the stream of parties that ally the party, not null
	 */
	@Nonnull
	public Stream<IServerPartyAPI> getPartiesThatAlly(@Nonnull UUID allyId);

}
