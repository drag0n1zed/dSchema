package xaero.pac.common.server.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
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
import xaero.pac.common.server.player.config.PlayerConfigOptionSpec;
import xaero.pac.common.server.player.config.api.IPlayerConfigAPI.SetResult;
import xaero.pac.common.server.player.config.api.PlayerConfigType;
import xaero.pac.common.server.player.config.sub.PlayerSubConfig;
import xaero.pac.common.server.player.localization.AdaptiveLocalizer;

import java.util.UUID;

import static xaero.pac.common.server.command.ConfigCommandUtil.*;

public class ConfigSetCommand {

	private <T extends Comparable<T>> SetResult tryToSet(CommandContext<CommandSourceStack> context, ServerPlayer player, AdaptiveLocalizer adaptiveLocalizer, IPlayerConfig playerConfig, PlayerConfigOptionSpec<T> option, String valueInput, boolean reset) {
		SetResult result;
		if(reset) {
			result = playerConfig.tryToReset(option);
		} else {
			T value;
			try {
				value = option.getCommandInputParser().apply(valueInput);
			} catch (Throwable t) {
				context.getSource().sendFailure(adaptiveLocalizer.getFor(player, "gui.xaero_pac_config_option_set_invalid_value_format"));
				return SetResult.INVALID;
			}
			result = playerConfig.tryToSet(option, value);
		}
		if(result == SetResult.INVALID)
			context.getSource().sendFailure(adaptiveLocalizer.getFor(player, "gui.xaero_pac_config_option_set_invalid_value"));
		return result;
	}

	public void register(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection environment) {
		SuggestionProvider<CommandSourceStack> optionSuggestor = ConfigGetOrHelpCommand.getOptionSuggestor();
		SuggestionProvider<CommandSourceStack> playerSubConfigSuggestionProvider = getSubConfigSuggestionProvider(PlayerConfigType.PLAYER);
		SuggestionProvider<CommandSourceStack> serverSubConfigSuggestionProvider = getSubConfigSuggestionProvider(PlayerConfigType.SERVER);

		registerSetCommands("set", dispatcher, optionSuggestor, playerSubConfigSuggestionProvider,
				serverSubConfigSuggestionProvider, false);

		registerSetCommands("reset", dispatcher, optionSuggestor, playerSubConfigSuggestionProvider,
				serverSubConfigSuggestionProvider, true);
	}

	private void registerSetCommands(String literalPrefix, CommandDispatcher<CommandSourceStack> dispatcher,
									 SuggestionProvider<CommandSourceStack> optionSuggestor,
									 SuggestionProvider<CommandSourceStack> playerSubConfigSuggestionProvider,
									 SuggestionProvider<CommandSourceStack> serverSubConfigSuggestionProvider,
									 boolean reset){
		Command<CommandSourceStack> regularExecutor = getExecutor(PlayerConfigType.PLAYER, reset);
		Command<CommandSourceStack> defaultExecutor = getExecutor(PlayerConfigType.DEFAULT_PLAYER, reset);
		Command<CommandSourceStack> serverExecutor = getExecutor(PlayerConfigType.SERVER, reset);
		Command<CommandSourceStack> expiredExecutor = getExecutor(PlayerConfigType.EXPIRED, reset);
		Command<CommandSourceStack> wildernessExecutor = getExecutor(PlayerConfigType.WILDERNESS, reset);

		LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal(CommonCommandRegister.COMMAND_PREFIX).then(Commands.literal("player-config")
				.then(Commands.literal(literalPrefix)
				.requires(sourceStack -> true)
				.then(addValueArgumentIfNeeded(reset, regularExecutor, Commands.argument("key", StringArgumentType.word())
				.suggests(optionSuggestor)))));
		dispatcher.register(command);

		//sub version of this ^
		command = Commands.literal(CommonCommandRegister.COMMAND_PREFIX).then(Commands.literal("player-config")
				.then(Commands.literal("sub")
				.then(Commands.literal(literalPrefix)
				.requires(sourceStack -> true)
				.then(Commands.argument("sub-id", StringArgumentType.word())
				.suggests(playerSubConfigSuggestionProvider)
				.then(addValueArgumentIfNeeded(reset, regularExecutor, Commands.argument("key", StringArgumentType.word())
				.suggests(optionSuggestor)))))));
		dispatcher.register(command);

		command = Commands.literal(CommonCommandRegister.COMMAND_PREFIX).then(Commands.literal("player-config")
				.then(Commands.literal("for")
				.then(Commands.argument("player", GameProfileArgument.gameProfile())
				.requires(sourceStack -> sourceStack.hasPermission(2))
				.then(Commands.literal(literalPrefix)
				.then(addValueArgumentIfNeeded(reset, regularExecutor, Commands.argument("key", StringArgumentType.word())
				.suggests(optionSuggestor)))))));
		dispatcher.register(command);

		//sub version of this ^
		command = Commands.literal(CommonCommandRegister.COMMAND_PREFIX).then(Commands.literal("player-config")
				.then(Commands.literal("for")
				.then(Commands.argument("player", GameProfileArgument.gameProfile())
				.requires(sourceStack -> sourceStack.hasPermission(2))
				.then(Commands.literal("sub")
				.then(Commands.literal(literalPrefix)
				.then(Commands.argument("sub-id", StringArgumentType.word())
				.suggests(playerSubConfigSuggestionProvider)
				.then(addValueArgumentIfNeeded(reset, regularExecutor, Commands.argument("key", StringArgumentType.word())
				.suggests(optionSuggestor)))))))));
		dispatcher.register(command);

		command = Commands.literal(CommonCommandRegister.COMMAND_PREFIX).then(Commands.literal("player-config").then(Commands.literal("default")
				.requires(sourceStack -> sourceStack.hasPermission(2))
				.then(Commands.literal(literalPrefix)
				.then(addValueArgumentIfNeeded(reset, defaultExecutor, Commands.argument("key", StringArgumentType.word())
				.suggests(optionSuggestor))))));
		dispatcher.register(command);

		command = Commands.literal(CommonCommandRegister.COMMAND_PREFIX).then(Commands.literal("server-claims-config")
				.requires(sourceStack -> sourceStack.hasPermission(2))
				.then(Commands.literal(literalPrefix)
				.then(addValueArgumentIfNeeded(reset, serverExecutor, Commands.argument("key", StringArgumentType.word())
				.suggests(optionSuggestor)))));
		dispatcher.register(command);

		//sub version of this ^
		command = Commands.literal(CommonCommandRegister.COMMAND_PREFIX).then(Commands.literal("server-claims-config")
				.requires(sourceStack -> sourceStack.hasPermission(2))
				.then(Commands.literal("sub")
				.then(Commands.literal(literalPrefix)
				.then(Commands.argument("sub-id", StringArgumentType.word())
				.suggests(serverSubConfigSuggestionProvider)
				.then(addValueArgumentIfNeeded(reset, serverExecutor, Commands.argument("key", StringArgumentType.word())
				.suggests(optionSuggestor)))))));
		dispatcher.register(command);

		command = Commands.literal(CommonCommandRegister.COMMAND_PREFIX).then(Commands.literal("expired-claims-config")
				.requires(sourceStack -> sourceStack.hasPermission(2))
				.then(Commands.literal(literalPrefix)
				.then(addValueArgumentIfNeeded(reset, expiredExecutor, Commands.argument("key", StringArgumentType.word())
				.suggests(optionSuggestor)))));
		dispatcher.register(command);

		command = Commands.literal(CommonCommandRegister.COMMAND_PREFIX).then(Commands.literal("wilderness-config")
				.requires(sourceStack -> sourceStack.hasPermission(2))
				.then(Commands.literal(literalPrefix)
				.then(addValueArgumentIfNeeded(reset, wildernessExecutor, Commands.argument("key", StringArgumentType.word())
				.suggests(optionSuggestor)))));
		dispatcher.register(command);
	}

	private <T extends ArgumentBuilder<CommandSourceStack, T>> T addValueArgumentIfNeeded(boolean reset, Command<CommandSourceStack> executor, T builder){
		if(reset)
			return builder.executes(executor);
		else
			return builder.then(Commands.argument("value", StringArgumentType.string()).executes(executor));
	}

	public Command<CommandSourceStack> getExecutor(PlayerConfigType type, boolean reset){
		return context -> {
			ServerPlayer sourcePlayer = context.getSource().getPlayerOrException();

			String targetConfigOptionId = StringArgumentType.getString(context, "key");
			MinecraftServer server = context.getSource().getServer();
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
					serverData = ServerData.from(server);
			AdaptiveLocalizer adaptiveLocalizer = serverData.getAdaptiveLocalizer();
			PlayerConfigOptionSpec<?> option = (PlayerConfigOptionSpec<?>) serverData.getPlayerConfigs().getOptionForId(targetConfigOptionId);
			if(option == null) {
				context.getSource().sendFailure(adaptiveLocalizer.getFor(sourcePlayer, "gui.xaero_pac_config_option_set_invalid_key"));
				return 0;
			}
			GameProfile inputPlayer = null;
			UUID configPlayerUUID = type == PlayerConfigType.SERVER ? PlayerConfig.SERVER_CLAIM_UUID : null;
			if(type == PlayerConfigType.PLAYER) {
				inputPlayer = getConfigInputPlayer(context, sourcePlayer,
						"gui.xaero_pac_config_option_set_too_many_targets",
						"gui.xaero_pac_config_option_set_invalid_target", adaptiveLocalizer);
				if(inputPlayer == null)
					return 0;
				configPlayerUUID = inputPlayer.getId();
			}

			String valueInput = reset ? null : StringArgumentType.getString(context, "value");

			IPlayerConfig playerConfig =
					type == PlayerConfigType.DEFAULT_PLAYER ?
						serverData.getPlayerConfigs().getDefaultConfig() :
							type == PlayerConfigType.EXPIRED ?
								serverData.getPlayerConfigs().getExpiredClaimConfig() :
										serverData.getPlayerConfigs().getLoadedConfig(configPlayerUUID);
			IPlayerConfig effectivePlayerConfig = getEffectiveConfig(context, playerConfig);
			if(effectivePlayerConfig == null) {
				context.getSource().sendFailure(adaptiveLocalizer.getFor(sourcePlayer, "gui.xaero_pac_config_option_set_invalid_sub"));
				return 0;
			}
			boolean isOP = context.getSource().hasPermission(2);
			if(!isOP && PlayerConfig.isOptionOPConfigurable(option)) {
				//such options are not redirected to the default config, so they need a separate check
				context.getSource().sendFailure(adaptiveLocalizer.getFor(sourcePlayer, "gui.xaero_pac_config_op_option"));
				return 0;
			}
			SetResult result = tryToSet(context, sourcePlayer, adaptiveLocalizer, effectivePlayerConfig, option, valueInput, reset);
			if(result == SetResult.INVALID)
				return 0;
			if(result == SetResult.ILLEGAL_OPTION){
				context.getSource().sendFailure(adaptiveLocalizer.getFor(sourcePlayer, "gui.xaero_pac_config_option_set_illegal_option"));
				return 0;
			}
			Object wantedValue = reset ? effectivePlayerConfig.getDefaultRawValue(option) : option.getCommandInputParser().apply(valueInput);
			Object actualValue = effectivePlayerConfig.getFromEffectiveConfig(option);
			if(effectivePlayerConfig instanceof PlayerSubConfig<?> subConfig && subConfig.isInherited(option))
				actualValue = null;

			Component wantedValueName = option.getValueDisplayName(wantedValue);
			if (type == PlayerConfigType.PLAYER)
				sourcePlayer.sendSystemMessage(adaptiveLocalizer.getFor(sourcePlayer, "gui.xaero_pac_config_option_set", inputPlayer.getName(), targetConfigOptionId, wantedValueName));
			else
				sourcePlayer.sendSystemMessage(adaptiveLocalizer.getFor(sourcePlayer, "gui.xaero_pac_config_option_set", type.getName(), targetConfigOptionId, wantedValueName));
			if (result == SetResult.DEFAULTED && wantedValue != null && wantedValue != actualValue) {
				Component actualValueName = option.getValueDisplayName(actualValue);
				sourcePlayer.sendSystemMessage(adaptiveLocalizer.getFor(sourcePlayer, "gui.xaero_pac_config_option_set_server_force", actualValueName));
			}
			return 1;
		};
	}

}
