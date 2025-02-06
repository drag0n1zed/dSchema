package xaero.pac.common.server.player;

import net.minecraft.server.level.ServerPlayer;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.packet.ServerLoginHandshakePacket;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.PartyMemberDynamicInfoSyncable;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.claims.player.ServerPlayerClaimInfo;
import xaero.pac.common.server.claims.player.request.PlayerClaimActionRequestHandler;
import xaero.pac.common.server.claims.sync.ClaimsManagerSynchronizer;
import xaero.pac.common.server.claims.sync.player.ClaimsManagerPlayerClaimOwnerPropertiesSync;
import xaero.pac.common.server.claims.sync.player.ClaimsManagerPlayerRegionSync;
import xaero.pac.common.server.claims.sync.player.ClaimsManagerPlayerStateSync;
import xaero.pac.common.server.claims.sync.player.ClaimsManagerPlayerSubClaimPropertiesSync;
import xaero.pac.common.server.parties.party.IPartyManager;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.parties.party.sync.PartySynchronizer;
import xaero.pac.common.server.parties.party.sync.player.PlayerFullPartySync;
import xaero.pac.common.server.player.config.sync.task.PlayerConfigSyncSpreadoutTask;
import xaero.pac.common.server.player.data.ServerPlayerData;
import xaero.pac.common.server.player.data.api.ServerPlayerDataAPI;

public class PlayerLoginHandler {

	public void handlePreWorldJoin(ServerPlayer player, IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData) {
		ServerPlayerData playerData = (ServerPlayerData) ServerPlayerDataAPI.from(player);

		OpenPartiesAndClaims.INSTANCE.getPacketHandler().sendToPlayer(player, new ServerLoginHandshakePacket());

		IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>> playerClaimInfo = serverData.getServerClaimsManager().getPlayerInfo(player.getUUID());
		((ServerPlayerClaimInfo)(Object)playerClaimInfo).setPlayerUsername(player.getGameProfile().getName());

		serverData.getForceLoadManager().updateTicketsFor(serverData.getPlayerConfigs(), player.getUUID(), false);

		serverData.getPlayerPartyAssigner().assign(serverData, player, serverData.getPartyMemberInfoUpdater());

		PlayerFullPartySync playerFullPartySync = new PlayerFullPartySync((PartySynchronizer)(Object)serverData.getPartyManager().getPartySynchronizer());

		ClaimsManagerPlayerClaimOwnerPropertiesSync claimsManagerPlayerClaimOwnerPropertiesSync = ClaimsManagerPlayerClaimOwnerPropertiesSync.Builder.begin()
				.setPlayer(player)
				.setSynchronizer((ClaimsManagerSynchronizer) serverData.getServerClaimsManager().getClaimsManagerSynchronizer())
				.build();
		ClaimsManagerPlayerSubClaimPropertiesSync claimsManagerPlayerSubClaimPropertiesSync = ClaimsManagerPlayerSubClaimPropertiesSync.Builder.begin()
				.setPlayer(player)
				.setClaimOwnerPropertiesSync(claimsManagerPlayerClaimOwnerPropertiesSync)
				.setSynchronizer((ClaimsManagerSynchronizer) serverData.getServerClaimsManager().getClaimsManagerSynchronizer())
				.build();
		ClaimsManagerPlayerStateSync claimsManagerPlayerStateSync = ClaimsManagerPlayerStateSync.Builder.begin()
				.setPlayer(player)
				.setSynchronizer((ClaimsManagerSynchronizer) serverData.getServerClaimsManager().getClaimsManagerSynchronizer())
				.setSubClaimPropertiesSync(claimsManagerPlayerSubClaimPropertiesSync)
				.build();

		playerData.onLogin(
				playerFullPartySync,
				ClaimsManagerPlayerRegionSync.Builder.begin()
						.setClaimsManager(serverData.getServerClaimsManager())
						.setStateSyncHandler(claimsManagerPlayerStateSync)
						.setPlayerId(player.getUUID())
						.build(),
				claimsManagerPlayerStateSync,
				claimsManagerPlayerClaimOwnerPropertiesSync,
				claimsManagerPlayerSubClaimPropertiesSync,
				PlayerClaimActionRequestHandler.Builder.begin()
						.setManager(serverData.getServerClaimsManager())
						.setServerTickHandler(serverData.getServerTickHandler())
						.build(),
				PlayerConfigSyncSpreadoutTask.Builder.begin().build()
		);
		playerData.setOftenSyncedPartyMemberInfo(new PartyMemberDynamicInfoSyncable(player.getUUID(), true));

		serverData.getServerClaimsManager().getPlayerInfo(player.getUUID()).registerActivity(serverData.getServerInfo());
	}

	public void handlePostWorldJoin(ServerPlayer player, IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData) {
		serverData.getPlayerConfigs().getSynchronizer().syncOnLogin(player);

		IPartyManager<IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> partyManager = serverData.getPartyManager();
		IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly> playerParty = partyManager.getPartyByMember(player.getUUID());

		if(playerParty != null)
			serverData.getPartyManager().getPartySynchronizer().syncToClient(player, playerParty);

		IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>> claimsManager = serverData.getServerClaimsManager();
		claimsManager.getClaimsManagerSynchronizer().syncOnLogin(player);
	}

}
