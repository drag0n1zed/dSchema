package xaero.pac.common.server.claims.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
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
import xaero.pac.common.server.player.config.api.IPlayerConfigAPI;
import xaero.pac.common.server.player.config.api.IPlayerConfigOptionSpecAPI;
import xaero.pac.common.server.player.config.api.PlayerConfigOptions;
import xaero.pac.common.server.player.config.api.PlayerConfigType;
import xaero.pac.common.server.player.localization.AdaptiveLocalizer;

import java.util.UUID;
import java.util.function.Predicate;

import static xaero.pac.common.server.command.ConfigCommandUtil.getConfigInputPlayer;
import static xaero.pac.common.server.command.ConfigCommandUtil.getSubConfigSuggestionProvider;

public class ClaimsSubClaimUseCommand {

	public void register(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection environment) {
		Command<CommandSourceStack> regularExecutor = getExecutor(PlayerConfigType.PLAYER);
		Command<CommandSourceStack> serverExecutor = getExecutor(PlayerConfigType.SERVER);

		SuggestionProvider<CommandSourceStack> playerSubConfigSuggestionProvider = getSubConfigSuggestionProvider(PlayerConfigType.PLAYER);
		SuggestionProvider<CommandSourceStack> serverSubConfigSuggestionProvider = getSubConfigSuggestionProvider(PlayerConfigType.SERVER);

		Predicate<CommandSourceStack> serverRequirement = ClaimsClaimCommands.getServerClaimCommandRequirement();

		LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal(ClaimsCommandRegister.COMMAND_PREFIX)
				.then(Commands.literal("sub-claim")
				.then(getMainCommandPart(playerSubConfigSuggestionProvider, regularExecutor)));
		dispatcher.register(command);

		command = Commands.literal(ClaimsCommandRegister.COMMAND_PREFIX).then(Commands.literal("sub-claim")
				.then(Commands.literal("for")
				.requires(sourceStack -> sourceStack.hasPermission(2))
				.then(Commands.argument("player", GameProfileArgument.gameProfile())
				.then(getMainCommandPart(playerSubConfigSuggestionProvider, regularExecutor)))));
		dispatcher.register(command);

		command = Commands.literal(ClaimsCommandRegister.COMMAND_PREFIX).then(Commands.literal("server")
				.requires(serverRequirement)
				.then(Commands.literal("sub-claim")
				.then(getMainCommandPart(serverSubConfigSuggestionProvider, serverExecutor))));
		dispatcher.register(command);

		command = Commands.literal(ClaimsCommandRegister.COMMAND_PREFIX).then(Commands.literal("server")
				.requires(serverRequirement)
				.then(Commands.literal("sub-claim")
				.then(Commands.literal("for")
				.requires(sourceStack -> sourceStack.hasPermission(2))
				.then(Commands.argument("player", GameProfileArgument.gameProfile())
				.then(getMainCommandPart(serverSubConfigSuggestionProvider, serverExecutor))))));
		dispatcher.register(command);
	}

	private LiteralArgumentBuilder<CommandSourceStack> getMainCommandPart(SuggestionProvider<CommandSourceStack> subConfigSuggestionProvider, Command<CommandSourceStack> executor){
		return Commands.literal("use")
				.then(Commands.argument("sub-id", StringArgumentType.word())
				.suggests(subConfigSuggestionProvider)
				.executes(executor));
	}

	private static Command<CommandSourceStack> getExecutor(PlayerConfigType type){
		return context -> {
			ServerPlayer sourcePlayer = context.getSource().getPlayerOrException();
			MinecraftServer server = context.getSource().getServer();
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(server);
			AdaptiveLocalizer adaptiveLocalizer = serverData.getAdaptiveLocalizer();

			String inputSubId = StringArgumentType.getString(context, "sub-id");
			GameProfile inputPlayer = getConfigInputPlayer(context, sourcePlayer,
					"gui.xaero_claims_sub_use_too_many_targets",
					"gui.xaero_claims_sub_use_invalid_target", adaptiveLocalizer);
			if(inputPlayer == null)
				return 0;
			UUID configPlayerUUID = inputPlayer.getId();

			IPlayerConfig playerConfig = serverData.getPlayerConfigs().getLoadedConfig(configPlayerUUID);
			IPlayerConfigOptionSpecAPI<String> option = type == PlayerConfigType.SERVER ? PlayerConfigOptions.USED_SERVER_SUBCLAIM : PlayerConfigOptions.USED_SUBCLAIM;
			IPlayerConfig rootConfig = type == PlayerConfigType.SERVER ? serverData.getPlayerConfigs().getServerClaimConfig() : playerConfig;

			IPlayerConfig result = rootConfig.getSubConfig(inputSubId);
			if(result == null){
				context.getSource().sendFailure(adaptiveLocalizer.getFor(sourcePlayer, "gui.xaero_claims_sub_use_not_exist"));
				return 0;
			}
			IPlayerConfigAPI.SetResult setResult = playerConfig.tryToSet(option, inputSubId);
			if(setResult == IPlayerConfigAPI.SetResult.INVALID) {
				context.getSource().sendFailure(adaptiveLocalizer.getFor(sourcePlayer, "gui.xaero_claims_sub_use_invalid_value"));
				return 0;
			}
			sourcePlayer.sendSystemMessage(adaptiveLocalizer.getFor(sourcePlayer, "gui.xaero_claims_sub_use", inputSubId));
			return 1;
		};
	}

}
