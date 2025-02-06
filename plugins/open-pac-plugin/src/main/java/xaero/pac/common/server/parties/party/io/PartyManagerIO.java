package xaero.pac.common.server.parties.party.io;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.parties.party.ally.PartyAlly;
import xaero.pac.common.server.config.ServerConfig;
import xaero.pac.common.server.io.FileIOHelper;
import xaero.pac.common.server.io.FilePathConfig;
import xaero.pac.common.server.io.IOThreadWorker;
import xaero.pac.common.server.io.ObjectManagerIO;
import xaero.pac.common.server.io.serialization.SerializationHandler;
import xaero.pac.common.server.io.serialization.SerializedDataFileIO;
import xaero.pac.common.server.parties.party.PartyManager;
import xaero.pac.common.server.parties.party.ServerParty;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public final class PartyManagerIO<S> extends ObjectManagerIO<S, String, ServerParty, PartyManager> {

	private final Path partiesPath;
	private final boolean partiesEnabled;

	private PartyManagerIO(String extension, SerializationHandler<S, String, ServerParty, PartyManager> serializationHandler, SerializedDataFileIO<S, String> serializedDataFileIO, IOThreadWorker ioThreadWorker,
			MinecraftServer server, PartyManager manager, FileIOHelper fileIOHelper) {
		super(serializationHandler, serializedDataFileIO, ioThreadWorker, server, extension, manager, fileIOHelper);
		partiesPath = server.getWorldPath(LevelResource.ROOT).resolve("data").resolve(OpenPartiesAndClaims.MOD_ID).resolve("parties");
		partiesEnabled = ServerConfig.CONFIG.partiesEnabled.get();
	}

	@Override
	protected Stream<FilePathConfig> getObjectFolderPaths() {
		return Stream.of(new FilePathConfig(partiesPath, false));
	}

	@Override
	public void load() {
		if(!partiesEnabled)
			return;
		OpenPartiesAndClaims.LOGGER.info("Loading parties...");
		super.load();
		manager.getTypedAllStream().forEach(p -> {
			Iterator<PartyAlly> allyPartyIterator = p.getAllyPartiesIterator();
			List<UUID> alliesToRemove = null;
			while(allyPartyIterator.hasNext()) {
				UUID allyId = allyPartyIterator.next().getPartyId();
				ServerParty allyParty = manager.getPartyById(allyId);
				if(allyParty == null) {
					if(alliesToRemove == null)
						alliesToRemove = new ArrayList<>();
					alliesToRemove.add(allyId);
				} else
					p.updateAllyNameMap(allyParty.getId(), allyParty.getOwner().getUsername());
			}
			if(alliesToRemove != null)
				alliesToRemove.forEach(p::removeAllyParty);
		});
		manager.setLoaded(true);
		OpenPartiesAndClaims.LOGGER.info("Loaded parties!");
	}

	@Override
	public boolean save() {
		if(!partiesEnabled)
			return true;
		OpenPartiesAndClaims.LOGGER.debug("Saving parties...");
		return super.save();
	}

	@Override
	protected Path getFilePath(ServerParty object, String fileName) {
		return partiesPath.resolve(fileName + this.fileExtension);
	}

	@Override
	public void delete(ServerParty object) {
		if(!partiesEnabled)
			return;
		super.delete(object);
	}

	@Override
	public void onServerTick() {
		super.onServerTick();
	}

	@Override
	protected String getObjectId(String fileNameNoExtension, Path file, FilePathConfig filePathConfig) {
		return fileNameNoExtension;
	}

	@Override
	protected void onObjectLoad(ServerParty loadedObject) {
		manager.addParty(loadedObject);
	}

	public static final class Builder<S> extends ObjectManagerIO.Builder<S, String, ServerParty, PartyManager, Builder<S>>{

		private Builder() {
		}

		@Override
		public Builder<S> setDefault() {
			super.setDefault();
			return this;
		}

		@Override
		protected PartyManagerIO<S> buildInternally() {
			return new PartyManagerIO<>(fileExtension, serializationHandler, serializedDataFileIO, ioThreadWorker, server, manager, fileIOHelper);
		}

		@Override
		public PartyManagerIO<S> build() {
			return (PartyManagerIO<S>) super.build();
		}

		public static <S> Builder<S> begin() {
			return new Builder<S>().setDefault();
		}

	}

}
