package xaero.pac.common.packet.claims;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import xaero.pac.common.claims.ClaimsManager;
import xaero.pac.common.claims.player.request.ClaimActionRequest;
import xaero.pac.common.server.player.data.ServerPlayerData;
import xaero.pac.common.server.player.data.api.ServerPlayerDataAPI;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class ServerboundClaimActionRequestPacket {

	private final ClaimActionRequest request;

	public ServerboundClaimActionRequestPacket(ClaimActionRequest request) {
		super();
		this.request = request;
	}

	public static class Codec implements BiConsumer<ServerboundClaimActionRequestPacket, FriendlyByteBuf>, Function<FriendlyByteBuf, ServerboundClaimActionRequestPacket> {

		@Override
		public ServerboundClaimActionRequestPacket apply(FriendlyByteBuf input) {
			try {
				if(input.readableBytes() > 1024)
					return null;
				CompoundTag tag = (CompoundTag) input.readNbt(NbtAccounter.unlimitedHeap());
				if(tag == null)
					return null;
				byte actionByte = tag.getByte("a");
				ClaimsManager.Action action;
				try {
					action = ClaimsManager.Action.values()[actionByte];
				} catch(ArrayIndexOutOfBoundsException aioobe) {
					return null;
				}
				int left = tag.getInt("l");
				int top = tag.getInt("t");
				int right = tag.getInt("r");
				int bottom = tag.getInt("b");
				if(left > right || top > bottom)
					return null;
				boolean byServer = tag.getBoolean("s");
				return new ServerboundClaimActionRequestPacket(new ClaimActionRequest(action, left, top, right, bottom, byServer));
			} catch(Throwable t) {
				return null;
			}
		}

		@Override
		public void accept(ServerboundClaimActionRequestPacket t, FriendlyByteBuf u) {
			CompoundTag tag = new CompoundTag();
			tag.putByte("a", (byte) t.request.getAction().ordinal());
			tag.putInt("l", t.request.getLeft());
			tag.putInt("t", t.request.getTop());
			tag.putInt("r", t.request.getRight());
			tag.putInt("b", t.request.getBottom());
			tag.putBoolean("s", t.request.isByServer());
			u.writeNbt(tag);
		}

	}

	public static class ServerHandler implements BiConsumer<ServerboundClaimActionRequestPacket,ServerPlayer> {

		@Override
		public void accept(ServerboundClaimActionRequestPacket t, ServerPlayer serverPlayer) {
			ServerPlayerData playerData = (ServerPlayerData) ServerPlayerDataAPI.from(serverPlayer);
			playerData.getClaimActionRequestHandler().onReceive(serverPlayer, t.request);
		}

	}

}
