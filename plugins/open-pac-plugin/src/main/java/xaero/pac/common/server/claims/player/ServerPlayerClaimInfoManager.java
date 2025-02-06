package xaero.pac.common.server.claims.player;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.ModConfigSpec;
import xaero.pac.common.claims.player.PlayerClaimInfoManager;
import xaero.pac.common.claims.player.PlayerDimensionClaims;
import xaero.pac.common.server.claims.ServerClaimsManager;
import xaero.pac.common.server.claims.forceload.ForceLoadTicketManager;
import xaero.pac.common.server.claims.player.expiration.ServerPlayerClaimsExpirationHandler;
import xaero.pac.common.server.claims.player.io.PlayerClaimInfoManagerIO;
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.expiration.ObjectManagerIOExpirableObjectManager;
import xaero.pac.common.server.io.ObjectManagerIOManager;
import xaero.pac.common.server.player.config.IPlayerConfig;
import xaero.pac.common.server.player.config.IPlayerConfigManager;
import xaero.pac.common.server.player.permission.api.IPermissionNodeAPI;
import xaero.pac.common.server.player.permission.api.IPlayerPermissionSystemAPI;
import xaero.pac.common.util.linked.LinkedChain;

import java.util.*;

//only used by ServerClaimsManager
public final class ServerPlayerClaimInfoManager extends PlayerClaimInfoManager<ServerPlayerClaimInfo, ServerPlayerClaimInfoManager>
	implements ObjectManagerIOManager<ServerPlayerClaimInfo, ServerPlayerClaimInfoManager>, ObjectManagerIOExpirableObjectManager<ServerPlayerClaimInfo> {

	private final MinecraftServer server;
	private ServerClaimsManager claimsManager;
	private final IPlayerConfigManager configManager;
	private final ForceLoadTicketManager ticketManager;
	private final Set<ServerPlayerClaimInfo> toSave;
	private final Set<ResourceLocation> claimableDimensionsSet;
	private boolean loaded;
	private PlayerClaimInfoManagerIO<?> io;
	private ServerPlayerClaimsExpirationHandler expirationHandler;

	public ServerPlayerClaimInfoManager(MinecraftServer server, IPlayerConfigManager configManager, ForceLoadTicketManager ticketManager,
										Map<UUID, ServerPlayerClaimInfo> storage, LinkedChain<ServerPlayerClaimInfo> linkedPlayerInfo, Set<ServerPlayerClaimInfo> toSave) {
		super(storage, linkedPlayerInfo);
		this.server = server;
		this.configManager = configManager;
		this.ticketManager = ticketManager;
		this.toSave = toSave;
		claimableDimensionsSet = new HashSet<>();
		for(String s : ServerConfig.CONFIG.claimableDimensionsList.get())
			claimableDimensionsSet.add(ResourceLocation.parse(s));
	}

	public void setClaimsManager(ServerClaimsManager claimsManager) {
		if(this.claimsManager != null)
			throw new IllegalStateException();
		this.claimsManager = claimsManager;
	}

	public void setIo(PlayerClaimInfoManagerIO<?> io) {
		if(this.io != null)
			throw new IllegalStateException();
		this.io = io;
	}

	public void setExpirationHandler(ServerPlayerClaimsExpirationHandler expirationHandler) {
		if(this.expirationHandler != null)
			throw new IllegalStateException();
		this.expirationHandler = expirationHandler;
	}

	public boolean isClaimable(ResourceLocation dimension) {
		boolean contains = claimableDimensionsSet.contains(dimension);
		return ServerConfig.CONFIG.claimableDimensionsListType.get() == ServerConfig.ConfigListType.ONLY && contains || ServerConfig.CONFIG.claimableDimensionsListType.get() == ServerConfig.ConfigListType.ALL_BUT && !contains;
	}

	@Override
	public void addToSave(ServerPlayerClaimInfo object) {
		toSave.add(object);
	}

	@Override
	public Iterable<ServerPlayerClaimInfo> getToSave() {
		return toSave;
	}

	public ForceLoadTicketManager getTicketManager() {
		return ticketManager;
	}

	public boolean isLoaded() {
		return loaded;
	}

	public void onLoad() {
		loaded = true;
	}

	public IPlayerConfig getConfig(UUID playerId) {
		return configManager.getLoadedConfig(playerId);
	}

	@Override
	protected ServerPlayerClaimInfo create(String username, UUID playerId, Map<ResourceLocation, PlayerDimensionClaims> claims) {
		return new ServerPlayerClaimInfo(getConfig(playerId), username, playerId, claims, this, new ArrayDeque<>());
	}

	@Override
	protected void onAdd(ServerPlayerClaimInfo playerInfo) {
		super.onAdd(playerInfo);
		if(loaded)
			getClaimsManager().getClaimsManagerSynchronizer().syncToPlayersSubClaimPropertiesUpdate(getConfig(playerInfo.getPlayerId()));
	}

	@Override
	protected void onRemove(ServerPlayerClaimInfo playerInfo) {
		super.onRemove(playerInfo);
		io.delete(playerInfo);
		toSave.remove(playerInfo);
	}

	public int getPlayerBaseLimit(UUID playerId, ServerPlayer player, ModConfigSpec.IntValue limitConfig, IPermissionNodeAPI<Integer> permissionNode){
		IPlayerPermissionSystemAPI permissionSystem = claimsManager.getPermissionHandler().getSystem();
		int defaultLimit = limitConfig.get();
		if(permissionSystem == null)
			return defaultLimit;
		if(permissionNode == null || permissionNode.getNodeString().isEmpty())
			return defaultLimit;
		if(player == null)
			player = server.getPlayerList().getPlayer(playerId);
		if(player == null)
			return defaultLimit;
		return permissionSystem.getIntPermission(player, permissionNode).orElse(defaultLimit);
	}

	public ServerPlayerClaimsExpirationHandler getExpirationHandler() {
		return expirationHandler;
	}

	public ServerClaimsManager getClaimsManager() {
		return claimsManager;
	}

	@Override
	public Iterator<ServerPlayerClaimInfo> getExpirationIterator() {
		return iterator();
	}

}
