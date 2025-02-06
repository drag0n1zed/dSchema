package xaero.pac.common.server.claims;

import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import xaero.pac.common.claims.ClaimsManager;
import xaero.pac.common.claims.player.PlayerChunkClaim;
import xaero.pac.common.claims.result.api.AreaClaimResult;
import xaero.pac.common.claims.result.api.ClaimResult;
import xaero.pac.common.claims.tracker.ClaimsManagerTracker;
import xaero.pac.common.server.claims.forceload.ForceLoadTicketManager;
import xaero.pac.common.server.claims.player.ServerPlayerClaimInfo;
import xaero.pac.common.server.claims.player.ServerPlayerClaimInfoManager;
import xaero.pac.common.server.claims.player.expiration.ServerPlayerClaimsExpirationHandler;
import xaero.pac.common.server.claims.player.io.PlayerClaimInfoManagerIO;
import xaero.pac.common.server.claims.player.task.PlayerClaimReplaceSpreadoutTask;
import xaero.pac.common.server.claims.sync.ClaimsManagerSynchronizer;
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.player.config.IPlayerConfigManager;
import xaero.pac.common.server.player.config.PlayerConfig;
import xaero.pac.common.server.player.config.api.PlayerConfigOptions;
import xaero.pac.common.server.player.permission.api.UsedPermissionNodes;
import xaero.pac.common.server.task.ServerSpreadoutQueuedTaskHandler;
import xaero.pac.common.util.linked.LinkedChain;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public final class ServerClaimsManager extends ClaimsManager<ServerPlayerClaimInfo, ServerPlayerClaimInfoManager, ServerRegionClaims, ServerDimensionClaimsManager, ServerClaimStateHolder> implements IServerClaimsManager<PlayerChunkClaim, ServerPlayerClaimInfo, ServerDimensionClaimsManager> {

	private final int MAX_REQUEST_SIZE = 100;//will go 1 chunk beyond this before cancelling but that's fine
	private final ClaimsManagerSynchronizer claimsManagerSynchronizer;
	private final ServerSpreadoutQueuedTaskHandler<PlayerClaimReplaceSpreadoutTask> claimReplaceTaskHandler;
	private final ServerClaimsPermissionHandler permissionHandler;
	private final LinkedChain<ServerClaimStateHolder> linkedClaimStates;
	private boolean loaded;

	protected ServerClaimsManager(MinecraftServer server, ServerPlayerClaimInfoManager playerClaimInfoManager,
								  IPlayerConfigManager configManager, Map<ResourceLocation, ServerDimensionClaimsManager> dimensions,
								  ClaimsManagerSynchronizer claimsManagerSynchronizer, Int2ObjectMap<PlayerChunkClaim> indexToClaimState,
								  Map<PlayerChunkClaim, ServerClaimStateHolder> claimStates, ClaimsManagerTracker claimsManagerTracker, ServerSpreadoutQueuedTaskHandler<PlayerClaimReplaceSpreadoutTask> claimReplaceTaskHandler, ServerClaimsPermissionHandler permissionHandler, LinkedChain<ServerClaimStateHolder> linkedClaimStates) {
		super(playerClaimInfoManager, configManager, dimensions, indexToClaimState, claimStates, claimsManagerTracker);
		this.claimsManagerSynchronizer = claimsManagerSynchronizer;
		this.claimReplaceTaskHandler = claimReplaceTaskHandler;
		this.permissionHandler = permissionHandler;
		this.linkedClaimStates = linkedClaimStates;
	}

	public void setIo(PlayerClaimInfoManagerIO<?> io) {
		this.playerClaimInfoManager.setIo(io);
	}

	public void setExpirationHandler(ServerPlayerClaimsExpirationHandler expirationHandler) {
		this.playerClaimInfoManager.setExpirationHandler(expirationHandler);
	}

	public ServerPlayerClaimsExpirationHandler.Builder beginExpirationHandlerBuilder() {
		return ServerPlayerClaimsExpirationHandler.Builder.begin().setManager(playerClaimInfoManager).setClaimsManager(this);
	}

	@Override
	protected ServerDimensionClaimsManager create(ResourceLocation dimension,
												  Long2ObjectMap<ServerRegionClaims> claims) {
		boolean playerClaimsSyncAllowed = ServerConfig.CONFIG.allowExistingClaimsInUnclaimableDimensions.get() || isClaimable(dimension);
		return new ServerDimensionClaimsManager(dimension, claims, new LinkedChain<>(), this, playerClaimsSyncAllowed);
	}

	@Override
	protected ServerClaimStateHolder createStateHolder(PlayerChunkClaim claim) {
		return new ServerClaimStateHolder(claim);
	}

	public long countStateRegions(PlayerChunkClaim state, int direction) {
		ServerClaimStateHolder stateHolder = claimStateHolders.get(state);
		stateHolder.countRegions(direction);
		if (stateHolder.getRegionCount() <= 0)
			removeClaimState(state);
		return stateHolder.getRegionCount();
	}

	private boolean withinDistance(int fromX, int fromZ, int x, int z) {
		int maxClaimDistance = ServerConfig.CONFIG.maxClaimDistance.get();
		return Math.abs(x - fromX) <= maxClaimDistance && Math.abs(z - fromZ) <= maxClaimDistance;
	}

	@Override
	public boolean isClaimable(@Nonnull ResourceLocation dimension) {
		return playerClaimInfoManager.isClaimable(dimension);
	}

	@Override
	protected void onClaimStateAdded(ServerClaimStateHolder stateHolder) {
		linkedClaimStates.add(stateHolder);
	}

	@Override
	protected void removeClaimState(PlayerChunkClaim state) {
		linkedClaimStates.remove(claimStateHolders.get(state));
		super.removeClaimState(state);
		//removal is synced because the client can't know when to remove states while the initial sync is in progress
		claimsManagerSynchronizer.syncToPlayersRemoveClaimState(state);
	}

	@Nullable
	@Override
	public PlayerChunkClaim claim(@Nonnull ResourceLocation dimension, @Nonnull UUID id, int subConfigIndex, int x, int z, boolean forceload) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return null;
		PlayerChunkClaim result = super.claim(dimension, id, subConfigIndex, x, z, forceload);
		if(loaded)
			claimsManagerTracker.onChunkChange(dimension, x, z, result);
		return result;
	}

	@Override
	public void unclaim(@Nonnull ResourceLocation dimension, int x, int z) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return;
		super.unclaim(dimension, x, z);
		if(loaded)
			claimsManagerTracker.onChunkChange(dimension, x, z, null);
	}

	private ClaimResult<PlayerChunkClaim> tryToClaimHelper(ResourceLocation dimension, UUID playerId, int subConfigIndex, int fromX, int fromZ, int x, int z, boolean forceLoaded, boolean replace, boolean isServer) {
		PlayerChunkClaim currentClaim = get(dimension, x, z);
		boolean claimCountUnaffected = false;
		if(currentClaim != null) {
			claimCountUnaffected = Objects.equals(currentClaim.getPlayerId(), playerId);
			if(!replace && !claimCountUnaffected)
				return new ClaimResult<>(currentClaim, ClaimResult.Type.ALREADY_CLAIMED);
		}
		ServerPlayerClaimInfo playerClaimInfo = getPlayerInfo(playerId);
		boolean withinLimit = claimCountUnaffected || isServer ||
				playerClaimInfo.getClaimCount() < getPlayerBaseClaimLimit(playerId) + configManager.getLoadedConfig(playerId).getEffective(PlayerConfigOptions.BONUS_CHUNK_CLAIMS);
		if(withinLimit) {
			PlayerChunkClaim claim = new PlayerChunkClaim(playerId, subConfigIndex, forceLoaded, 0);
			if(Objects.equals(claim, currentClaim))
				return new ClaimResult<>(currentClaim, ClaimResult.Type.ALREADY_CLAIMED);
			PlayerChunkClaim c = claim(dimension, claim.getPlayerId(), subConfigIndex, x, z, claim.isForceloadable());
			return new ClaimResult<>(c, Objects.equals(c, claim) ? ClaimResult.Type.SUCCESSFUL_CLAIM : ClaimResult.Type.CLAIM_LIMIT_REACHED);//forceload limit
		} else {
			return new ClaimResult<>(currentClaim, ClaimResult.Type.CLAIM_LIMIT_REACHED);
		}
	}

	@Nonnull
	@Override
	public ClaimResult<PlayerChunkClaim> tryToClaimTyped(@Nonnull ResourceLocation dimension, @Nonnull UUID playerId, int subConfigIndex, int fromX, int fromZ, int x, int z, boolean replace) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return new ClaimResult<>(null, ClaimResult.Type.CLAIMS_ARE_DISABLED);
		if(!replace && getPlayerInfo(playerId).isReplacementInProgress())
			return new ClaimResult<>(null, ClaimResult.Type.REPLACEMENT_IN_PROGRESS);
		boolean isServer = Objects.equals(playerId, PlayerConfig.SERVER_CLAIM_UUID);
		if(!isServer && !isClaimable(dimension))
			return new ClaimResult<>(null, ClaimResult.Type.UNCLAIMABLE_DIMENSION);
		if(!replace && !withinDistance(fromX, fromZ, x, z))
			return new ClaimResult<>(null, ClaimResult.Type.TOO_FAR);
		return tryToClaimHelper(dimension, playerId, subConfigIndex, fromX, fromZ, x, z, false, replace, isServer);
	}

	private ClaimResult<PlayerChunkClaim> tryToUnclaimHelper(ResourceLocation dimension, UUID id, int fromX, int fromZ, int x, int z, boolean replace) {
		PlayerChunkClaim currentClaim = get(dimension, x, z);
		if(currentClaim == null || !replace && !Objects.equals(id, currentClaim.getPlayerId()))
			return new ClaimResult<>(currentClaim, ClaimResult.Type.NOT_CLAIMED_BY_USER);
	 	unclaim(dimension, x, z);
	 	return new ClaimResult<>(null, ClaimResult.Type.SUCCESSFUL_UNCLAIM);
	}

	@Nonnull
	@Override
	public ClaimResult<PlayerChunkClaim> tryToUnclaimTyped(@Nonnull ResourceLocation dimension, @Nonnull UUID id, int fromX, int fromZ, int x, int z, boolean replace) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return new ClaimResult<>(null, ClaimResult.Type.CLAIMS_ARE_DISABLED);
		//boolean isServer = Objects.equals(id, PlayerConfig.SERVER_CLAIM_UUID);
		if(!replace && !withinDistance(fromX, fromZ, x, z))
			return new ClaimResult<>(null, ClaimResult.Type.TOO_FAR);
		return tryToUnclaimHelper(dimension, id, fromX, fromZ, x, z, replace);
	}

	private ClaimResult<PlayerChunkClaim> tryToForceloadHelper(ResourceLocation dimension, UUID id, int fromX, int fromZ, int x, int z, boolean enable, boolean replace, boolean isServer) {
		PlayerChunkClaim currentClaim = get(dimension, x, z);
		if(currentClaim != null && (replace || Objects.equals(currentClaim.getPlayerId(), id))) {
			if(currentClaim.isForceloadable() == enable)
				return new ClaimResult<>(currentClaim, enable ? ClaimResult.Type.ALREADY_FORCELOADABLE : ClaimResult.Type.ALREADY_UNFORCELOADED);
			ServerPlayerClaimInfo playerClaimInfo = getPlayerInfo(id);
			boolean withinLimit = isServer || !enable ||
					playerClaimInfo.getForceloadCount() < getPlayerBaseForceloadLimit(id) + configManager.getLoadedConfig(id).getEffective(PlayerConfigOptions.BONUS_CHUNK_FORCELOADS);
			if(!withinLimit)
				return new ClaimResult<>(currentClaim, ClaimResult.Type.FORCELOAD_LIMIT_REACHED);

			ClaimResult<PlayerChunkClaim> result = tryToClaimHelper(dimension, currentClaim.getPlayerId(), currentClaim.getSubConfigIndex(), fromX, fromZ, x, z, enable, true, isServer);
			if(result.getResultType() == ClaimResult.Type.SUCCESSFUL_CLAIM)
				return new ClaimResult<>(result.getClaimResult(), enable ? ClaimResult.Type.SUCCESSFUL_FORCELOAD : ClaimResult.Type.SUCCESSFUL_UNFORCELOAD);
//			else if(result.getResultType() == ClaimResult.Type.CLAIM_LIMIT_REACHED)
//				return new ClaimResult<>(result.getClaimResult(), ClaimResult.Type.FORCELOAD_LIMIT_REACHED);
			else
				return result;
		} else
		 	return new ClaimResult<>(currentClaim, ClaimResult.Type.NOT_CLAIMED_BY_USER_FORCELOAD);
	}

	@Nonnull
	@Override
	public ClaimResult<PlayerChunkClaim> tryToForceloadTyped(@Nonnull ResourceLocation dimension, @Nonnull UUID id, int fromX, int fromZ, int x, int z, boolean enable, boolean replace) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return new ClaimResult<>(null, ClaimResult.Type.CLAIMS_ARE_DISABLED);
		boolean isServer = Objects.equals(id, PlayerConfig.SERVER_CLAIM_UUID);
		if(enable && !isServer && !isClaimable(dimension))
			return new ClaimResult<>(null, ClaimResult.Type.UNCLAIMABLE_DIMENSION);
		if(!replace && !withinDistance(fromX, fromZ, x, z))
			return new ClaimResult<>(null, ClaimResult.Type.TOO_FAR);
		return tryToForceloadHelper(dimension, id, fromX, fromZ, x, z, enable, replace, isServer);
	}

	public AreaClaimResult tryClaimActionOverArea(ResourceLocation dimension, UUID playerId, int subConfigIndex, int fromX, int fromZ, int left, int top, int right, int bottom, Action action, boolean replace) {
		if(!ServerConfig.CONFIG.claimsEnabled.get())
			return new AreaClaimResult(Sets.newHashSet(ClaimResult.Type.CLAIMS_ARE_DISABLED), left, top, right, bottom);
		Set<ClaimResult.Type> resultTypes = new HashSet<>();
		boolean isServer = Objects.equals(playerId, PlayerConfig.SERVER_CLAIM_UUID);
		if(!isServer && (action == Action.CLAIM || action == Action.FORCELOAD) && !isClaimable(dimension)) {
			resultTypes.add(ClaimResult.Type.UNCLAIMABLE_DIMENSION);
			return new AreaClaimResult(resultTypes, left, top, right, bottom);
		}
		int effectiveLeft = left;
		int effectiveTop = top;
		int effectiveRight = right;
		int effectiveBottom = bottom;
		if(!replace) {
			int maxClaimDistance = ServerConfig.CONFIG.maxClaimDistance.get();
			boolean outOfBounds = false;
			if(effectiveLeft < fromX - maxClaimDistance) {
				effectiveLeft = fromX - maxClaimDistance;
				outOfBounds = true;
			}
			if(effectiveTop < fromZ - maxClaimDistance) {
				effectiveTop = fromZ - maxClaimDistance;
				outOfBounds = true;
			}
			if(effectiveRight > fromX + maxClaimDistance) {
				effectiveRight = fromX + maxClaimDistance;
				outOfBounds = true;
			}
			if(effectiveBottom > fromZ + maxClaimDistance) {
				effectiveBottom = fromZ + maxClaimDistance;
				outOfBounds = true;
			}
			if(outOfBounds)
				resultTypes.add(ClaimResult.Type.TOO_FAR);
		}

		int maxRequestLength = 32;
		if(effectiveRight - effectiveLeft >= maxRequestLength)
			effectiveRight = effectiveLeft + maxRequestLength - 1;
		if(effectiveBottom - effectiveTop >= maxRequestLength)
			effectiveBottom = effectiveTop + maxRequestLength - 1;

		int total;
		if(effectiveLeft > effectiveRight || effectiveTop > effectiveBottom)
			total = 0;
		else
			total = (1 + effectiveRight - effectiveLeft) * (1 + effectiveBottom - effectiveTop);
		int toAffect = total;
		if(total > MAX_REQUEST_SIZE)
			toAffect = MAX_REQUEST_SIZE;
		outer:
		for(int x = effectiveLeft; x <= effectiveRight; x++)
			for(int z = effectiveTop; z <= effectiveBottom; z++) {
				ClaimResult<PlayerChunkClaim> result = null;
				if(action == Action.CLAIM)
					result = tryToClaimHelper(dimension, playerId, subConfigIndex, fromX, fromZ, x, z, false, replace, isServer);
				else if(action == Action.UNCLAIM)
					result = tryToUnclaimHelper(dimension, playerId, fromX, fromZ, x, z, replace);
				else if(action == Action.FORCELOAD)
					result = tryToForceloadHelper(dimension, playerId, fromX, fromZ, x, z, true, replace, isServer);
				else if(action == Action.UNFORCELOAD)
					result = tryToForceloadHelper(dimension, playerId, fromX, fromZ, x, z, false, replace, isServer);
				else
					break outer;
				resultTypes.add(result.getResultType());
				if(result.getResultType().success) {
					if(toAffect <= 0) {
						resultTypes.add(ClaimResult.Type.TOO_MANY_CHUNKS);
						break outer;
					} else
						toAffect--;
				}
				if(result.getResultType() == ClaimResult.Type.CLAIM_LIMIT_REACHED ||
						result.getResultType() == ClaimResult.Type.FORCELOAD_LIMIT_REACHED ||
						result.getResultType() == ClaimResult.Type.REPLACEMENT_IN_PROGRESS
				)
					break outer;
			}
		return new AreaClaimResult(resultTypes, left, top, right, bottom);
	}

	@Nonnull
	@Override
	public AreaClaimResult tryToClaimArea(@Nonnull ResourceLocation dimension, @Nonnull UUID playerId, int subConfigIndex, int fromX, int fromZ, int left, int top, int right, int bottom, boolean replace) {
		return tryClaimActionOverArea(dimension, playerId, subConfigIndex, fromX, fromZ, left, top, right, bottom, Action.CLAIM, replace);
	}

	@Nonnull
	@Override
	public AreaClaimResult tryToUnclaimArea(@Nonnull ResourceLocation dimension, @Nonnull UUID id, int fromX, int fromZ, int left, int top, int right, int bottom, boolean replace) {
		return tryClaimActionOverArea(dimension, id, -1, fromX, fromZ, left, top, right, bottom, Action.UNCLAIM, replace);
	}

	@Nonnull
	@Override
	public AreaClaimResult tryToForceloadArea(@Nonnull ResourceLocation dimension, @Nonnull UUID id, int fromX, int fromZ, int left, int top, int right, int bottom, boolean enable, boolean replace) {
		return tryClaimActionOverArea(dimension, id, -1, fromX, fromZ, left, top, right, bottom, enable ? Action.FORCELOAD : Action.UNFORCELOAD, replace);
	}

	@Nullable
	@Override
	public PlayerChunkClaim get(@Nonnull ResourceLocation dimension, int x, int z) {
		PlayerChunkClaim actualClaim = super.get(dimension, x, z);
		//allowExistingClaimsInUnclaimableDimensions is applied here, not when loading the files, so that new changes to claims still affect the "ignored" claims, e.g. when a server claims a chunk claimed by player
		if(actualClaim == null || ServerConfig.CONFIG.allowExistingClaimsInUnclaimableDimensions.get() || Objects.equals(actualClaim.getPlayerId(), PlayerConfig.SERVER_CLAIM_UUID) || Objects.equals(actualClaim.getPlayerId(), PlayerConfig.EXPIRED_CLAIM_UUID) || isClaimable(dimension))
			return actualClaim;
		else
			return null;
	}

	@Override
	public int getPlayerBaseClaimLimit(@Nonnull UUID playerId){
		return playerClaimInfoManager.getPlayerBaseLimit(playerId, null, ServerConfig.CONFIG.maxPlayerClaims, UsedPermissionNodes.MAX_PLAYER_CLAIMS);
	}

	@Override
	public int getPlayerBaseForceloadLimit(@Nonnull UUID playerId){
		return playerClaimInfoManager.getPlayerBaseLimit(playerId, null, ServerConfig.CONFIG.maxPlayerClaimForceloads, UsedPermissionNodes.MAX_PLAYER_FORCELOADS);
	}

	@Override
	public int getPlayerBaseClaimLimit(@Nonnull ServerPlayer player){
		return playerClaimInfoManager.getPlayerBaseLimit(null, player, ServerConfig.CONFIG.maxPlayerClaims, UsedPermissionNodes.MAX_PLAYER_CLAIMS);
	}

	@Override
	public int getPlayerBaseForceloadLimit(@Nonnull ServerPlayer player){
		return playerClaimInfoManager.getPlayerBaseLimit(null, player, ServerConfig.CONFIG.maxPlayerClaimForceloads, UsedPermissionNodes.MAX_PLAYER_FORCELOADS);
	}

	public Iterator<ServerClaimStateHolder> getClaimStateHolderIterator(){
		return linkedClaimStates.iterator();
	}

	@Override
	public ClaimsManagerSynchronizer getClaimsManagerSynchronizer() {
		return claimsManagerSynchronizer;
	}

	@Override
	public ServerSpreadoutQueuedTaskHandler<PlayerClaimReplaceSpreadoutTask> getClaimReplaceTaskHandler() {
		return claimReplaceTaskHandler;
	}

	@Override
	public ServerClaimsPermissionHandler getPermissionHandler() {
		return permissionHandler;
	}

	public boolean isLoaded() {
		return loaded;
	}

	public void onLoad() {
		loaded = true;
	}

	public final static class Builder extends ClaimsManager.Builder<ServerPlayerClaimInfo, ServerPlayerClaimInfoManager, ServerRegionClaims, ServerDimensionClaimsManager, ServerClaimStateHolder, Builder>{

		private MinecraftServer server;
		private IPlayerConfigManager configManager;
		private ForceLoadTicketManager ticketManager;
		private ClaimsManagerSynchronizer claimsManagerSynchronizer;
		private ServerSpreadoutQueuedTaskHandler<PlayerClaimReplaceSpreadoutTask> claimReplaceTaskHandler;
		private ServerClaimsPermissionHandler permissionHandler;

		public static Builder begin() {
			return new Builder().setDefault();
		}

		@Override
		public Builder setDefault() {
			super.setDefault();
			setServer(null);
			setTicketManager(null);
			setClaimsManagerSynchronizer(null);
			setConfigManager(null);
			return this;
		}

		public Builder setServer(MinecraftServer server) {
			this.server = server;
			return this;
		}

		public Builder setTicketManager(ForceLoadTicketManager ticketManager) {
			this.ticketManager = ticketManager;
			return this;
		}

		public Builder setClaimsManagerSynchronizer(ClaimsManagerSynchronizer claimsManagerSynchronizer) {
			this.claimsManagerSynchronizer = claimsManagerSynchronizer;
			return this;
		}

		public Builder setClaimReplaceTaskHandler(ServerSpreadoutQueuedTaskHandler<PlayerClaimReplaceSpreadoutTask> claimReplaceTaskHandler) {
			this.claimReplaceTaskHandler = claimReplaceTaskHandler;
			return this;
		}

		public Builder setConfigManager(IPlayerConfigManager configManager) {
			this.configManager = configManager;
			return self;
		}

		public Builder setPermissionHandler(ServerClaimsPermissionHandler permissionHandler) {
			this.permissionHandler = permissionHandler;
			return self;
		}

		@Override
		public ServerClaimsManager build() {
			if(server == null || ticketManager == null || claimsManagerSynchronizer == null || configManager == null || claimReplaceTaskHandler == null || permissionHandler == null)
				throw new IllegalStateException();
			ServerPlayerClaimInfoManager playerInfoManager = new ServerPlayerClaimInfoManager(server, configManager, ticketManager, new HashMap<>(), new LinkedChain<>(), new HashSet<>());
			setPlayerClaimInfoManager(playerInfoManager);
			ServerClaimsManager result = (ServerClaimsManager) super.build();
			playerInfoManager.setClaimsManager(result);
			claimsManagerSynchronizer.setClaimsManager(result);

			result.getPlayerInfo(PlayerConfig.SERVER_CLAIM_UUID).setPlayerUsername("\"Server\"");
			result.getPlayerInfo(PlayerConfig.EXPIRED_CLAIM_UUID).setPlayerUsername("\"Expiration\"");
			return result;
		}

		@Override
		protected ServerClaimsManager buildInternally(Map<PlayerChunkClaim, ServerClaimStateHolder> claimStates, ClaimsManagerTracker claimsManagerTracker, Int2ObjectMap<PlayerChunkClaim> indexToClaimState) {
			LinkedChain<ServerClaimStateHolder> linkedClaimStates = new LinkedChain<>();
			claimStates.values().forEach(linkedClaimStates::add);
			return new ServerClaimsManager(server, playerClaimInfoManager, configManager, dimensions, claimsManagerSynchronizer, indexToClaimState, claimStates, claimsManagerTracker, claimReplaceTaskHandler, permissionHandler, linkedClaimStates);
		}

	}

}
