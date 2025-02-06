package xaero.pac.common.server.claims.command;

import com.google.common.collect.Sets;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.claims.result.api.AreaClaimResult;
import xaero.pac.common.claims.result.api.ClaimResult;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.ServerData;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.claims.sync.ClaimsManagerSynchronizer;
import xaero.pac.common.server.command.CommandRequirementHelper;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.config.IPlayerConfig;
import xaero.pac.common.server.player.config.PlayerConfig;
import xaero.pac.common.server.player.data.ServerPlayerData;
import xaero.pac.common.server.player.data.api.ServerPlayerDataAPI;
import xaero.pac.common.server.player.localization.AdaptiveLocalizer;

import java.util.UUID;
import java.util.function.Predicate;

public class ClaimsClaimCommands {

	protected static ArgumentBuilder<CommandSourceStack, ?> createClaimCommand(ArgumentBuilder<CommandSourceStack, ?> builder, boolean shouldClaim, boolean serverClaim, boolean opReplaceCurrent){
		return builder
			.executes(context -> {
				ServerPlayer player = context.getSource().getPlayerOrException();
				ServerLevel world = player.serverLevel();
				int chunkX = player.chunkPosition().x;
				int chunkZ = player.chunkPosition().z;
				try {
					ColumnPos columnPos = ColumnPosArgument.getColumnPos(context, "block pos");
					chunkX = columnPos.x() >> 4;
					chunkZ = columnPos.z() >> 4;
				} catch(IllegalArgumentException iae) {
				}

				MinecraftServer server = context.getSource().getServer();
				IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(server);
				ServerPlayerData playerData = (ServerPlayerData) ServerPlayerDataAPI.from(player);
				AdaptiveLocalizer adaptiveLocalizer = serverData.getAdaptiveLocalizer();
				boolean shouldServerClaim = serverClaim;
				if(playerData.isClaimsServerMode())
					shouldServerClaim = true;
				if(shouldServerClaim && serverData.getServerClaimsManager().getPermissionHandler().shouldPreventServerClaim(player, playerData, server)){
					context.getSource().sendFailure(adaptiveLocalizer.getFor(player, "gui.xaero_claims_claim_no_server_permission"));
					return 0;
				}
				UUID playerId = shouldServerClaim ? PlayerConfig.SERVER_CLAIM_UUID : player.getUUID();

				if(serverData.getServerTickHandler().getTickCounter() == playerData.getClaimActionRequestHandler().getLastRequestTickCounter())
					return 0;//going too fast
				playerData.getClaimActionRequestHandler().setLastRequestTickCounter(serverData.getServerTickHandler().getTickCounter());

				IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>> claimsManager = serverData.getServerClaimsManager();

				claimsManager.getPermissionHandler().ensureAdminModeStatusPermission(player, playerData);
				boolean shouldReplace = opReplaceCurrent || playerData.isClaimsAdminMode();
				ClaimResult<?> result = null;
				try {
					if(shouldClaim) {
						IPlayerConfig playerConfig = serverData.getPlayerConfigs().getLoadedConfig(player.getUUID());
						IPlayerConfig usedSubConfig = shouldServerClaim ? playerConfig.getUsedServerSubConfig() : playerConfig.getUsedSubConfig();
						int subConfigIndex = usedSubConfig.getSubIndex();
						result = claimsManager.tryToClaimTyped(world.dimension().location(), playerId, subConfigIndex, player.chunkPosition().x, player.chunkPosition().z, chunkX, chunkZ, shouldReplace);

						if(result.getResultType() == ClaimResult.Type.ALREADY_CLAIMED) {
							IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>> claimOwnerInfo = claimsManager.getPlayerInfo(result.getClaimResult().getPlayerId());
							context.getSource().sendFailure(adaptiveLocalizer.getFor(player, "gui.xaero_claims_claim_already_claimed_by", claimOwnerInfo.getPlayerUsername()));
							return 0;
						}
					} else {
						result = claimsManager.tryToUnclaimTyped(world.dimension().location(), playerId, player.chunkPosition().x, player.chunkPosition().z, chunkX, chunkZ, shouldReplace);
						if(!result.getResultType().success) {
							context.getSource().sendFailure(adaptiveLocalizer.getFor(player, result.getResultType().message));
							return 0;
						}
					}
					if(result.getResultType().success) {
						player.sendSystemMessage(adaptiveLocalizer.getFor(player, shouldClaim ? "gui.xaero_claims_claimed_at" : "gui.xaero_claims_unclaimed_at", chunkX, chunkZ));
						return 1;
					} else {
						if(result.getResultType().fail)
							context.getSource().sendFailure(adaptiveLocalizer.getFor(player, result.getResultType().message));
						else
							player.sendSystemMessage(adaptiveLocalizer.getFor(player, result.getResultType().message));
						return 0;
					}
				} finally {
					if(result != null)
						((ClaimsManagerSynchronizer)claimsManager.getClaimsManagerSynchronizer()).syncToPlayerClaimActionResult(
								new AreaClaimResult(Sets.newHashSet(result.getResultType()), chunkX, chunkZ, chunkX, chunkZ),
								player);
				}
			});
	}

	public static Predicate<CommandSourceStack> getServerClaimCommandRequirement(){
		return CommandRequirementHelper.onServerThread(source -> {
			if(source.hasPermission(2))
				return true;
			try {
				ServerPlayer player = source.getPlayerOrException();
				MinecraftServer server = player.getServer();
				IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
						serverData = ServerData.from(server);
				if(serverData.getServerClaimsManager().getPermissionHandler().playerHasServerClaimPermission(player))
					return true;
			} catch (CommandSyntaxException e) {
			}
			return false;
		});
	}

}
