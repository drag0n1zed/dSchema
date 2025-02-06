package xaero.pac.common.server;

import net.minecraft.server.MinecraftServer;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.lazypacket.LazyPacketSender;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.task.ServerSpreadoutTaskHandler;

import java.util.ArrayList;
import java.util.List;

public final class ServerTickHandler {

	private final LazyPacketSender lazyPacketSender;
	private final List<ServerSpreadoutTaskHandler<?,?>> spreadoutTaskHandlers;
	private long lastUseTimeUpdate;
	private long tickCounter;

	private ServerTickHandler(LazyPacketSender lazyPacketSender, List<ServerSpreadoutTaskHandler<?, ?>> spreadoutTaskHandlers) {
		this.lazyPacketSender = lazyPacketSender;
		this.spreadoutTaskHandlers = spreadoutTaskHandlers;
		this.lastUseTimeUpdate = System.currentTimeMillis();
	}

	public void onTick(IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData) throws Throwable {
		serverData.getIoThreadWorker().checkCrashes();
		serverData.getChunkProtection().onServerTick();
		serverData.getPartyManagerIO().onServerTick();
		serverData.getPlayerConfigsIO().onServerTick();


		@SuppressWarnings("unused")
		boolean hasSaved = //finish saving 1 live saver before starting with another
				serverData.getPartyLiveSaver().onServerTick() ||
				serverData.getPlayerConfigLiveSaver().onServerTick() ||
				serverData.getPlayerClaimInfoLiveSaver().onServerTick();

		lazyPacketSender.onServerTick();

		serverData.getPartyManager().getPartySynchronizer().onServerTick();
		serverData.getServerClaimsManager().getClaimsManagerSynchronizer().onServerTick();

		long time = System.currentTimeMillis();
		if(time - lastUseTimeUpdate > 600000/*10 minutes*/) {
			serverData.getServerInfo().setTotalUseTime(serverData.getServerInfo().getTotalUseTime() + (time - lastUseTimeUpdate));
			lastUseTimeUpdate = time;
			serverData.getServerInfoIO().save();
		}
		@SuppressWarnings("unused")
		boolean expirationCheck = serverData.getPartyExpirationHandler().onServerTick(serverData) || //prevents doing both on the same tick
									serverData.getServerPlayerClaimsExpirationHandler().onServerTick(serverData);

		spreadoutTaskHandlers.forEach(th -> th.onTick(serverData));

		tickCounter++;
	}

	public void registerSpreadoutTaskHandler(ServerSpreadoutTaskHandler<?,?> handler){
		if(!spreadoutTaskHandlers.contains(handler)) {
			spreadoutTaskHandlers.add(handler);
		}
	}

	public LazyPacketSender getLazyPacketSender() {
		return lazyPacketSender;
	}

	public long getTickCounter() {
		return tickCounter;
	}

	public static final class Builder {

		private MinecraftServer server;

		private Builder(){}

		public Builder setDefault(){
			setServer(null);
			return this;
		}

		public Builder setServer(MinecraftServer server) {
			this.server = server;
			return this;
		}

		public ServerTickHandler build(){
			if(server == null)
				throw new IllegalStateException();
			LazyPacketSender lazyPacketSender = LazyPacketSender.Builder.begin()
					.setServer(server)
					.setBytesPerTickLimit(104858 /*maximum ~2 MB per second*/)
					.setCapacity(104857600 /*~100 MB*/)
					.setBytesPerConfirmation(26214 * 20 /*~500 KB*/)
					.build();

			return new ServerTickHandler(lazyPacketSender, new ArrayList<>());
		}

		public static Builder begin(){
			return new Builder().setDefault();
		}

	}

}
