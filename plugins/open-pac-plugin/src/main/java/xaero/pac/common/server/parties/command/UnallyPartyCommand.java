package xaero.pac.common.server.parties.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import xaero.pac.common.claims.player.IPlayerChunkClaim;
import xaero.pac.common.claims.player.IPlayerClaimPosList;
import xaero.pac.common.claims.player.IPlayerDimensionClaims;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.parties.party.member.PartyMemberRank;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.ServerData;
import xaero.pac.common.server.claims.IServerClaimsManager;
import xaero.pac.common.server.claims.IServerDimensionClaimsManager;
import xaero.pac.common.server.claims.IServerRegionClaims;
import xaero.pac.common.server.claims.player.IServerPlayerClaimInfo;
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.parties.party.IPartyManager;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.localization.AdaptiveLocalizer;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class UnallyPartyCommand {

	public void register(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection environment, CommandRequirementProvider commandRequirementProvider) {
		Predicate<CommandSourceStack> requirement = commandRequirementProvider.getMemberRequirement((party, mi) -> mi.getRank().ordinal() >= PartyMemberRank.MODERATOR.ordinal());
		LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal(PartyCommandRegister.COMMAND_PREFIX).requires(c -> ServerConfig.CONFIG.partiesEnabled.get()).then(Commands.literal("ally")
				.requires(requirement).then(Commands.literal("remove")
				.then(Commands.argument("owner", StringArgumentType.word())
						.suggests((context, builder) -> {
							//limited at 16 to reduce synced data for super large parties
							ServerPlayer commandPlayer = context.getSource().getPlayerOrException();
							IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(context.getSource().getServer());
							IPartyManager<IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> partyManager = serverData.getPartyManager();
							IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly> playerParty = partyManager.getPartyByMember(commandPlayer.getUUID());
							String lowercaseInput = builder.getRemainingLowerCase();
							Stream<IPartyAlly> partyAllyStream;
							if(playerParty.getAllyCount() > 1024) {
								IPartyAlly exactAlly = playerParty.getAlly(lowercaseInput);
								partyAllyStream = Stream.ofNullable(exactAlly);
							} else
								partyAllyStream = playerParty.getTypedAllyPartiesStream();
							//probably not a good idea to let players spam something like this somewhat easily, so it's limited at 1024
							return SharedSuggestionProvider.suggest(partyAllyStream
									.map(IPartyAlly::getPartyId)
									.map(partyManager::getPartyById)
									.filter(Objects::nonNull)
									.map(party -> party.getOwner().getUsername())
									.filter(ownerName -> ownerName.toLowerCase().startsWith(lowercaseInput))
									.limit(16),
									builder);
						})
						.executes(context -> {
							ServerPlayer player = context.getSource().getPlayerOrException();
							UUID playerId = player.getUUID();
							MinecraftServer server = context.getSource().getServer();
							IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(server);
							AdaptiveLocalizer adaptiveLocalizer = serverData.getAdaptiveLocalizer();
							IPartyManager<IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> partyManager = serverData.getPartyManager();
							IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly> playerParty = partyManager.getPartyByMember(playerId);

							String targetOwnerName = StringArgumentType.getString(context, "owner");
							IPartyAlly targetAlly = playerParty.getAlly(targetOwnerName);
							IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly> targetPlayerParty = targetAlly == null ? null : partyManager.getPartyById(targetAlly.getPartyId());

							if(targetPlayerParty == null) {
								context.getSource().sendFailure(adaptiveLocalizer.getFor(player, "gui.xaero_parties_unally_party_not_found", targetOwnerName));
								return 0;
							}

							playerParty.removeAllyParty(targetPlayerParty.getId());

							new PartyOnCommandUpdater().update(playerId, serverData, targetPlayerParty, serverData.getPlayerConfigs(), mi -> false, Component.translatable("gui.xaero_parties_unally_target_party_message", Component.literal(playerParty.getDefaultName()).withStyle(s -> s.withColor(ChatFormatting.DARK_GREEN)), Component.literal(targetPlayerParty.getDefaultName())));

							IPartyMember casterInfo = playerParty.getMemberInfo(playerId);
							new PartyOnCommandUpdater().update(playerId, serverData, playerParty, serverData.getPlayerConfigs(), mi -> false, Component.translatable("gui.xaero_parties_unally_caster_party_message", Component.literal(casterInfo.getUsername()).withStyle(s -> s.withColor(ChatFormatting.DARK_GREEN)), Component.literal(targetPlayerParty.getDefaultName()).withStyle(s -> s.withColor(ChatFormatting.YELLOW))));

							return 1;
						}))));
		dispatcher.register(command);
	}

}
