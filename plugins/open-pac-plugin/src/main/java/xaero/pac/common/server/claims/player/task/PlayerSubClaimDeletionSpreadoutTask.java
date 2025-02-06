package xaero.pac.common.server.claims.player.task;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.claims.player.PlayerChunkClaim;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.config.IPlayerConfig;
import xaero.pac.common.server.player.config.api.PlayerConfigType;
import xaero.pac.common.server.player.data.ServerPlayerData;
import xaero.pac.common.server.player.localization.AdaptiveLocalizer;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;

public final class PlayerSubClaimDeletionSpreadoutTask extends PlayerClaimReplaceSpreadoutTask {

	private PlayerSubClaimDeletionSpreadoutTask(IPlayerClaimReplaceSpreadoutTaskCallback callback, UUID claimOwnerId, Predicate<IPlayerChunkClaim> matcher, IPlayerChunkClaim with) {
		super(callback, claimOwnerId, matcher, with);
	}

	public static final class Builder {

		private MinecraftServer server;
		private IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>> playerInfo;
		private int subConfigIndex;
		private boolean forceloadable;
		private boolean last;
		private UUID callerUUID;

		private Builder(){}

		public Builder setDefault(){
			setServer(null);
			setPlayerInfo(null);
			setSubConfigIndex(-1);
			setForceloadable(false);
			setLast(false);
			setCallerUUID(null);
			return this;
		}

		public Builder setServer(MinecraftServer server) {
			this.server = server;
			return this;
		}

		public Builder setPlayerInfo(IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>> playerInfo) {
			this.playerInfo = playerInfo;
			return this;
		}

		public Builder setSubConfigIndex(int subConfigIndex) {
			this.subConfigIndex = subConfigIndex;
			return this;
		}

		public Builder setForceloadable(boolean forceloadable) {
			this.forceloadable = forceloadable;
			return this;
		}

		public Builder setLast(boolean last) {
			this.last = last;
			return this;
		}

		public Builder setCallerUUID(UUID callerUUID) {
			this.callerUUID = callerUUID;
			return this;
		}

		public PlayerSubClaimDeletionSpreadoutTask build(){
			if(server == null || playerInfo == null || subConfigIndex == -1)
				throw new IllegalStateException();
			Callback callback = new Callback(server, playerInfo, subConfigIndex, last, callerUUID);
			UUID claimOwnerId = playerInfo.getPlayerId();
			Predicate<IPlayerChunkClaim> matcher = c -> c.getSubConfigIndex() == subConfigIndex && c.isForceloadable() == forceloadable;
			IPlayerChunkClaim with = new PlayerChunkClaim(claimOwnerId, -1, forceloadable, 0);
			return new PlayerSubClaimDeletionSpreadoutTask(callback, claimOwnerId, matcher, with);
		}

		public static Builder begin(){
			return new Builder().setDefault();
		}

	}

	private static final class Callback implements IPlayerClaimReplaceSpreadoutTaskCallback {

		private final MinecraftServer server;
		private final IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>> playerInfo;
		private final int subConfigIndex;
		private final boolean last;
		private final UUID callerUUID;

		public Callback(MinecraftServer server, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>> playerInfo, int subConfigIndex, boolean last, UUID callerUUID) {
			this.server = server;
			this.playerInfo = playerInfo;
			this.subConfigIndex = subConfigIndex;
			this.last = last;
			this.callerUUID = callerUUID;
		}

		@Override
		public void onWork(int tickCount) {
		}

		@Override
		public void onFinish(ResultType resultType, int tickCount, int totalCount, IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData) {
			if(last) {
				ServerPlayer onlinePlayer = callerUUID == null ? null : server.getPlayerList().getPlayer(callerUUID);
				AdaptiveLocalizer adaptiveLocalizer = serverData.getAdaptiveLocalizer();
				if (resultType.isSuccess()) {
					IPlayerConfig config = serverData.getPlayerConfigs().getLoadedConfig(playerInfo.getPlayerId());
					IPlayerConfig removedSub = config.removeSubConfig(subConfigIndex);
					if (onlinePlayer != null && removedSub != null) {
						if(removedSub.getType() != PlayerConfigType.SERVER && !removedSub.getPlayerId().equals(callerUUID)) {
							ServerPlayerData playerData = (ServerPlayerData) ServerPlayerData.from(onlinePlayer);
							if(Objects.equals(playerData.getLastOtherConfigRequest(), removedSub.getPlayerId()))
								serverData.getPlayerConfigs().getSynchronizer().syncSubExistence(onlinePlayer, removedSub, false);//notify the "other player" config
						}
						onlinePlayer.sendSystemMessage(adaptiveLocalizer.getFor(onlinePlayer, "gui.xaero_pac_config_delete_sub_complete", removedSub.getSubId()));
					}
				} else {
					if (onlinePlayer != null)
						onlinePlayer.sendSystemMessage(adaptiveLocalizer.getFor(onlinePlayer, resultType.getMessage()));
				}
			}
		}

	}

}
