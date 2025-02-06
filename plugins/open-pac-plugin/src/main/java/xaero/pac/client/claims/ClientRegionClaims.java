package xaero.pac.client.claims;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.SimpleBitStorage;
import xaero.pac.client.claims.player.ClientPlayerClaimInfoManager;
import xaero.pac.common.claims.PlayerChunkClaimHolder;
import xaero.pac.common.claims.RegionClaims;
import xaero.pac.common.claims.player.PlayerChunkClaim;
import xaero.pac.common.claims.storage.RegionClaimsPaletteStorage;
import xaero.pac.common.server.player.config.IPlayerConfigManager;

public final class ClientRegionClaims extends RegionClaims<ClientPlayerClaimInfoManager, ClientRegionClaims> implements IClientRegionClaims {

	private ClientRegionClaims(ResourceLocation dimension, int x, int z, RegionClaimsPaletteStorage storage) {
		super(dimension, x, z, storage);
	}

	public void onRegionClaim(ClientRegionClaims otherRegion /*new region or the old region when "reversed"*/, ClientPlayerClaimInfoManager playerClaimsManager, IPlayerConfigManager configManager, boolean reverse) {
		ClientRegionClaims oldRegion = reverse ? otherRegion : this;
		ClientRegionClaims newRegion = reverse ? this : otherRegion;
		int x = getX();
		int z = getZ();
		for(int i = 0; i < 32; i++){
			for(int j = 0; j < 32; j++){
				PlayerChunkClaim claim = oldRegion == null ? null : oldRegion.get(i, j);
				PlayerChunkClaim newClaim = newRegion == null ? null : newRegion.get(i, j);
				onClaimSet((x << 5) | i, (z << 5) | j, claim, newClaim, playerClaimsManager, configManager);
			}
		}
	}

	public static final class Builder extends RegionClaims.Builder<ClientPlayerClaimInfoManager, ClientRegionClaims, Builder>{

		public static Builder begin() {
			return new Builder().setDefault();
		}

		@Override
		public Builder setDefault() {
			super.setDefault();
			return self;
		}

		@Override
		public Builder setStorage(RegionClaimsPaletteStorage storage) {
			return super.setStorage(storage);
		}

		@Override
		public ClientRegionClaims build() {
			if(storage == null)
				setStorage(new RegionClaimsPaletteStorage(new Object2IntOpenHashMap<>(), null, Lists.newArrayList((PlayerChunkClaimHolder)null), new SimpleBitStorage(1, 1024), false));
			return (ClientRegionClaims) super.build();
		}

		@Override
		protected ClientRegionClaims buildInternally() {
			return new ClientRegionClaims(dimension, x, z, storage);
		}

	}

}
