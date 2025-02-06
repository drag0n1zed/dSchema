package xaero.pac.common.packet.config;

import net.minecraft.nbt.CompoundTag;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.client.player.config.IPlayerConfigClientStorage;
import xaero.pac.client.player.config.IPlayerConfigClientStorageManager;
import xaero.pac.client.player.config.IPlayerConfigStringableOptionClientStorage;
import xaero.pac.common.server.player.config.PlayerConfig;
import xaero.pac.common.server.player.config.api.PlayerConfigType;

public class ClientboundPlayerConfigRemoveSubPacket extends ClientboundPlayerConfigAbstractStatePacket {

	private final String subIdToRemove;

	public ClientboundPlayerConfigRemoveSubPacket(PlayerConfigType type, boolean otherPlayer, String subIdToRemove){
		super(type, otherPlayer, PlayerConfig.MAIN_SUB_ID);
		this.subIdToRemove = subIdToRemove;
	}

	public static class Codec extends ClientboundPlayerConfigAbstractStatePacket.Codec<ClientboundPlayerConfigRemoveSubPacket> {

		@Override
		protected ClientboundPlayerConfigRemoveSubPacket decode(CompoundTag nbt, PlayerConfigType type, boolean otherPlayer, String subId) {
			String subIdToRemove = nbt.getString("i");
			if(subIdToRemove.isEmpty() || subIdToRemove.length() > 100) {
				OpenPartiesAndClaims.LOGGER.info("Bad sub id!");
				return null;
			}
			return new ClientboundPlayerConfigRemoveSubPacket(type, otherPlayer, subIdToRemove);
		}

		@Override
		protected void encode(ClientboundPlayerConfigRemoveSubPacket packet, CompoundTag nbt) {
			nbt.putString("i", packet.subIdToRemove);
		}

		@Override
		protected int getExtraSizeLimit() {
			return 0;
		}

	}

	public static class ClientHandler extends ClientboundPlayerConfigAbstractStatePacket.ClientHandler<ClientboundPlayerConfigRemoveSubPacket> {

		@Override
		protected void accept(ClientboundPlayerConfigRemoveSubPacket t, IPlayerConfigClientStorageManager<IPlayerConfigClientStorage<IPlayerConfigStringableOptionClientStorage<?>>> playerConfigStorageManager, IPlayerConfigClientStorage<IPlayerConfigStringableOptionClientStorage<?>> storage) {
			storage.removeSubConfig(t.subIdToRemove);
		}

	}

}
