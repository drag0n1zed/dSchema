package xaero.pac.common.server.lazypacket;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;

public class LazyPacketManager {

	private final MinecraftServer server;
	private final List<UUID> orderHolder;
	private final Map<UUID, PlayerLazyPacketManager> storage;
	private int currentIndex;
	private int totalBytesEnqueued;

	private LazyPacketManager(MinecraftServer server, List<UUID> orderHolder, Map<UUID, PlayerLazyPacketManager> storage) {
		super();
		this.server = server;
		this.orderHolder = orderHolder;
		this.storage = storage;
	}

	private PlayerLazyPacketManager getForPlayer(UUID id) {
		PlayerLazyPacketManager result = storage.get(id);
		if(result == null) {
			result = PlayerLazyPacketManager.Builder.begin().setServer(server).setPlayerId(id).build();
			int insertionIndex = Collections.binarySearch(orderHolder, id);
			if(insertionIndex < 0)
				insertionIndex = -1 - insertionIndex;
			orderHolder.add(insertionIndex, id);
			storage.put(id, result);
		}
		return result;
	}

	public void clearForPlayer(ServerPlayer player, UUID playerId) {
		int insertionIndex = Collections.binarySearch(orderHolder, playerId);
		if(insertionIndex >= 0) {
			orderHolder.remove(insertionIndex);
			PlayerLazyPacketManager removedPlayerPackets = storage.remove(playerId);
			if(player != null)
				removedPlayerPackets.onDropped(player);
		}
	}

	public void enqueue(ServerPlayer player, LazyPacket<?> packet) {
		if(getForPlayer(player.getUUID()).enqueue(packet))
			totalBytesEnqueued += packet.prepare();
	}

	public void countSentBytes(LazyPacket<?> packet) {
		totalBytesEnqueued -= packet.getPreparedSize();
	}

	public int getTotalBytesEnqueued() {
		return totalBytesEnqueued;
	}

	public PlayerLazyPacketManager getNext(int bytesPerConfirmation, boolean overCapacity) {
		if(!orderHolder.isEmpty()) {
			for(int i = 0; i < orderHolder.size(); i++) {//check the whole list once at most
				currentIndex = currentIndex % orderHolder.size();
				UUID id = orderHolder.get(currentIndex);
				currentIndex++;
				PlayerLazyPacketManager playerPackets = getForPlayer(id);
				if(playerPackets.hasNext(overCapacity, this))
					return playerPackets;
			}
		}
		return null;
	}

	void onConfirmation(ServerPlayer player) {
		getForPlayer(player.getUUID()).clientConfirm();
	}

	public boolean isClogged(ServerPlayer player){
		return getForPlayer(player.getUUID()).isClogged();
	}

	public static final class Builder {

		private MinecraftServer server;

		public Builder setDefault() {
			setServer(null);
			return this;
		}

		public Builder setServer(MinecraftServer server) {
			this.server = server;
			return this;
		}

		public LazyPacketManager build() {
			if(server == null)
				throw new IllegalStateException();
			return new LazyPacketManager(server, new ArrayList<>(), new HashMap<>());
		}

		public static Builder begin() {
			return new Builder().setDefault();
		}

	}

}
