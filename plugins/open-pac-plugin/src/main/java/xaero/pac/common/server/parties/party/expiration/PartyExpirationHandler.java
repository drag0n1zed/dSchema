package xaero.pac.common.server.parties.party.expiration;

import net.minecraft.server.MinecraftServer;
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
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.expiration.ObjectExpirationHandler;
import xaero.pac.common.server.info.ServerInfo;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.parties.party.PartyManager;
import xaero.pac.common.server.parties.party.ServerParty;

public final class PartyExpirationHandler extends ObjectExpirationHandler<ServerParty, PartyManager>{

	private final MinecraftServer server;

	protected PartyExpirationHandler(ServerInfo serverInfo, MinecraftServer server, PartyManager manager, long liveCheckInterval, long expirationTime,
			String checkingMessage) {
		super(serverInfo, manager, liveCheckInterval, expirationTime, checkingMessage);
		this.server = server;
	}

	@Override
	protected void handle(IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData) {
		if(!ServerConfig.CONFIG.partiesEnabled.get())
			return;
		super.handle(serverData);
	}

	@Override
	public void preExpirationCheck(ServerParty party) {
	}

	@Override
	public boolean checkIfActive(ServerParty party) {
		return party.getOnlineMemberStream().findFirst().isPresent();
	}

	@Override
	public boolean expire(ServerParty party, IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData) {
		manager.removeTypedParty(party);
		onElementExpirationDone();
		return false;
	}

	public static final class Builder extends ObjectExpirationHandler.Builder<ServerParty, PartyManager, Builder>{

		private MinecraftServer server;

		public Builder setDefault() {
			super.setDefault();
			setServer(null);
			setCheckingMessage("Checking for expired parties...");
			setExpirationTime((long) ServerConfig.CONFIG.partyExpirationTime.get() * 60 * 60 * 1000);
			setLiveCheckInterval((long) ServerConfig.CONFIG.partyExpirationCheckInterval.get() * 60000);
			return this;
		}

		public Builder setServer(MinecraftServer server) {
			this.server = server;
			return this;
		}

		public PartyExpirationHandler build() {
			if(server == null)
				throw new IllegalStateException();
			return (PartyExpirationHandler) super.build();
		}

		public static Builder begin() {
			return new Builder().setDefault();
		}

		@Override
		protected ObjectExpirationHandler<ServerParty, PartyManager> buildInternally() {
			return new PartyExpirationHandler(serverInfo, server, manager, liveCheckInterval, expirationTime, checkingMessage);
		}

	}

}
