package xaero.pac.common.packet.claims;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.BitStorage;
import net.minecraft.util.SimpleBitStorage;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.server.lazypacket.LazyPacket;

import java.util.function.Function;

public class ClientboundClaimsRegionPacket extends LazyPacket<ClientboundClaimsRegionPacket> {

	public static final Encoder<ClientboundClaimsRegionPacket> ENCODER = new Encoder<>();
	public static final Decoder DECODER = new Decoder();

	private final int x;
	private final int z;
	private final int[] paletteInts;
	private final int bits;
	private final long[] data;

	public ClientboundClaimsRegionPacket(int x, int z, int[] paletteInts, int bits, long[] data) {
		super();
		this.x = x;
		this.z = z;
		this.paletteInts = paletteInts;
		this.bits = bits;
		this.data = data;
	}

	@Override
	protected Function<FriendlyByteBuf, ClientboundClaimsRegionPacket> getDecoder() {
		return DECODER;
	}

	@Override
	protected void writeOnPrepare(FriendlyByteBuf dest) {
		CompoundTag nbt = new CompoundTag();
		nbt.putInt("x", x);
		nbt.putInt("z", z);
		nbt.putIntArray("p", paletteInts);
		nbt.putByte("b", (byte)bits);
		nbt.putLongArray("d", data);
		dest.writeNbt(nbt);
	}

	public static class Decoder implements Function<FriendlyByteBuf, ClientboundClaimsRegionPacket> {

		@Override
		public ClientboundClaimsRegionPacket apply(FriendlyByteBuf input) {
			try {
				if(input.readableBytes() > 16384)
					return null;
				CompoundTag nbt = (CompoundTag) input.readNbt(NbtAccounter.unlimitedHeap());
				if(nbt == null)
					return null;
				int x = nbt.getInt("x");
				int z = nbt.getInt("z");
				int[] paletteInts = nbt.getIntArray("p");
				int bits = nbt.getByte("b");
				long[] data = nbt.getLongArray("d");
				return new ClientboundClaimsRegionPacket(x, z, paletteInts, bits, data);
			} catch(Throwable t) {
				OpenPartiesAndClaims.LOGGER.error("invalid packet", t);
				return null;
			}
		}

	}

	public static class ClientHandler extends Handler<ClientboundClaimsRegionPacket> {

		@Override
		public void handle(ClientboundClaimsRegionPacket t) {
			BitStorage regionData = new SimpleBitStorage(t.bits, 1024, t.data);
			OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getClientClaimsSyncHandler().onRegion(t.x, t.z, t.paletteInts, regionData);
		}

	}

}
