package xaero.pac.common.server.parties.command;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import xaero.pac.common.parties.party.IPartyPlayerInfo;
import xaero.pac.common.parties.party.ally.IPartyAlly;
import xaero.pac.common.parties.party.member.IPartyMember;
import xaero.pac.common.server.IServerData;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.player.config.IPlayerConfigManager;
import xaero.pac.common.server.player.config.api.PlayerConfigOptions;
import xaero.pac.common.server.player.localization.AdaptiveLocalizer;

import java.util.Iterator;
import java.util.UUID;
import java.util.function.Predicate;

public class PartyOnCommandUpdater {

	private void onOnlineMember(UUID commandCasterId, MinecraftServer server, IPartyMember mi, ServerPlayer onlineMember, Predicate<IPartyMember> shouldUpdateCommandsForMember, Component massMessage) {
		if(shouldUpdateCommandsForMember.test(mi))
			server.getCommands().sendCommands(onlineMember);
		if(massMessage != null)
			onlineMember.sendSystemMessage(massMessage);
	}

	public
	<
		M extends IPartyMember, I extends IPartyPlayerInfo, A extends IPartyAlly
	> void update(UUID commandCasterId, IServerData<?,?> serverData, IServerParty<M, I, A> party, IPlayerConfigManager configs, Predicate<IPartyMember> shouldUpdateCommandsForMember, Component massMessageContent) {
		String partyName = party.getDefaultName();
		String partyCustomName = configs.getLoadedConfig(party.getOwner().getUUID()).getEffective(PlayerConfigOptions.PARTY_NAME);
		if(!partyCustomName.isEmpty())
			partyName = partyCustomName;
		Component partyNameComponent = Component.literal("[" + partyName + "] ").withStyle(s -> s.withColor(ChatFormatting.GOLD).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(party.getDefaultName()))));
		Component massMessage = Component.literal("");
		massMessage.getSiblings().add(partyNameComponent);

		MinecraftServer server = serverData.getServer();
		AdaptiveLocalizer adaptiveLocalizer = serverData.getAdaptiveLocalizer();
		PlayerList playerList = server.getPlayerList();
		if(playerList.getPlayerCount() > party.getMemberCount()) {
			Iterator<M> iterator = party.getTypedMemberInfoStream().iterator();
			while(iterator.hasNext()) {
				M memberInfo = iterator.next();
				ServerPlayer onlinePlayer = playerList.getPlayer(memberInfo.getUUID());
				if(onlinePlayer != null) {
					massMessage.getSiblings().clear();
					massMessage.getSiblings().add(partyNameComponent);
					massMessage.getSiblings().add(adaptiveLocalizer.getFor(onlinePlayer, massMessageContent));
					onOnlineMember(commandCasterId, server, memberInfo, onlinePlayer, shouldUpdateCommandsForMember, massMessage);
				}
			}
		} else {
			for (ServerPlayer onlinePlayer : playerList.getPlayers()) {
				M memberInfo = party.getMemberInfo(onlinePlayer.getUUID());
				if(memberInfo != null) {
					massMessage.getSiblings().clear();
					massMessage.getSiblings().add(partyNameComponent);
					massMessage.getSiblings().add(adaptiveLocalizer.getFor(onlinePlayer, massMessageContent));
					onOnlineMember(commandCasterId, server, memberInfo, onlinePlayer, shouldUpdateCommandsForMember, massMessage);
				}
			}
		}
	}

}
