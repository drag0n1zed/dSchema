package xaero.pac.common.server.player.data.api;

import net.minecraft.server.level.ServerPlayer;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.ServerData;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.core.ServerCore;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.data.IOpenPACServerPlayer;
import xaero.pac.common.server.player.data.ServerPlayerData;

import javax.annotation.Nonnull;

/**
 * API for data attached to a server player
 */
public abstract class ServerPlayerDataAPI {

	/**
	 * Checks if the player is using the claims admin mode.
	 *
	 * @return true if the player is in the claims admin mode, otherwise false
	 */
	public abstract boolean isClaimsAdminMode();

	/**
	 * Checks if the player is using the claims non-ally mode.
	 *
	 * @return true if the player is in the claims non-ally mode, otherwise false
	 */
	public abstract boolean isClaimsNonallyMode();

	/**
	 * Checks if the player is using the server claim mode.
	 *
	 * @return true if the player is in the server claim mode, otherwise false
	 */
	public abstract boolean isClaimsServerMode();

	/**
	 * Gets the player data for a specified logged in player.
	 *
	 * @param player  the player, not null
	 * @return the parties and claims player data for the player, not null
	 */
	@Nonnull
	public static ServerPlayerDataAPI from(@Nonnull ServerPlayer player) {
		ServerPlayerDataAPI result = ((IOpenPACServerPlayer)player).getXaero_OPAC_PlayerData();
		if(result == null)
			((IOpenPACServerPlayer) player).setXaero_OPAC_PlayerData(result = new ServerPlayerData());
		ServerPlayerData data = (ServerPlayerData)result;
		if(!data.hasHandledLogin() && player.connection != null && ServerCore.getServerGamePacketListenerConnection(player.connection) != null && !ServerCore.getServerGamePacketListenerConnection(player.connection).isConnecting()){//isConnecting() = the channel is null
			ServerPlayer placedPlayer = player.getServer().getPlayerList().getPlayer(player.getUUID());
			if(placedPlayer == player) {//this method might be called before placing the player, when syncing commands, which is a problem, so we're making sure that the player has been placed
				data.setHandledLogin(true);
				IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
						serverData = ServerData.from(player.getServer());
				//Minecraft leaves players in the list on login exceptions, which causes this mod to crash afterwards.
				//Putting this stuff here, instead of just the login event, to ensure that the login is handled for all real players.
				serverData.getPlayerLoginHandler().handlePreWorldJoin(player, serverData);
				serverData.getPlayerWorldJoinHandler().onWorldJoin(serverData, player.serverLevel(), player);
				serverData.getPlayerLoginHandler().handlePostWorldJoin(player, serverData);
			}
		}
		return result;
	}

}
