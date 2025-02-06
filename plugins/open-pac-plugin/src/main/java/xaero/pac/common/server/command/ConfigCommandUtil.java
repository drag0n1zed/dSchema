package xaero.pac.common.server.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.ServerData;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.config.IPlayerConfig;
import xaero.pac.common.server.player.config.PlayerConfig;
import xaero.pac.common.server.player.config.api.PlayerConfigType;
import xaero.pac.common.server.player.localization.AdaptiveLocalizer;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class ConfigCommandUtil {

	static IPlayerConfig getEffectiveConfig(CommandContext<CommandSourceStack> context, IPlayerConfig playerConfig){
		IPlayerConfig effectivePlayerConfig = playerConfig;
		try {
			String subConfigId = StringArgumentType.getString(context, "sub-id");
			effectivePlayerConfig = playerConfig.getSubConfig(subConfigId);
		} catch(IllegalArgumentException e){
		}
		return effectivePlayerConfig;
	}

	public static GameProfile getConfigInputPlayer(CommandContext<CommandSourceStack> context, ServerPlayer sourcePlayer, String tooManyTargetMessage, String invalidTargetMessage, AdaptiveLocalizer adaptiveLocalizer) throws CommandSyntaxException {
		GameProfile inputPlayer;
		try {
			Collection<GameProfile> profiles = GameProfileArgument.getGameProfiles(context, "player");
			if(profiles.size() > 1) {
				if(tooManyTargetMessage != null)
					context.getSource().sendFailure(adaptiveLocalizer.getFor(sourcePlayer, tooManyTargetMessage));
				return null;
			} else if(profiles.isEmpty()) {
				if(invalidTargetMessage != null)
					context.getSource().sendFailure(adaptiveLocalizer.getFor(sourcePlayer, invalidTargetMessage));
				return null;
			}
			inputPlayer = profiles.iterator().next();
		} catch(IllegalArgumentException e) {
			inputPlayer = sourcePlayer.getGameProfile();
		}
		return inputPlayer;
	}

	public static SuggestionProvider<CommandSourceStack> getSubConfigSuggestionProvider(PlayerConfigType type){
		return (context, builder) -> {
			ServerPlayer sourcePlayer = context.getSource().getPlayerOrException();
			MinecraftServer server = sourcePlayer.getServer();
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(server);
			AdaptiveLocalizer adaptiveLocalizer = serverData.getAdaptiveLocalizer();
			UUID configOwnerId;
			if(type != PlayerConfigType.SERVER) {
				GameProfile gameProfile = getConfigInputPlayer(context, sourcePlayer, null, null, adaptiveLocalizer);
				if (gameProfile == null)
					return SharedSuggestionProvider.suggest(Stream.empty(), builder);
				configOwnerId = gameProfile.getId();
			} else
				configOwnerId = PlayerConfig.SERVER_CLAIM_UUID;
			String lowerCaseInput = builder.getRemainingLowerCase();
			IPlayerConfig playerConfig = serverData.getPlayerConfigs().getLoadedConfig(configOwnerId);
			List<String> subConfigIds = playerConfig.getSubConfigIds();
			Stream<String> baseStream = subConfigIds.stream();
			if(!lowerCaseInput.isEmpty())
				baseStream = baseStream.filter(s -> s.toLowerCase().startsWith(lowerCaseInput));
			return SharedSuggestionProvider.suggest(baseStream.limit(64), builder);
		};
	}

}
