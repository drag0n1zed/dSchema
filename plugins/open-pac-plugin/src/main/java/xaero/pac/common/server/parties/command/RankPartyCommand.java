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

import java.util.Arrays;
import java.util.UUID;
import java.util.function.Predicate;

public class RankPartyCommand {

	public void register(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection environment, CommandRequirementProvider commandRequirementProvider) {
		Predicate<CommandSourceStack> requirement = commandRequirementProvider.getMemberRequirement((party, mi) -> mi.getRank().ordinal() >= PartyMemberRank.ADMIN.ordinal());
		LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal(PartyCommandRegister.COMMAND_PREFIX).requires(c -> ServerConfig.CONFIG.partiesEnabled.get()).then(Commands.literal("member")
				.requires(requirement).then(Commands.literal("rank").requires(requirement).then(Commands.argument("rank", StringArgumentType.word())
				.suggests((context, builder) -> {
					return SharedSuggestionProvider.suggest(Arrays.asList(PartyMemberRank.values()).stream().map(r -> r.toString()), builder);
				})
				.then(Commands.argument("name", StringArgumentType.word())
						.suggests(PartyCommands.getPartyMemberSuggestor())
						.executes(context -> {
							ServerPlayer player = context.getSource().getPlayerOrException();
							UUID playerId = player.getUUID();
							MinecraftServer server = context.getSource().getServer();
							IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(server);
							AdaptiveLocalizer adaptiveLocalizer = serverData.getAdaptiveLocalizer();
							IPartyManager<IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> partyManager = serverData.getPartyManager();
							IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly> playerParty = partyManager.getPartyByMember(playerId);

							String targetUsername = StringArgumentType.getString(context, "name");
							IPartyPlayerInfo targetPlayerInfo = playerParty.getMemberInfo(targetUsername);

							if(targetPlayerInfo == null) {
								context.getSource().sendFailure(adaptiveLocalizer.getFor(player, "gui.xaero_parties_rank_not_member", targetUsername));
								return 0;
							}

							IPartyMember casterInfo = playerParty.getMemberInfo(playerId);
							boolean casterIsOwner = playerParty.getOwner() == casterInfo;
							IPartyMember targetMember = (IPartyMember) targetPlayerInfo;

							if(!casterIsOwner && targetMember.getRank().ordinal() >= casterInfo.getRank().ordinal() || targetMember == playerParty.getOwner()) {
								context.getSource().sendFailure(adaptiveLocalizer.getFor(player, "gui.xaero_parties_rank_not_lower_rank_player"));
								return 0;
							}

							String targetRankString = StringArgumentType.getString(context, "rank");
							PartyMemberRank targetRank = PartyMemberRank.valueOf(targetRankString);

							if(!casterIsOwner && targetRank.ordinal() >= casterInfo.getRank().ordinal()) {
								context.getSource().sendFailure(adaptiveLocalizer.getFor(player, "gui.xaero_parties_rank_not_lower_rank"));
								return 0;
							}

							playerParty.setRankTyped(targetMember, targetRank);

							UUID targetPlayerId = targetMember.getUUID();
							ServerPlayer rankedPlayer = server.getPlayerList().getPlayer(targetPlayerId);
							if(rankedPlayer != null)
								server.getCommands().sendCommands(rankedPlayer);

							new PartyOnCommandUpdater().update(playerId, serverData, playerParty, serverData.getPlayerConfigs(), mi -> false, Component.translatable("gui.xaero_parties_rank_party_message", Component.literal(casterInfo.getUsername()).withStyle(s -> s.withColor(ChatFormatting.DARK_GREEN)), Component.literal(targetPlayerInfo.getUsername()).withStyle(s -> s.withColor(ChatFormatting.YELLOW)), Component.literal(targetRank.toString()).withStyle(s -> s.withColor(targetRank.getColor()))));

							return 1;
						})))));
		dispatcher.register(command);
	}

}
