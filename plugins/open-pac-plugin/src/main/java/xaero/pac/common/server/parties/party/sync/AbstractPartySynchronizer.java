package xaero.pac.common.server.parties.party.sync;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.Party;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.parties.party.member.PartyMember;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.lazypacket.LazyPacket;
import xaero.pac.common.server.parties.party.IServerParty;

import java.util.Iterator;
import java.util.function.Predicate;

public abstract class AbstractPartySynchronizer {

	protected final MinecraftServer server;
	protected IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData;

	public AbstractPartySynchronizer(MinecraftServer server) {
		super();
		this.server = server;
	}

	@SuppressWarnings("unchecked")
	public void setServerData(IServerData<?,?> serverData) {
		if(this.serverData != null)
			throw new IllegalAccessError();
		this.serverData = (IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>) serverData;
	}

	protected void sendToClient(ServerPlayer player, Object packet, boolean instant) {
		if(instant)
			OpenPartiesAndClaims.INSTANCE.getPacketHandler().sendToPlayer(player, packet);
		else
			serverData.getServerTickHandler().getLazyPacketSender().enqueue(player, (LazyPacket<?>) packet);
	}

	protected void instantSendToClient(ServerPlayer player, Object packet) {
	}

	private void sendToMember(PartyMember mi, Predicate<IPartyPlayerInfo> exception, PlayerList playerList, Object packet, boolean instant){
		if(exception.test(mi))
			return;
		ServerPlayer onlinePlayer = playerList.getPlayer(mi.getUUID());
		if(onlinePlayer != null)
			sendToClient(onlinePlayer, packet, instant);
	}

	protected void syncToParty(Party party, Predicate<IPartyPlayerInfo> exception, Object packet, boolean instant) {
		PlayerList playerList = server.getPlayerList();
		//iterates over the smaller count
		if(playerList.getPlayerCount() > party.getMemberCount()) {
			Iterator<PartyMember> iterator = party.getTypedMemberInfoStream().iterator();
			while(iterator.hasNext())
				sendToMember(iterator.next(), exception, playerList, packet, instant);
		} else {
			for (ServerPlayer onlinePlayer : playerList.getPlayers()) {
				IPartyMember memberInfo = party.getMemberInfo(onlinePlayer.getUUID());
				if(memberInfo != null && !exception.test(memberInfo))
					sendToClient(onlinePlayer, packet, instant);
			}
		}
	}

}
