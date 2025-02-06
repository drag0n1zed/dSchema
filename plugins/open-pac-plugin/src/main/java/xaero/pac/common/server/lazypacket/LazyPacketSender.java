package xaero.pac.common.server.lazypacket;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.packet.LazyPacketsConfirmationPacket;

import java.util.UUID;

public class LazyPacketSender {//sends packets over time with no unnecessary rushing

	private final static LazyPacketsConfirmationPacket CONFIRMATION_PACKET = new LazyPacketsConfirmationPacket();

	private final MinecraftServer server;
	private final LazyPacketManager manager;
	private final int bytesPerTickLimit;//can go over this because individual packets are sent in full
	private final float speedUpAtOccupancy;//how much [0.0;1.0] should the queue be allowed to fill up until we start speeding up (confirmation packets still in use though)
	private final int capacity;//at which point to fully stop being lazy (no limits at all)
	private final int bytesPerConfirmation;

	private LazyPacketSender(MinecraftServer server, LazyPacketManager manager, int bytesPerTickLimit, float speedUpAtOccupancy, int capacity, int bytesPerConfirmation) {
		super();
		this.server = server;
		this.manager = manager;
		this.bytesPerTickLimit = bytesPerTickLimit;
		this.speedUpAtOccupancy = speedUpAtOccupancy;
		this.capacity = capacity;
		this.bytesPerConfirmation = bytesPerConfirmation;
	}

	public void clearForPlayer(ServerPlayer player) {
		clearForPlayer(player, player.getUUID());
	}

	public void clearForPlayer(ServerPlayer player, UUID playerId) {
		manager.clearForPlayer(player, playerId);
	}

	public void enqueue(ServerPlayer player, LazyPacket<?> packet) {
		manager.enqueue(player, packet);
	}

	public void onConfirmation(ServerPlayer player) {
		manager.onConfirmation(player);
	}

	public void onServerTick() {
		int bytesSent = 0;
		int bytesToSend = bytesPerTickLimit;
		float occupancy = (float)manager.getTotalBytesEnqueued() / capacity;
		if(occupancy > speedUpAtOccupancy)
			bytesToSend *= occupancy / speedUpAtOccupancy;
		boolean overCapacity;
		while((overCapacity = manager.getTotalBytesEnqueued() > capacity) || bytesSent < bytesToSend) {
			PlayerLazyPacketManager playerPackets = manager.getNext(bytesPerConfirmation, overCapacity);
			if(playerPackets == null)
				break;
			LazyPacket<?> packet = playerPackets.getNext();
			bytesSent += packet.getPreparedSize();
			ServerPlayer player = server.getPlayerList().getPlayer(playerPackets.getPlayerId());
			if(player == null){
				//player disappeared without clearing packets
				//happens after crashes unrelated to this mod prevent certain events from firing
				clearForPlayer(null, playerPackets.getPlayerId());
				continue;
			}
			OpenPartiesAndClaims.INSTANCE.getPacketHandler().sendToPlayer(player, packet);

			manager.countSentBytes(packet);
			playerPackets.onSend(packet);
			if(!playerPackets.isWaitingForConfirmation() && playerPackets.needsConfirmation(bytesPerConfirmation)) {
				playerPackets.startWaitingForConfirmation();
				OpenPartiesAndClaims.INSTANCE.getPacketHandler().sendToPlayer(player, CONFIRMATION_PACKET);
			}
		}
//		if(bytesSent != 0 || manager.getTotalBytesEnqueued() != 0)
//			OpenPartiesAndClaims.LOGGER.info("sent " + bytesSent + " with " + manager.getTotalBytesEnqueued() + " left");
	}

	public boolean isClogged(ServerPlayer player){
		return manager.isClogged(player);
	}

	public static final class Builder {

		private MinecraftServer server;
		private int bytesPerTickLimit;
		private float speedUpAtOccupancy;
		private int capacity;
		private int bytesPerConfirmation;

		public Builder setDefault() {
			setServer(null);
			setBytesPerTickLimit(0);
			setSpeedUpAtOccupancy(0.125f);
			setCapacity(0);
			setBytesPerConfirmation(0);
			return this;
		}

		public Builder setServer(MinecraftServer server) {
			this.server = server;
			return this;
		}

		public Builder setBytesPerTickLimit(int bytesPerTickLimit) {
			this.bytesPerTickLimit = bytesPerTickLimit;
			return this;
		}

		public Builder setSpeedUpAtOccupancy(float speedUpAtOccupancy) {
			this.speedUpAtOccupancy = speedUpAtOccupancy;
			return this;
		}

		public Builder setCapacity(int capacity) {
			this.capacity = capacity;
			return this;
		}

		public Builder setBytesPerConfirmation(int bytesPerConfirmation) {
			this.bytesPerConfirmation = bytesPerConfirmation;
			return this;
		}

		public LazyPacketSender build() {
			if(server == null || bytesPerTickLimit == 0 || capacity == 0 || bytesPerConfirmation == 0)
				throw new IllegalStateException();
			return new LazyPacketSender(server, LazyPacketManager.Builder.begin().setServer(server).build(), bytesPerTickLimit, speedUpAtOccupancy, capacity, bytesPerConfirmation);
		}

		public static Builder begin() {
			return new Builder().setDefault();
		}

	}

}
