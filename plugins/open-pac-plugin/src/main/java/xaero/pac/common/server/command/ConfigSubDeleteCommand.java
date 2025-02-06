package xaero.pac.common.server.command;

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
import xaero.pac.common.server.player.config.PlayerConfig;
import xaero.pac.common.server.player.config.api.PlayerConfigType;
import xaero.pac.common.server.player.config.sub.PlayerSubConfigDeletionStarter;
import xaero.pac.common.server.player.data.ServerPlayerData;
import xaero.pac.common.server.player.localization.AdaptiveLocalizer;

import java.util.UUID;

import static xaero.pac.common.server.command.ConfigCommandUtil.getConfigInputPlayer;
import static xaero.pac.common.server.command.ConfigCommandUtil.getSubConfigSuggestionProvider;

public class ConfigSubDeleteCommand {

	public void register(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection environment) {
		Command<CommandSourceStack> regularExecutor = getExecutor(PlayerConfigType.PLAYER);
		Command<CommandSourceStack> serverExecutor = getExecutor(PlayerConfigType.SERVER);

		SuggestionProvider<CommandSourceStack> playerSubConfigSuggestionProvider = getSubConfigSuggestionProvider(PlayerConfigType.PLAYER);
		SuggestionProvider<CommandSourceStack> serverSubConfigSuggestionProvider = getSubConfigSuggestionProvider(PlayerConfigType.SERVER);

		LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal(CommonCommandRegister.COMMAND_PREFIX)
				.then(Commands.literal("player-config")
				.then(getMainCommandPart(playerSubConfigSuggestionProvider, regularExecutor)));
		dispatcher.register(command);

		command = Commands.literal(CommonCommandRegister.COMMAND_PREFIX).then(Commands.literal("player-config")
				.then(Commands.literal("for")
				.requires(sourceStack -> sourceStack.hasPermission(2))
				.then(Commands.argument("player", GameProfileArgument.gameProfile())
				.then(getMainCommandPart(playerSubConfigSuggestionProvider, regularExecutor)))));
		dispatcher.register(command);

		command = Commands.literal(CommonCommandRegister.COMMAND_PREFIX).then(Commands.literal("server-claims-config")
				.requires(sourceStack -> sourceStack.hasPermission(2))
				.then(getMainCommandPart(serverSubConfigSuggestionProvider, serverExecutor)));
		dispatcher.register(command);
	}

	private LiteralArgumentBuilder<CommandSourceStack> getMainCommandPart(SuggestionProvider<CommandSourceStack> subConfigSuggestionProvider, Command<CommandSourceStack> executor){
		return Commands.literal("sub")
				.then(Commands.literal("delete")
				.then(Commands.argument("sub-id", StringArgumentType.word())
				.suggests(subConfigSuggestionProvider)
				.executes(executor)));
	}

	private static Command<CommandSourceStack> getExecutor(PlayerConfigType type){
		return context -> {
			ServerPlayer sourcePlayer = context.getSource().getPlayerOrException();
			MinecraftServer server = context.getSource().getServer();
			IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>>
					serverData = ServerData.from(server);
			AdaptiveLocalizer adaptiveLocalizer = serverData.getAdaptiveLocalizer();

			String inputSubId = StringArgumentType.getString(context, "sub-id");
			GameProfile inputPlayer = null;
			UUID configPlayerUUID;
			if(type == PlayerConfigType.PLAYER) {
				inputPlayer = getConfigInputPlayer(context, sourcePlayer,
						"gui.xaero_pac_config_delete_sub_too_many_targets",
						"gui.xaero_pac_config_delete_sub_invalid_target", adaptiveLocalizer);
				if(inputPlayer == null)
					return 0;
				configPlayerUUID = inputPlayer.getId();
			} else
				configPlayerUUID = PlayerConfig.SERVER_CLAIM_UUID;


			ServerPlayerData playerData = (ServerPlayerData) ServerPlayerData.from(sourcePlayer);
			if(serverData.getServerTickHandler().getTickCounter() == playerData.getLastSubConfigCreationTick())
				return 0;//going too fast
			playerData.setLastSubConfigCreationTick(serverData.getServerTickHandler().getTickCounter());

			PlayerConfig<?> playerConfig = (PlayerConfig<?>) serverData.getPlayerConfigs().getLoadedConfig(configPlayerUUID);
			PlayerConfig<?> result = playerConfig.getSubConfig(inputSubId);
			if(result == null){
				context.getSource().sendFailure(adaptiveLocalizer.getFor(sourcePlayer, "gui.xaero_pac_config_delete_sub_not_exist"));
				return 0;
			}
			if(result == playerConfig){
				context.getSource().sendFailure(adaptiveLocalizer.getFor(sourcePlayer, "gui.xaero_pac_config_delete_sub_cant_main"));
				return 0;
			}
			IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>> playerInfo = serverData.getServerClaimsManager().getPlayerInfo(configPlayerUUID);
			if(playerInfo.hasReplacementTasks()){
				context.getSource().sendFailure(adaptiveLocalizer.getFor(sourcePlayer, "gui.xaero_pac_config_delete_sub_already_replacing"));
				return 0;
			}
			new PlayerSubConfigDeletionStarter().start(sourcePlayer, playerInfo, result, serverData);
			return 1;
		};
	}



}
