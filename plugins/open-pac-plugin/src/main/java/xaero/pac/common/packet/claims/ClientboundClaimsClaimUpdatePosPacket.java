package xaero.pac.common.packet.claims;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.FriendlyByteBuf;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.server.lazypacket.LazyPacket;

import java.util.function.Function;

public class ClientboundClaimsClaimUpdatePosPacket extends LazyPacket<ClientboundClaimsClaimUpdatePosPacket> {

	public static final Encoder<ClientboundClaimsClaimUpdatePosPacket> ENCODER = new Encoder<>();
	public static final Decoder DECODER = new Decoder();
	private final int x;
	private final int z;

	public ClientboundClaimsClaimUpdatePosPacket(int x, int z) {
		super();
		this.x = x;
		this.z = z;
	}

	@Override
	protected Function<FriendlyByteBuf, ClientboundClaimsClaimUpdatePosPacket> getDecoder() {
		return DECODER;
	}

	@Override
	protected void writeOnPrepare(FriendlyByteBuf u) {
		CompoundTag nbt = new CompoundTag();
		nbt.putInt("x", x);
		nbt.putInt("z", z);
		u.writeNbt(nbt);
	}

	@Override
	public String toString() {
		return String.format("[%d, %d]", x, z);
	}

	public static class Decoder implements Function<FriendlyByteBuf, ClientboundClaimsClaimUpdatePosPacket> {

		@Override
		public ClientboundClaimsClaimUpdatePosPacket apply(FriendlyByteBuf input) {
			try {
				if(input.readableBytes() > 1024)
					return null;
				CompoundTag nbt = (CompoundTag) input.readNbt(NbtAccounter.unlimitedHeap());
				if(nbt == null)
					return null;
				int x = nbt.getInt("x");
				int z = nbt.getInt("z");
				return new ClientboundClaimsClaimUpdatePosPacket(x, z);
			} catch(Throwable t) {
				OpenPartiesAndClaims.LOGGER.error("invalid packet", t);
				return null;
			}
		}

	}

	public static class ClientHandler extends Handler<ClientboundClaimsClaimUpdatePosPacket> {

		@Override
		public void handle(ClientboundClaimsClaimUpdatePosPacket t) {
			OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClientClaimsSyncHandler().onClaimUpdatePos(t.x, t.z);
		}

	}

}
