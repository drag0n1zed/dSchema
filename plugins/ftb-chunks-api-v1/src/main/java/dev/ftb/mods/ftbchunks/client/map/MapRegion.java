package dev.ftb.mods.ftbchunks.client.map;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.ftb.mods.ftbchunks.FTBChunks;
import dev.ftb.mods.ftbchunks.client.FTBChunksClient;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.math.MathUtils;
import dev.ftb.mods.ftblibrary.math.XZ;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author LatvianModder
 */
public class MapRegion implements MapTask {
	public static final Color4I GRID_COLOR = Color4I.rgba(70, 70, 70, 50);

	public final MapDimension dimension;
	public final XZ pos;
	private MapRegionData data;
	private long lastDataAccess; // time of last access, so stale regions can be released to save memory
	private boolean isLoadingData;
	public boolean saveData;
	private NativeImage renderedMapImage;
	private boolean updateRenderedMapImage;
	public boolean updateRenderedMapTexture;
	private int renderedMapImageTextureId;
	public boolean mapImageLoaded;
	public boolean renderingMapImage;
	private final Map<XZ, MapChunk> chunks = new HashMap<>();

	public MapRegion(MapDimension d, XZ p) {
		dimension = d;
		pos = p;
		data = null;
		isLoadingData = false;
		saveData = false;
		renderedMapImage = null;
		updateRenderedMapImage = true;
		updateRenderedMapTexture = true;
		renderedMapImageTextureId = -1;
		mapImageLoaded = false;
	}

	public void created() {
		dimension.saveData = true;
	}

	public boolean isDataLoaded() {
		return data != null;
	}

	public long getLastDataAccess() {
		return lastDataAccess;
	}

	@NotNull
	public MapRegionData getDataBlocking() {
		synchronized (dimension.manager.lock) {
			return getDataBlockingNoSync();
		}
	}

	@NotNull
	public MapRegionData getDataBlockingNoSync() {
		if (data == null) {
			data = new MapRegionData(this);
			try {
				data.read();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		lastDataAccess = System.currentTimeMillis();
		return data;
	}

	@Nullable
	public MapRegionData getData() {
		if (data == null && !isLoadingData) {
			isLoadingData = true;
			FTBChunks.EXECUTOR.execute(this::getDataBlocking);
		}

		if (data != null) {
			lastDataAccess = System.currentTimeMillis();
		}

		return data;
	}

	public NativeImage getRenderedMapImage() {
		synchronized (dimension.manager.lock) {
			if (renderedMapImage == null) {
				renderedMapImage = new NativeImage(NativeImage.Format.RGBA, 512, 512, true);
				renderedMapImage.fillRect(0, 0, 512, 512, 0);
				update(false);
			}
		}

		if (updateRenderedMapImage && !renderingMapImage) {
			updateRenderedMapImage = false;
			mapImageLoaded = false;
			renderingMapImage = true;
			FTBChunks.EXECUTOR.execute(new RenderMapImageTask(this));
		}

		return renderedMapImage;
	}

	public int getRenderedMapImageTextureId() {
		if (renderedMapImageTextureId == -1) {
			renderedMapImageTextureId = FTBChunksClient.generateTextureId(512, 512);
		}

		getRenderedMapImage();

		if (updateRenderedMapTexture) {
			mapImageLoaded = false;

			Minecraft.getInstance().submit(() -> {
				RenderSystem.bindTexture(renderedMapImageTextureId);
				uploadRenderedMapImage();
				mapImageLoaded = true;
				FTBChunksClient.updateMinimap = true;
			});

			updateRenderedMapTexture = false;
		}

		return renderedMapImageTextureId;
	}

	public void release(boolean releaseMapChunks) {
		if (saveData && data != null) {
			try {
				data.write();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		if (releaseMapChunks) chunks.clear();
		data = null;
		lastDataAccess = 0L;
		isLoadingData = false;
		releaseMapImage();
	}

	public void releaseIfStale(long now, long releaseIntervalMillis) {
		if (now - lastDataAccess > releaseIntervalMillis && data != null) {
			if (pos.equals(XZ.regionFromChunk(Minecraft.getInstance().player.chunkPosition()))) {
				FTBChunks.LOGGER.debug("not releasing region {} / {} - player present", this, pos);
				return;  // don't release region where the player is
			}
			for (ChunkPos cp : FTBChunksClient.getPendingRerender()) {
				if (XZ.regionFromChunk(cp).equals(pos)) {
					FTBChunks.LOGGER.debug("not releasing region {} / {} - re-render pending", this, pos);
					return;  // don't release regions that have a re-render pending
				}
			}
			FTBChunks.LOGGER.debug("releasing data for region {} / {} (not accessed in last {} seconds)", this, pos, releaseIntervalMillis / 1000L);
			release(false);
		}
	}

	public void releaseMapImage() {
		synchronized (dimension.manager.lock) {
			if (renderedMapImage != null) {
				renderedMapImage.close();
				renderedMapImage = null;
			}
		}

		if (renderedMapImageTextureId != -1) {
			GlStateManager._deleteTexture(renderedMapImageTextureId);
			renderedMapImageTextureId = -1;
		}

		mapImageLoaded = false;
	}

	@Override
	public void runMapTask() throws Exception {
		if (data != null) {
			data.write();
		}
	}

	public void setRenderedMapImageRGBA(int x, int z, int col) {
		synchronized (dimension.manager.lock) {
			renderedMapImage.setPixelRGBA(x, z, col);
		}
	}

	private void uploadRenderedMapImage() {
		synchronized (dimension.manager.lock) {
			renderedMapImage.upload(0, 0, 0, false);
		}
	}

	public void afterImageRenderTask() {
		synchronized (dimension.manager.lock) {
			updateRenderedMapTexture = true;
			FTBChunksClient.updateMinimap = true;
			renderingMapImage = false;
		}
	}

	public void update(boolean save) {
		if (save) {
			saveData = true;
			dimension.saveData = true;
		}

		updateRenderedMapImage = true;
		updateRenderedMapTexture = true;
		FTBChunksClient.updateMinimap = true;
	}

	public MapRegion offset(int x, int z) {
		return dimension.getRegion(pos.offset(x, z));
	}

	public RegionSyncKey getSyncKey() {
		RegionSyncKey key = new RegionSyncKey();
		key.dim = dimension.dimension;
		key.x = pos.x;
		key.z = pos.z;
		key.random = MathUtils.RAND.nextInt();
		return key;
	}

	public double distToPlayer() {
		return MathUtils.distSq(pos.x * 512D + 256D, pos.z * 512D + 256D, Minecraft.getInstance().player.getX(), Minecraft.getInstance().player.getZ());
	}

	@Override
	public String toString() {
		return pos.toRegionString();
	}

	public MapChunk getMapChunk(XZ xz) {
		return chunks.get(xz);
	}

	public MapChunk getOrCreateMapChunk(XZ xz) {
		return chunks.computeIfAbsent(xz, p -> new MapChunk(this, p).created());
	}

	public MapChunk getChunkForAbsoluteChunkPos(XZ pos) {
		XZ effectivePos = pos.x != (pos.x & 31) || pos.z != (pos.z & 31) ?
				XZ.of(pos.x & 31, pos.z & 31) :
				pos;

		synchronized (dimension.manager.lock) {
			return getOrCreateMapChunk(effectivePos);
		}
	}

	public void addMapChunk(MapChunk mapChunk) {
		chunks.put(mapChunk.pos, mapChunk);
	}

	public Collection<MapChunk> getModifiedChunks() {
		return chunks.values().stream().filter(c -> c.modified > 0L).toList();
	}
}