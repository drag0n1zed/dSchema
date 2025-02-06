package xaero.pac.client.parties.party;

import xaero.pac.common.parties.party.Party;
import xaero.pac.common.parties.party.ally.PartyAlly;
import xaero.pac.common.parties.party.member.PartyInvite;
import xaero.pac.common.parties.party.member.PartyMember;
import xaero.pac.common.util.linked.LinkedChain;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ClientParty extends Party implements IClientParty<PartyMember, PartyInvite, PartyAlly> {

	protected ClientParty(PartyMember owner, UUID id, List<PartyMember> staffInfo, Map<UUID, PartyMember> memberInfo,
						  LinkedChain<PartyMember> linkedMemberInfo, Map<UUID, PartyInvite> invitedPlayers, LinkedChain<PartyInvite> linkedInvitedPlayers, Map<UUID, PartyAlly> allyParties, LinkedChain<PartyAlly> linkedAllyParties) {
		super(owner, id, staffInfo, memberInfo, linkedMemberInfo, invitedPlayers, linkedInvitedPlayers, allyParties, linkedAllyParties);
	}

	public static final class Builder extends Party.Builder {

		private Builder() {
		}

		@Override
		public Builder setDefault() {
			super.setDefault();
			return this;
		}

		@Override
		public Builder setOwner(PartyMember owner) {
			super.setOwner(owner);
			return this;
		}

		@Override
		public Builder setId(UUID id) {
			super.setId(id);
			return this;
		}

		@Override
		public Builder setMemberInfo(Map<UUID, PartyMember> memberInfo) {
			super.setMemberInfo(memberInfo);
			return this;
		}

		@Override
		public Builder setInvitedPlayers(Map<UUID, PartyInvite> invitedPlayers) {
			super.setInvitedPlayers(invitedPlayers);
			return this;
		}

		@Override
		public Builder setAllyParties(Map<UUID, PartyAlly> allyParties) {
			super.setAllyParties(allyParties);
			return this;
		}

		@Override
		public ClientParty build() {
			return (ClientParty) super.build();
		}

		public static Builder begin() {
			return new Builder().setDefault();
		}

		@Override
		protected ClientParty buildInternally(List<PartyMember> staffInfo, LinkedChain<PartyMember> linkedMemberInfo, LinkedChain<PartyInvite> linkedInvitedPlayers, LinkedChain<PartyAlly> linkedAllyParties) {
			return new ClientParty(owner, id, staffInfo, memberInfo, linkedMemberInfo, invitedPlayers, linkedInvitedPlayers, allyParties, linkedAllyParties);
		}

	}

}
