package xaero.pac.common.server.claims.player.io;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.storage.LevelResource;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.claims.player.PlayerChunkClaim;
import xaero.pac.common.claims.player.PlayerDimensionClaims;
import xaero.pac.common.server.claims.ServerClaimsManager;
import xaero.pac.common.server.claims.player.ServerPlayerClaimInfo;
import xaero.pac.common.server.claims.player.ServerPlayerClaimInfoManager;
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.io.FileIOHelper;
import xaero.pac.common.server.io.FilePathConfig;
import xaero.pac.common.server.io.IOThreadWorker;
import xaero.pac.common.server.io.ObjectManagerIO;
import xaero.pac.common.server.io.serialization.SerializationHandler;
import xaero.pac.common.server.io.serialization.SerializedDataFileIO;
import xaero.pac.common.server.player.config.IPlayerConfig;
import xaero.pac.common.server.player.config.PlayerConfig;

import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public final class PlayerClaimInfoManagerIO<S>
 extends ObjectManagerIO<S, UUID, ServerPlayerClaimInfo, ServerPlayerClaimInfoManager> {

	private final ServerClaimsManager serverClaimsManager;
	private final Path claimsFolderPath;
	private final boolean claimsEnabled;

	private PlayerClaimInfoManagerIO(String extension, SerializationHandler<S, UUID, ServerPlayerClaimInfo, ServerPlayerClaimInfoManager> serializationHandler, SerializedDataFileIO<S, UUID> serializedDataFileIO, IOThreadWorker ioThreadWorker, MinecraftServer server, ServerClaimsManager serverClaimsManager, ServerPlayerClaimInfoManager claimsManager, FileIOHelper fileIOHelper, Path claimsFolderPath) {
		super(serializationHandler, serializedDataFileIO, ioThreadWorker, server, extension, claimsManager, fileIOHelper);
		this.serverClaimsManager = serverClaimsManager;
		this.claimsFolderPath = claimsFolderPath;
		this.claimsEnabled = ServerConfig.CONFIG.claimsEnabled.get();
	}

	@Override
	protected Stream<FilePathConfig> getObjectFolderPaths() {
		return Stream.of(new FilePathConfig(claimsFolderPath, false));
	}

	@Override
	public void load() {
		if(!claimsEnabled)
			return;
		long before = System.currentTimeMillis();
		OpenPartiesAndClaims.LOGGER.info("Loading claims...");
		super.load();
		OpenPartiesAndClaims.LOGGER.info("Loaded claims in " + (System.currentTimeMillis() - before) + "ms");
		manager.onLoad();
		serverClaimsManager.onLoad();
	}

	@Override
	public boolean save() {
//		if(true)
//			return true;
		if(!claimsEnabled)
			return true;
		OpenPartiesAndClaims.LOGGER.debug("Saving claims!");
		return super.save();
	}

	@Override
	protected Path getFilePath(ServerPlayerClaimInfo object, String fileName) {
		return claimsFolderPath.resolve(fileName + this.fileExtension);
	}

	@Override
	public void delete(ServerPlayerClaimInfo object) {
		if(!claimsEnabled)
			return;
		super.delete(object);
	}

	@Override
	public void onServerTick() {
		super.onServerTick();
	}

	@Override
	protected UUID getObjectId(String fileNameNoExtension, Path file, FilePathConfig filePathConfig) {
		return UUID.fromString(fileNameNoExtension);
	}

	@Override
	protected void onObjectLoad(ServerPlayerClaimInfo loadedObject) {
		//the result is not stored anywhere directly but the info in it is forwarded to the claims manager, which updates the player claims manager
		ServerPlayerClaimInfo playerInfo = serverClaimsManager.getPlayerInfo(loadedObject.getPlayerId());
		IPlayerConfig playerConfig = playerInfo.getConfig();
		if(!Objects.equals(loadedObject.getPlayerId(), PlayerConfig.EXPIRED_CLAIM_UUID) && !Objects.equals(loadedObject.getPlayerId(), PlayerConfig.SERVER_CLAIM_UUID))
			playerInfo.setPlayerUsername(loadedObject.getPlayerUsername());
		playerInfo.setRegisteredActivity(loadedObject.getRegisteredActivity());
		loadedObject.getFullStream().forEach(
				e -> {
					ResourceLocation dim = e.getKey();
					PlayerDimensionClaims dimensionClaims = e.getValue();
					BiConsumer<PlayerChunkClaim, ChunkPos> claimConsumer = (claim, pos) -> {
						serverClaimsManager.claim(dim, loadedObject.getPlayerId(), claim.getSubConfigIndex(), pos.x,
								pos.z, claim.isForceloadable());
					};
					dimensionClaims.getTypedStream().forEach(posList -> {
						PlayerChunkClaim claim = posList.getClaimState();
						if(claim.getSubConfigIndex() != -1 && !playerConfig.subConfigExists(claim.getSubConfigIndex()))
							claim = new PlayerChunkClaim(claim.getPlayerId(), -1, claim.isForceloadable(), 0);//converting sub-claim to main claim
						final PlayerChunkClaim finalClaim = claim;
						posList.getStream().forEach(pos -> claimConsumer.accept(finalClaim, pos));
					});
				}
			);
	}

	public static final class Builder<S> extends ObjectManagerIO.Builder<S, UUID, ServerPlayerClaimInfo, ServerPlayerClaimInfoManager, Builder<S>>{

		private ServerClaimsManager serverClaimsManager;

		private Builder() {
		}

		@Override
		public Builder<S> setDefault() {
			super.setDefault();
			setServerClaimsManager(null);
			return this;
		}

		public Builder<S> setServerClaimsManager(ServerClaimsManager serverClaimsManager) {
			this.serverClaimsManager = serverClaimsManager;
			return this;
		}

		@Override
		public PlayerClaimInfoManagerIO<S> build() {
			if(serverClaimsManager == null)
				throw new IllegalStateException();
			setManager(serverClaimsManager.getPlayerClaimInfoManager());
			return (PlayerClaimInfoManagerIO<S>) super.build();
		}

		public PlayerClaimInfoManagerIO<S> buildInternally() {
			Path claimsFolderPath = server.getWorldPath(LevelResource.ROOT).resolve("data").resolve(OpenPartiesAndClaims.MOD_ID).resolve("player-claims");
			return new PlayerClaimInfoManagerIO<>(fileExtension, serializationHandler, serializedDataFileIO, ioThreadWorker, server, serverClaimsManager, manager, fileIOHelper, claimsFolderPath);
		}

		public static <S>Builder<S> begin() {
			return new Builder<S>().setDefault();
		}

	}

}
