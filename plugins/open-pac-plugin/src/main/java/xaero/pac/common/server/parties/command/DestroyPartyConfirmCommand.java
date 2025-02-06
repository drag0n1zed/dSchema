package xaero.pac.common.server.parties.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
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
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.parties.party.IPartyManager;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.localization.AdaptiveLocalizer;

import java.util.UUID;
import java.util.function.Predicate;

public class DestroyPartyConfirmCommand {

	public void register(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection environment, CommandRequirementProvider commandRequirementProvider) {
		Predicate<CommandSourceStack> requirement = commandRequirementProvider.getMemberRequirement((party, mi) -> party.getOwner() == mi);
		LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal(PartyCommandRegister.COMMAND_PREFIX).requires(c -> ServerConfig.CONFIG.partiesEnabled.get()).then(Commands.literal("destroy")
				.requires(requirement)
				.executes(context -> {
					ServerPlayer player = context.getSource().getPlayerOrException();
					MinecraftServer server = context.getSource().getServer();
					IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(server);
					AdaptiveLocalizer adaptiveLocalizer = serverData.getAdaptiveLocalizer();
					context.getSource().sendFailure(adaptiveLocalizer.getFor(player, "gui.xaero_parties_party_destroy_info"));
					return 0;
				})
				.then(Commands.literal("confirm")
				.executes(context -> {
					ServerPlayer player = context.getSource().getPlayerOrException();
					UUID playerId = player.getUUID();
					MinecraftServer server = context.getSource().getServer();
					IServerData<IServerClaimsManager<IPlayerChunkClaim, IServerPlayerClaimInfo<IPlayerDimensionClaims<IPlayerClaimPosList>>, IServerDimensionClaimsManager<IServerRegionClaims>>, IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> serverData = ServerData.from(server);
					IPartyManager<IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly>> partyManager = serverData.getPartyManager();
					IServerParty<IPartyMember, IPartyPlayerInfo, IPartyAlly> playerParty = partyManager.getPartyByMember(playerId);
					partyManager.removeTypedParty(playerParty);
					new PartyOnCommandUpdater().update(playerId, serverData, playerParty, serverData.getPlayerConfigs(), mi -> true, Component.translatable("gui.xaero_parties_party_destroy_members_info", Component.literal(playerParty.getOwner().getUsername()).withStyle(s -> s.withColor(ChatFormatting.YELLOW))));
					return 1;
				})));
		dispatcher.register(command);
	}

}
