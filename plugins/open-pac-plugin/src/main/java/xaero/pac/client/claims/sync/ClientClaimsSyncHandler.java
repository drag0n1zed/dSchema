package xaero.pac.client.claims.sync;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.BitStorage;
import xaero.pac.client.claims.ClientClaimsManager;
import xaero.pac.common.claims.PlayerChunkClaimHolder;
import xaero.pac.common.claims.player.PlayerChunkClaim;
import xaero.pac.common.claims.result.api.AreaClaimResult;
import xaero.pac.common.claims.storage.RegionClaimsPaletteStorage;

import java.util.ArrayList;
import java.util.UUID;

public class ClientClaimsSyncHandler {

	private final ClientClaimsManager claimsManager;
	private ResourceLocation dimensionSyncing;

	private ResourceLocation lastClaimUpdateDimension;
	private PlayerChunkClaim lastClaimUpdateState;
	private int lastClaimUpdateX;
	private int lastClaimUpdateZ;

	public ClientClaimsSyncHandler(ClientClaimsManager claimsManager) {
		super();
		this.claimsManager = claimsManager;
	}

	public void onPlayerInfo(UUID playerId, String username) {
		claimsManager.getPlayerClaimInfoManager().updatePlayerInfo(playerId, username, claimsManager);
	}

	public void onSubClaimInfo(UUID playerId, int subConfigIndex, String claimsName, Integer claimsColor) {
		claimsManager.getPlayerClaimInfoManager().updateSubClaimInfo(playerId, subConfigIndex, claimsName, claimsColor, claimsManager);
	}

	public void onClaimState(PlayerChunkClaim claim) {
		claimsManager.addClaimState(claim);
	}

	public void onLoading(boolean start) {
		claimsManager.setLoading(start);
	}

	public void onClaimLimits(int loadingClaimCount, int loadingForceloadCount, int claimLimit,
			int forceloadLimit, int maxClaimDistance, boolean alwaysUseLoadingValues) {
		claimsManager.setLoadingClaimCount(loadingClaimCount);
		claimsManager.setLoadingForceloadCount(loadingForceloadCount);
		claimsManager.setClaimLimit(claimLimit);
		claimsManager.setForceloadLimit(forceloadLimit);
		claimsManager.setMaxClaimDistance(maxClaimDistance);
		claimsManager.setAlwaysUseLoadingValues(alwaysUseLoadingValues);
	}

	public void onSubConfigIndices(int currentSubConfigIndex, int currentServerSubConfigIndex, String currentSubConfigId, String currentServerSubConfigId){
		claimsManager.setCurrentSubConfigIndex(currentSubConfigIndex);
		claimsManager.setCurrentServerSubConfigIndex(currentServerSubConfigIndex);
		claimsManager.setCurrentSubConfigId(currentSubConfigId);
		claimsManager.setCurrentServerSubConfigId(currentServerSubConfigId);
	}

	public void onDimension(ResourceLocation dim) {
		this.dimensionSyncing = dim;
	}

	public void onClaimUpdate(ResourceLocation dimension, int x, int z, UUID playerId, int subConfigIndex, boolean forceload, int claimSyncIndex) {
		if(playerId != null) {
			if(claimsManager.getClaimStateBySyncIndex(claimSyncIndex) == null)
				claimsManager.addClaimState(new PlayerChunkClaim(playerId, subConfigIndex, forceload, claimSyncIndex));
			lastClaimUpdateState = claimsManager.claim(dimension, playerId, subConfigIndex, x, z, forceload);
		} else {
			claimsManager.unclaim(dimension, x, z);
			lastClaimUpdateState = null;
		}
		lastClaimUpdateDimension = dimension;
		lastClaimUpdateX = x;
		lastClaimUpdateZ = z;
	}

	public void onClaimUpdatePos(int x, int z) {
		if(lastClaimUpdateDimension == null)
			throw new IllegalStateException();
		if(lastClaimUpdateState != null)
			claimsManager.claim(lastClaimUpdateDimension, lastClaimUpdateState.getPlayerId(), lastClaimUpdateState.getSubConfigIndex(), x, z, lastClaimUpdateState.isForceloadable());
		else
			claimsManager.unclaim(lastClaimUpdateDimension, x, z);
		lastClaimUpdateX = x;
		lastClaimUpdateZ = z;
	}

	public void onClaimUpdateNextXPos() {
		onClaimUpdatePos(lastClaimUpdateX + 1, lastClaimUpdateZ);
	}

	public void onClaimUpdateNextZPos() {
		onClaimUpdatePos(lastClaimUpdateX, lastClaimUpdateZ + 1);
	}

	public void onRegion(int x, int z, int[] paletteInts, BitStorage storage) {
		Object2IntMap<PlayerChunkClaim> paletteHelper = new Object2IntOpenHashMap<>();
		ArrayList<PlayerChunkClaimHolder> palette = Lists.newArrayList((PlayerChunkClaimHolder)null);
		for(int i = 0; i < paletteInts.length; i++){
			PlayerChunkClaim claim = claimsManager.getClaimStateBySyncIndex(paletteInts[i]);
			if(claim != null)//can be null
				paletteHelper.put(claim, palette.size()/*not i*/);
			palette.add(new PlayerChunkClaimHolder(claim));//possible that the storage still points at a null (based on sync mode)
		}
		RegionClaimsPaletteStorage newRegionStorage = new RegionClaimsPaletteStorage(paletteHelper, null, palette, storage, false);
		newRegionStorage.setNeedsHolderRecalculation(true);//will calculate holder data when there is an attempt to modify the region
		claimsManager.claimRegion(dimensionSyncing, x, z, newRegionStorage);
	}

	public void onClaimResult(AreaClaimResult result) {
		claimsManager.getClaimResultTracker().onClaimResult(result);
	}

	public void onClaimModes(boolean adminMode, boolean serverMode) {
		claimsManager.setAdminMode(adminMode);
		claimsManager.setServerMode(serverMode);
	}

	public void onClaimStateRemoved(int syncIndex) {
		PlayerChunkClaim state = claimsManager.getClaimStateBySyncIndex(syncIndex);
		if(state != null)
			claimsManager.removeClaimState(state);
	}

	public void onRemoveSubClaim(UUID playerId, int subConfigIndex) {
		claimsManager.removeSubClaim(playerId, subConfigIndex);
	}

	public void reset(){
		dimensionSyncing = null;
		lastClaimUpdateState = null;
		lastClaimUpdateDimension = null;
	}

}
