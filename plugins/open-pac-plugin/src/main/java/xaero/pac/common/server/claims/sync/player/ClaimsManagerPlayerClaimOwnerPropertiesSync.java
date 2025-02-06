package xaero.pac.common.server.claims.sync.player;

import net.minecraft.server.level.ServerPlayer;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.packet.claims.ClientboundClaimOwnerPropertiesPacket;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.claims.player.ServerPlayerClaimInfo;
import xaero.pac.common.server.claims.sync.ClaimsManagerSynchronizer;
import xaero.pac.common.server.parties.party.IServerParty;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public final class ClaimsManagerPlayerClaimOwnerPropertiesSync extends ClaimsManagerPlayerLazyPacketScheduler {

	//no field for the player because this handler can be moved to another one (e.g. on respawn)
	private Iterator<ServerPlayerClaimInfo> toSync;

	private ClaimsManagerPlayerClaimOwnerPropertiesSync(Iterator<ServerPlayerClaimInfo> toSync, ClaimsManagerSynchronizer synchronizer) {
		super(synchronizer);
		this.toSync = toSync;
	}

	@Override
	public void onTick(IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData, ServerPlayer player, int limit){
		List<ClientboundClaimOwnerPropertiesPacket.PlayerProperties> packetBuilder = new ArrayList<>(ClientboundClaimOwnerPropertiesPacket.MAX_PROPERTIES);
		int canSync = limit;
		while(canSync > 0 && toSync.hasNext()) {
			buildClaimPropertiesPacket(packetBuilder, toSync.next(), player);
			canSync--;
		}
		if(!packetBuilder.isEmpty())
			synchronizer.syncClaimOwnerProperties(packetBuilder, player);
	}

	private void buildClaimPropertiesPacket(List<ClientboundClaimOwnerPropertiesPacket.PlayerProperties> packetBuilder, ServerPlayerClaimInfo pi, ServerPlayer player) {
		UUID playerId = pi.getPlayerId();
		String username = pi.getPlayerUsername();
		packetBuilder.add(new ClientboundClaimOwnerPropertiesPacket.PlayerProperties(playerId, username));
		if(packetBuilder.size() == ClientboundClaimOwnerPropertiesPacket.MAX_PROPERTIES) {
			synchronizer.syncClaimOwnerProperties(packetBuilder, player);
			packetBuilder.clear();
		}
	}

	@Override
	public void onLazyPacketsDropped() {
		toSync = null;
	}

	public boolean isFinished(){
		return toSync == null || !toSync.hasNext();
	}

	@Override
	public boolean shouldWorkNotClogged(IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData, ServerPlayer player) {
		return started && !isFinished();
	}

	public static final class Builder {

		private ClaimsManagerSynchronizer synchronizer;
		private ServerPlayer player;

		private Builder(){}

		public Builder setDefault() {
			setSynchronizer(null);
			setPlayer(null);
			return this;
		}

		public Builder setSynchronizer(ClaimsManagerSynchronizer synchronizer) {
			this.synchronizer = synchronizer;
			return this;
		}

		public Builder setPlayer(ServerPlayer player) {
			this.player = player;
			return this;
		}

		public ClaimsManagerPlayerClaimOwnerPropertiesSync build(){
			if(synchronizer == null || player == null)
				throw new IllegalStateException();
			Iterator<ServerPlayerClaimInfo> toSync = synchronizer.getClaimPropertiesToSync(player);
			return new ClaimsManagerPlayerClaimOwnerPropertiesSync(toSync, synchronizer);
		}

		public static Builder begin(){
			return new Builder().setDefault();
		}

	}

}
