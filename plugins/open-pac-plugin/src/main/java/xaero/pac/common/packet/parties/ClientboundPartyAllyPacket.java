package xaero.pac.common.packet.parties;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.FriendlyByteBuf;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.client.parties.party.ClientPartyAllyInfo;
import xaero.pac.client.parties.party.IClientParty;
import xaero.pac.common.server.lazypacket.LazyPacket;

import java.util.UUID;
import java.util.function.Function;

public class ClientboundPartyAllyPacket extends LazyPacket<ClientboundPartyAllyPacket> {

	public static final Encoder<ClientboundPartyAllyPacket> ENCODER = new Encoder<>();
	public static final Decoder DECODER = new Decoder();

	private final Action action;
	private final ClientPartyAllyInfo allyInfo;

	public ClientboundPartyAllyPacket(Action action, ClientPartyAllyInfo allyInfo) {
		super();
		this.action = action;
		this.allyInfo = allyInfo;
	}

	@Override
	protected Function<FriendlyByteBuf, ClientboundPartyAllyPacket> getDecoder() {
		return DECODER;
	}

	@Override
	protected void writeOnPrepare(FriendlyByteBuf u) {
		CompoundTag tag = new CompoundTag();
		tag.putUUID("i", allyInfo.getAllyId());
		tag.putString("n", allyInfo.getAllyName());
		tag.putString("dn", allyInfo.getAllyDefaultName());
		tag.putString("a", action.toString());
		u.writeNbt(tag);
	}

	public static class Decoder implements Function<FriendlyByteBuf, ClientboundPartyAllyPacket> {

		@Override
		public ClientboundPartyAllyPacket apply(FriendlyByteBuf input) {
			try {
				if(input.readableBytes() > 102400)
					return null;
				CompoundTag tag = (CompoundTag) input.readNbt(NbtAccounter.unlimitedHeap());
				if(tag == null)
					return null;
				UUID allyId = tag.getUUID("i");
				String allyName = tag.getString("n");
				if(allyName.length() > 512)
					return null;
				String allyDefaultName = tag.getString("dn");
				if(allyDefaultName.length() > 512)
					return null;
				String actionString = tag.getString("a");
				if(actionString.isEmpty() || actionString.length() > 128)
					return null;
				Action action = Action.valueOf(actionString);
				return new ClientboundPartyAllyPacket(action, new ClientPartyAllyInfo(allyId, allyName, allyDefaultName));
			} catch(Throwable t) {
				OpenPartiesAndClaims.LOGGER.error("invalid packet ", t);
				return null;
			}
		}

	}

	public static class ClientHandler extends Handler<ClientboundPartyAllyPacket> {

		@Override
		public void handle(ClientboundPartyAllyPacket t) {
			IClientParty<?, ?, ?> party = OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClientPartyStorage().getParty();
			if(party == null)
				return;
			if(t.action == Action.ADD)
				party.addAllyParty(t.allyInfo.getAllyId());
			else
				party.removeAllyParty(t.allyInfo.getAllyId());
			if(t.action == Action.UPDATE)
				party.addAllyParty(t.allyInfo.getAllyId());

			if(t.action == Action.REMOVE)
				OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClientPartyStorage().getAllyInfoStorage().remove(t.allyInfo.getAllyId());
			else
				OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClientPartyStorage().getAllyInfoStorage().add(t.allyInfo);

		}

	}

	@Override
	public String toString() {
		return String.format("[%s, %s, %s]", action, allyInfo.getAllyName(), allyInfo.getAllyDefaultName());
	}

	public enum Action {
		ADD,
		REMOVE,
		UPDATE
	}

}
