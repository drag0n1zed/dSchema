package xaero.pac.common.server.player;

import net.minecraft.server.level.ServerPlayer;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.claims.player.ServerPlayerClaimWelcomer;
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.data.ServerPlayerData;
import xaero.pac.common.server.player.data.api.ServerPlayerDataAPI;

public class PlayerTickHandler {

	private final ServerPlayerClaimWelcomer claimWelcomer;

	private PlayerTickHandler(ServerPlayerClaimWelcomer claimWelcomer) {
		this.claimWelcomer = claimWelcomer;
	}

	public void onTick(ServerPlayer player, IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData) {
		ServerPlayerData mainCap = (ServerPlayerData) ServerPlayerDataAPI.from(player);
		if(!mainCap.hasHandledLogin())
			return;
		mainCap.onTick();
		if(mainCap.shouldResyncPlayerConfigs()) {
			serverData.getPlayerConfigs().getSynchronizer().syncAllToClient(player);
			mainCap.setShouldResyncPlayerConfigs(false);
		}
		if(ServerConfig.CONFIG.claimsEnabled.get()) {
			claimWelcomer.onPlayerTick(mainCap, player, serverData);
			IServerClaimsManager<?, ?, ?> claimsManager = serverData.getServerClaimsManager();
			int currentBaseClaimLimit = claimsManager.getPlayerBaseClaimLimit(player);
			int currentBaseForceloadLimit = claimsManager.getPlayerBaseForceloadLimit(player);
			if(mainCap.checkBaseClaimLimitsSync(currentBaseClaimLimit, currentBaseForceloadLimit)) {
				if(mainCap.haveCheckedBaseForceloadLimitOnce()) {
					claimsManager.getClaimsManagerSynchronizer().syncClaimLimits(serverData.getPlayerConfigs().getLoadedConfig(player.getUUID()), player);
					serverData.getForceLoadManager().updateTicketsFor(serverData.getPlayerConfigs(), player.getUUID(), false);
				}
				mainCap.setCheckedBaseForceloadLimitOnce();
				mainCap.setLastClaimLimitsSync(currentBaseClaimLimit, currentBaseForceloadLimit);
			}
		}

		serverData.getPartyManager().getPartySynchronizer().getOftenSyncedInfoSync().onPlayerTick(mainCap, player);
	}

	public static final class Builder {

		private Builder(){}

		public Builder setDefault(){
			return this;
		}

		public PlayerTickHandler build(){
			return new PlayerTickHandler(new ServerPlayerClaimWelcomer());
		}

		public static Builder begin(){
			return new Builder().setDefault();
		}

	}

}
