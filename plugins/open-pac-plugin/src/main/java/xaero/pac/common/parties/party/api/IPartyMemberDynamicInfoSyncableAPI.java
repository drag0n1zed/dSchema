package xaero.pac.common.parties.party.api;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

/**
 * API for the dynamic info (location) of a party member
 */
public interface IPartyMemberDynamicInfoSyncableAPI {

	/**
	 * Gets the party/ally player's UUID.
	 *
	 * @return the party/ally player's UUID, not null
	 */
	@Nonnull
	public UUID getPlayerId();

	/**
	 * Gets the X coordinate of the current position of this player in the world.
	 * <p>
	 * The player position is 0,0,0 on the server side before the first update.
	 *
	 * @return the X coordinate of the player's position
	 */
	public double getX();

	/**
	 * Gets the Y coordinate of the current position of this player in the world.
	 * <p>
	 * The player position is 0,0,0 on the server side before the first update.
	 *
	 * @return the Y coordinate of the player's position
	 */
	public double getY();

	/**
	 * Gets the Z coordinate of the current position of this player in the world.
	 * <p>
	 * The player position is 0,0,0 on the server side before the first update.
	 *
	 * @return the Z coordinate of the player's position
	 */
	public double getZ();

	/**
	 * Gets the ID of the dimension that this player is currently in.
	 *
	 * @return the dimension ID for this player, null on server side before the first update
	 */
	@Nullable
	public ResourceLocation getDimension();

}
