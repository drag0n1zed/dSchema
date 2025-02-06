package xaero.pac.common.claims.player;

import xaero.pac.common.claims.player.api.IPlayerChunkClaimAPI;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.UUID;

public class PlayerChunkClaim implements IPlayerChunkClaim {

	private final UUID playerId;
	private final int subConfigIndex;
	private final boolean forceloadable;
	private final int syncIndex;

	public PlayerChunkClaim(UUID playerId, int subConfigIndex, boolean forceloadable, int syncIndex) {
		super();
		this.playerId = playerId;
		this.subConfigIndex = subConfigIndex;
		this.forceloadable = forceloadable;
		this.syncIndex = syncIndex;
	}

	public boolean isForceloadable() {
		return forceloadable;
	}

	public static long getLongCoordinatesFor(int x, int z) {
		return ((long) x << 32) | (z & 0xFFFFFFFFL);
	}

	public static int getXFromLongCoordinates(long key) {
		return (int) (key >> 32);
	}

	public static int getZFromLongCoordinates(long key) {
		return (int) (key & 0xFFFFFFFF);
	}

	@Nonnull
	public UUID getPlayerId() {
		return playerId;
	}

	@Override
	public int getSubConfigIndex() {
		return subConfigIndex;
	}

	public int getSyncIndex() {
		return syncIndex;
	}

	@Override
	public int hashCode() {
		return Objects.hash(playerId, subConfigIndex, forceloadable);
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this)
			return true;
		if(obj == null || !(obj instanceof PlayerChunkClaim other))
			return false;
		return playerId.equals(other.playerId) && forceloadable == other.forceloadable && subConfigIndex == other.subConfigIndex;
	}

	@Override
	public boolean isSameClaimType(IPlayerChunkClaimAPI other) {//ignores forceloadable differences
		if(other == this)
			return true;
		if(other == null)
			return false;
		return playerId.equals(other.getPlayerId()) && subConfigIndex == other.getSubConfigIndex();
	}

	@Override
	public String toString() {
		return String.format("[%s, %d, %s, %d]", playerId, subConfigIndex, forceloadable, syncIndex);
	}

}
