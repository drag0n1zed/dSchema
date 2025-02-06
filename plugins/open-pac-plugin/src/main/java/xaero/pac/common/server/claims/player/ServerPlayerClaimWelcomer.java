package xaero.pac.common.server.claims.player;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerPlayer;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimInfo;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.config.IPlayerConfig;
import xaero.pac.common.server.player.config.PlayerConfig;
import xaero.pac.common.server.player.config.api.PlayerConfigOptions;
import xaero.pac.common.server.player.data.ServerPlayerData;
import xaero.pac.common.server.player.localization.AdaptiveLocalizer;

import java.util.Objects;
import java.util.UUID;

public class ServerPlayerClaimWelcomer {

	public void onPlayerTick(ServerPlayerData mainCap, ServerPlayer player, IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData){
		IPlayerChunkClaim lastClaimCheck = mainCap.getLastClaimCheck();
		IServerClaimsManager<?, ?, ?> claimsManager = serverData.getServerClaimsManager();
		IPlayerChunkClaim currentClaim = claimsManager.get(player.level().dimension().location(), player.chunkPosition());
		if (!Objects.equals(lastClaimCheck, currentClaim)) {
			AdaptiveLocalizer adaptiveLocalizer = serverData.getAdaptiveLocalizer();
			UUID currentClaimId = currentClaim == null ? null : currentClaim.getPlayerId();
			IPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>> playerClaimInfo = currentClaim == null ? null : serverData.getServerClaimsManager().getPlayerInfo(currentClaimId);
			boolean isOwner = !mainCap.isClaimsNonallyMode() && currentClaim != null && Objects.equals(currentClaimId, player.getUUID());
			boolean hasAccess = isOwner || serverData.getChunkProtection().hasChunkAccess(serverData.getChunkProtection().getClaimConfig(serverData.getPlayerConfigs(), currentClaim), player, null);

			IPlayerConfig claimConfig = serverData.getChunkProtection().getClaimConfig(serverData.getPlayerConfigs(), currentClaim);
			String customName = claimConfig.getEffective(PlayerConfigOptions.CLAIMS_NAME);
			int claimColor = claimConfig.getEffective(PlayerConfigOptions.CLAIMS_COLOR);
			MutableComponent subTitleText;
			if (playerClaimInfo == null)
				subTitleText = adaptiveLocalizer.getFor(player, customName == null || customName.isEmpty() ? "gui.xaero_pac_title_entered_wilderness" : customName);
			else {
				MutableComponent properDesc;
				Component forceloadedComponent = currentClaim.isForceloadable() ? adaptiveLocalizer.getFor(player, "gui.xaero_pac_marked_for_forceload") : Component.literal("");
				if (Objects.equals(currentClaimId, PlayerConfig.SERVER_CLAIM_UUID))
					properDesc = adaptiveLocalizer.getFor(player, "gui.xaero_pac_title_entered_server_claim", forceloadedComponent);
				else if (Objects.equals(currentClaimId, PlayerConfig.EXPIRED_CLAIM_UUID))
					properDesc = adaptiveLocalizer.getFor(player, "gui.xaero_pac_title_entered_expired_claim", forceloadedComponent);
				else
					properDesc = adaptiveLocalizer.getFor(player, "gui.xaero_pac_title_entered_claim", playerClaimInfo.getPlayerUsername(), forceloadedComponent);
				if (customName != null && !customName.isEmpty()) {
					subTitleText = Component.literal(customName + " - ");
					subTitleText.getSiblings().add(properDesc);
				} else
					subTitleText = properDesc;
			}
			subTitleText = subTitleText.withStyle(s -> s.withColor(isOwner ? ChatFormatting.DARK_GREEN : hasAccess ? ChatFormatting.GOLD : ChatFormatting.DARK_RED));

			MutableComponent subTitle = Component.literal("□ ").withStyle(s -> s.withColor(claimColor));
			subTitle.getSiblings().add(subTitleText);
			subTitle.getSiblings().add(Component.literal(" □").withStyle(s -> s.withColor(claimColor)));
			ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(subTitle);
			player.connection.send(packet);

			mainCap.setLastClaimCheck(currentClaim);
		}
	}

}
