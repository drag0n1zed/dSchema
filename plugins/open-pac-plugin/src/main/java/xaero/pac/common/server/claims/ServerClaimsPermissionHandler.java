package xaero.pac.common.server.claims;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.packet.ClientboundModesPacket;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.player.data.ServerPlayerData;
import xaero.pac.common.server.player.data.api.ServerPlayerDataAPI;
import xaero.pac.common.server.player.permission.api.IPlayerPermissionSystemAPI;
import xaero.pac.common.server.player.permission.api.UsedPermissionNodes;

public class ServerClaimsPermissionHandler {

	private IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, ?> serverData;

	public boolean playerHasServerClaimPermission(ServerPlayer player){
		if(player.hasPermissions(2))
			return true;
		IPlayerPermissionSystemAPI permissionSystem = getSystem();
		if(permissionSystem == null)
			return false;
		return permissionSystem.getPermission(player, UsedPermissionNodes.SERVER_CLAIMS);
	}

	public boolean shouldPreventServerClaim(ServerPlayer player, ServerPlayerDataAPI playerData, MinecraftServer server){
		if(!playerHasServerClaimPermission(player)) {
			if (playerData.isClaimsServerMode()) {
				((ServerPlayerData)playerData).setClaimsServerMode(false);
				OpenPartiesAndClaims.INSTANCE.getPacketHandler().sendToPlayer(player, new ClientboundModesPacket(playerData.isClaimsAdminMode(), playerData.isClaimsServerMode()));
				server.getCommands().sendCommands(player);
			}
			return true;
		}
		return false;
	}

	public boolean playerHasAdminModePermission(ServerPlayer player){
		if(player.hasPermissions(2))
			return true;
		IPlayerPermissionSystemAPI permissionSystem = getSystem();
		if(permissionSystem == null)
			return false;
		return permissionSystem.getPermission(player, UsedPermissionNodes.ADMIN_MODE);
	}

	public void ensureAdminModeStatusPermission(ServerPlayer player, ServerPlayerDataAPI playerData){
		if(playerData.isClaimsAdminMode() && !playerHasAdminModePermission(player)) {
			((ServerPlayerData)playerData).setClaimsAdminMode(false);
			OpenPartiesAndClaims.INSTANCE.getPacketHandler().sendToPlayer(player, new ClientboundModesPacket(playerData.isClaimsAdminMode(), playerData.isClaimsServerMode()));
		}
	}

	public IPlayerPermissionSystemAPI getSystem() {
		return serverData.getPlayerPermissionSystemManager().getUsedSystem();
	}

	@SuppressWarnings("unchecked")
	public void setServerData(IServerData<?,?> serverData) {
		if(this.serverData != null)
			throw new IllegalAccessError();
		this.serverData = (IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, ?>) serverData;
	}
}
