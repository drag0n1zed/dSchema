package xaero.pac.common.server.io;

import net.minecraft.server.MinecraftServer;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.server.io.exception.IOThreadWorkerException;
import xaero.pac.common.server.io.serialization.SerializationHandler;
import xaero.pac.common.server.io.serialization.SerializedDataFileIO;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.concurrent.CompletionException;
import java.util.stream.Stream;

public abstract class ObjectManagerIO
<
	S,
	I,
	T extends ObjectManagerIOObject,
	M extends ObjectManagerIOManager<T, M>
> {

	private final int MAX_PER_TICK = 5;
	protected final M manager;
	protected final String fileExtension;
	protected final SerializationHandler<S, I, T, M> serializationHandler;
	private final SerializedDataFileIO<S,I> serializedDataFileIO;
	private final IOThreadWorker ioThreadWorker;
	protected final MinecraftServer server;
	private final FileIOHelper fileIOHelper;

	@SuppressWarnings("unchecked")
	protected ObjectManagerIO(SerializationHandler<S, I, T, M> serializationHandler, SerializedDataFileIO<S,I> serializedDataFileIO, IOThreadWorker ioThreadWorker, MinecraftServer server, String fileExtension, M manager, FileIOHelper fileIOHelper) {
		this.serializationHandler = serializationHandler;
		this.serializedDataFileIO = serializedDataFileIO;
		this.ioThreadWorker = ioThreadWorker;
		this.server = server;
		this.fileExtension = fileExtension;
		this.manager = manager;
		this.fileIOHelper = fileIOHelper;
	}

	protected abstract Stream<FilePathConfig> getObjectFolderPaths();

	public void load() {
		Stream<FilePathConfig> folderPaths = getObjectFolderPaths();
		folderPaths.forEach(folderPathConfig -> loadInFolder(folderPathConfig.getPath(), folderPathConfig));
	}

	private void loadInFolder(Path folderPath, FilePathConfig filePathConfig){
		try {
			Files.createDirectories(folderPath);
			try(Stream<Path> contents = Files.list(folderPath)){
				contents.forEach
						(fd -> {
							if(Files.isDirectory(fd))
								return;
							T loadedObject = loadFile(fd, filePathConfig, true);
							if(loadedObject != null)
								onObjectLoad(loadedObject);
						});
			}
			if(filePathConfig.isLoadRecursively()) {
				//after loading all files in this folder, go deeper
				try (Stream<Path> contents = Files.list(folderPath)) {
					contents.forEach
							(fd -> {
								if (!Files.isDirectory(fd))
									return;
								loadInFolder(fd, filePathConfig);
							});
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected abstract void onObjectLoad(T loadedObject);

	protected abstract I getObjectId(String fileNameNoExtension, Path file, FilePathConfig filePathConfig);

	protected T loadFile(Path file, FilePathConfig filePathConfig, boolean backupOnError) {
		String fileName = file.getFileName().toString();
		if(!fileName.endsWith(this.fileExtension))
			return null;
		I id = getObjectId(fileName.substring(0, fileName.lastIndexOf('.')), file, filePathConfig);
		try {
			S serializedData = ioThreadWorker.get(() -> readSerializedData(id, file, serializedDataFileIO, 20));
			T object = serializationHandler.deserialize(id, manager, serializedData);
			return object;
		} catch(Throwable e) {
			if(e instanceof CompletionException && e.getCause() instanceof IOException)
				throw e;//it means that a file is completely inaccessible even after multiple attempts (can't even back it up in that case)
			OpenPartiesAndClaims.LOGGER.error(String.format("Exception loading data from file %s", fileName), e);
			if(backupOnError && (!(e instanceof CompletionException) || !(e.getCause() instanceof IOThreadWorkerException))) {//otherwise the file at hand is not the reason and the io thread worker is dead anyway
				ioThreadWorker.get(() -> {
					int backupAttempts = 5;
					while(true) {
						try {
							Path backupPath = fileIOHelper.quickFileBackupMove(file);
							OpenPartiesAndClaims.LOGGER.error(String.format("The file was ignored and backed up to %s", backupPath));
							break;
						} catch (IOException e2) {
							backupAttempts--;
							if(backupAttempts <= 0) {
								OpenPartiesAndClaims.LOGGER.error(String.format("IO Exception trying to backup unusable file %s", fileName), e2);
								throw e;
							}
							try {
								Thread.sleep(50);
							} catch (InterruptedException e1) {
							}
						}
					}
					return true;
				});
			}
		}
		return null;
	}

	public boolean save() {
		Iterator<T> iter = manager.getToSave().iterator();
		int saves = 0;
		long before = System.currentTimeMillis();
		while(iter.hasNext()){
			if(saves++ >= MAX_PER_TICK || saves > 1 && System.currentTimeMillis() - before > 10)
				return false;
			T object = iter.next();
			iter.remove();
			if(object.isDirty()) {//not guaranteed!
				Path filePath = getFilePath(object, object.getFileName());
				saveFile(object, filePath);
			}
		}
		return true;
	}

	protected abstract Path getFilePath(T object, String fileName);

	protected void saveFile(T object, Path filePath) {
//		OpenPartiesAndClaims.LOGGER.info("Saving file " + filePath);
		object.setDirty(false);
		try {
			S serializedData = serializationHandler.serialize(object);
			ioThreadWorker.enqueue(() -> {
				try {
					writeSerializedData(filePath, serializedDataFileIO, serializedData, fileIOHelper, 20);
				} catch(Throwable e) {
					OpenPartiesAndClaims.LOGGER.error(String.format("Exception saving data to file %s", filePath.getFileName().toString()), e);
				}
			});
		} catch(Throwable e) {
			OpenPartiesAndClaims.LOGGER.error(String.format("Exception saving data to file %s", filePath.getFileName().toString()), e);
		}
	}

	public void onServerTick() {

	}

	public void delete(T object) {
		Path filePath = getFilePath(object, object.getFileName());
		ioThreadWorker.enqueue(() -> {
			try {
				tryToDelete(filePath, 20);
			} catch(Throwable e) {
				OpenPartiesAndClaims.LOGGER.error(String.format("Exception deleting file %s", filePath.getFileName().toString()), e);
			}
		});
	}

	private static void tryToDelete(Path filePath, int extraAttempts) throws IOException {
		try {
			Files.deleteIfExists(filePath);
		} catch (IOException e) {
			if(extraAttempts == 0) {
				OpenPartiesAndClaims.LOGGER.error("IO exception while trying to delete data", e);
				throw e;
			} else {
				OpenPartiesAndClaims.LOGGER.info("IO exception while trying to delete data at " + filePath);
				OpenPartiesAndClaims.LOGGER.info("Retrying... Attempts left: " + extraAttempts);
				try {
					Thread.sleep(50);
				} catch (InterruptedException e1) {
					OpenPartiesAndClaims.LOGGER.warn("Wait interrupted...", e1);
				}
				tryToDelete(filePath, extraAttempts - 1);
			}
		}
	}

	private static <S,I> S readSerializedData(I id, Path fd, SerializedDataFileIO<S,I> serializedDataFileIO, int extraAttempts) {
		S serializedData;
		try {//2 try clauses so that the "finally" block of the inner one is called before handling the exception
			try(FileInputStream fileInput = new FileInputStream(fd.toFile()); BufferedInputStream bufferedInput = new BufferedInputStream(fileInput)){
				serializedData = serializedDataFileIO.read(id, bufferedInput);
			}
		} catch (IOException e) {
			if(extraAttempts == 0) {
				OpenPartiesAndClaims.LOGGER.error("IO exception while trying to load data", e);
				throw new RuntimeException(e);//purposely different from the other methods to avoid try catch around the readSerializedData call
			} else {
				OpenPartiesAndClaims.LOGGER.info("IO exception while trying to load data from " + fd);
				OpenPartiesAndClaims.LOGGER.info("Retrying... Attempts left: " + extraAttempts);
				try {
					Thread.sleep(50);
				} catch (InterruptedException e1) {
					OpenPartiesAndClaims.LOGGER.warn("Wait interrupted...", e1);
				}
				serializedData = readSerializedData(id, fd, serializedDataFileIO, extraAttempts - 1);
			}
		}
		return serializedData;
	}

	private static <S,I> void writeSerializedData(Path fd, SerializedDataFileIO<S,I> serializedDataFileIO, S serializedData, FileIOHelper fileIOHelper, int extraAttempts) throws IOException {
		Path tempPath = fd.resolveSibling(fd.getFileName().toString() + ".temp");
		try {//2 try clauses so that the "finally" block of the inner one is called before handling the exception
			Files.createDirectories(fd.getParent());
			try(FileOutputStream fileOutput = new FileOutputStream(tempPath.toFile()); BufferedOutputStream bufferedOutput = new BufferedOutputStream(fileOutput)){
				serializedDataFileIO.write(bufferedOutput, serializedData);
				fileIOHelper.safeMoveAndReplace(tempPath, fd, true);
			}
		} catch (IOException e) {
			if(extraAttempts == 0) {
				OpenPartiesAndClaims.LOGGER.error("IO exception while trying to save data", e);
				throw e;
			} else {
				OpenPartiesAndClaims.LOGGER.info("IO exception while trying to save data to " + fd);
				OpenPartiesAndClaims.LOGGER.info("Retrying... Attempts left: " + extraAttempts);
				try {
					Thread.sleep(50);
				} catch (InterruptedException e1) {
					OpenPartiesAndClaims.LOGGER.warn("Wait interrupted...", e1);
				}
				writeSerializedData(fd, serializedDataFileIO, serializedData, fileIOHelper, extraAttempts - 1);
			}
		}
	}

	public static abstract class Builder <
		S,
		I,
		T extends ObjectManagerIOObject,
		M extends ObjectManagerIOManager<T, M>,
		B extends Builder<S,I,T,M,B>
	> {

		protected B self;
		protected String fileExtension;
		protected SerializationHandler<S, I, T, M> serializationHandler;
		protected SerializedDataFileIO<S,I> serializedDataFileIO;
		protected IOThreadWorker ioThreadWorker;
		protected MinecraftServer server;
		protected FileIOHelper fileIOHelper;
		protected M manager;

		protected Builder() {
			this.self = (B) this;
		}

		public B setFileExtension(String fileExtension) {
			this.fileExtension = fileExtension;
			return self;
		}

		public B setSerializationHandler(SerializationHandler<S, I, T, M> serializationHandler) {
			this.serializationHandler = serializationHandler;
			return self;
		}

		public B setSerializedDataFileIO(SerializedDataFileIO<S, I> serializedDataFileIO) {
			this.serializedDataFileIO = serializedDataFileIO;
			return self;
		}

		public B setIoThreadWorker(IOThreadWorker ioThreadWorker) {
			this.ioThreadWorker = ioThreadWorker;
			return self;
		}

		public B setServer(MinecraftServer server) {
			this.server = server;
			return self;
		}

		public B setFileIOHelper(FileIOHelper fileIOHelper) {
			this.fileIOHelper = fileIOHelper;
			return self;
		}

		public B setManager(M manager) {
			this.manager = manager;
			return self;
		}

		public B setDefault() {
			setFileExtension(null);
			setSerializationHandler(null);
			setSerializedDataFileIO(null);
			setIoThreadWorker(null);
			setServer(null);
			setFileIOHelper(null);
			setManager(null);
			return self;
		}

		public ObjectManagerIO<S,I,T,M> build() {
			if (fileExtension == null || serializationHandler == null ||
					serializedDataFileIO == null || ioThreadWorker == null ||
					server == null || fileIOHelper == null || manager == null)
				throw new IllegalStateException();
			return buildInternally();
		}

		protected abstract ObjectManagerIO<S,I,T,M> buildInternally();

	}

}
