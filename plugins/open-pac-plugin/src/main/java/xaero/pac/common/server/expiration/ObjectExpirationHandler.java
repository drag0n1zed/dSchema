package xaero.pac.common.server.expiration;

import xaero.pac.OpenPartiesAndClaims;
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
import xaero.pac.common.server.expiration.task.ObjectExpirationCheckSpreadoutTask;
import xaero.pac.common.server.info.ServerInfo;
import xaero.pac.common.server.io.ObjectManagerIOManager;
import xaero.pac.common.server.parties.party.IServerParty;

import java.util.Iterator;

public abstract class ObjectExpirationHandler
<
	T extends ObjectManagerIOExpirableObject,
	M extends ObjectManagerIOManager<T, M> & ObjectManagerIOExpirableObjectManager<T>
> {

	private final ServerInfo serverInfo;
	protected final M manager;
	private final long liveCheckInterval;
	private long lastCheck;
	private Iterator<T> checkingIterator;
	private final String checkingMessage;
	private final long expirationTime;
	private boolean expiringAnElement;

	protected ObjectExpirationHandler(ServerInfo serverInfo, M manager,
			long liveCheckInterval, long expirationTime, String checkingMessage) {
		super();
		this.serverInfo = serverInfo;
		this.manager = manager;
		this.liveCheckInterval = liveCheckInterval;
		this.expirationTime = expirationTime;
		this.checkingMessage = checkingMessage;
		this.lastCheck = serverInfo.getTotalUseTime() - liveCheckInterval - 1;//forces a check on server start which is important for servers that restart often
	}

	public abstract void preExpirationCheck(T object);
	public abstract boolean checkIfActive(T object);
	public abstract boolean expire(T object, IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData);

	protected void handle(IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData) {
		checkingIterator = manager.getExpirationIterator();

		lastCheck = serverInfo.getTotalUseTime();
		OpenPartiesAndClaims.LOGGER.debug(checkingMessage);
		serverData.getObjectExpirationCheckTaskHandler().addTask(new ObjectExpirationCheckSpreadoutTask<>(this, checkingIterator), serverData);
	}

	public void onElementExpirationBegin(){
		expiringAnElement = true;
	}

	public void onElementExpirationDone() {
		expiringAnElement = false;
	}

	public boolean isExpiringAnElement() {
		return expiringAnElement;
	}

	public void onIterationDone(){
		OpenPartiesAndClaims.LOGGER.debug("Expiration iteration has finished!");
		checkingIterator = null;
	}

	public boolean onServerTick(IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData) {
		if(checkingIterator != null)
			return true;
		if(serverInfo.getTotalUseTime() - lastCheck > liveCheckInterval) {
			handle(serverData);
			return true;
		}
		return false;
	}

	public ServerInfo getServerInfo() {
		return serverInfo;
	}

	public long getExpirationTime() {
		return expirationTime;
	}

	public static abstract class Builder
	<
		T extends ObjectManagerIOExpirableObject,
		M extends ObjectManagerIOManager<T, M> & ObjectManagerIOExpirableObjectManager<T>,
		B extends Builder<T, M, B>
	> {

		protected final B self;
		protected ServerInfo serverInfo;
		protected M manager;
		protected long liveCheckInterval;
		protected String checkingMessage;
		protected long expirationTime;

		@SuppressWarnings("unchecked")
		protected Builder() {
			this.self = (B) this;
		}

		public B setDefault() {
			setServerInfo(null);
			setManager(null);
			setLiveCheckInterval(0);
			setCheckingMessage(null);
			setExpirationTime(0);
			return self;
		}

		public B setServerInfo(ServerInfo serverInfo) {
			this.serverInfo = serverInfo;
			return self;
		}

		public B setManager(M manager) {
			this.manager = manager;
			return self;
		}

		public B setLiveCheckInterval(long liveCheckInterval) {
			this.liveCheckInterval = liveCheckInterval;
			return self;
		}

		public B setCheckingMessage(String checkingMessage) {
			this.checkingMessage = checkingMessage;
			return self;
		}

		public B setExpirationTime(long expirationTime) {
			this.expirationTime = expirationTime;
			return self;
		}

		public ObjectExpirationHandler<T, M> build() {
			if(serverInfo == null || manager == null || liveCheckInterval <= 0 || checkingMessage == null || expirationTime <= 0)
				throw new IllegalStateException();
			return buildInternally();
		}

		protected abstract ObjectExpirationHandler<T, M> buildInternally();

	}

}
