package dev.ftb.mods.ftbchunks.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import dev.ftb.mods.ftbchunks.FTBChunks;
import dev.ftb.mods.ftbchunks.client.map.*;
import dev.ftb.mods.ftbchunks.data.HeightUtils;
import dev.ftb.mods.ftbchunks.net.TeleportFromMapPacket;
import dev.ftb.mods.ftblibrary.config.StringConfig;
import dev.ftb.mods.ftblibrary.config.ui.EditConfigFromStringScreen;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.math.MathUtils;
import dev.ftb.mods.ftblibrary.math.XZ;
import dev.ftb.mods.ftblibrary.ui.*;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.StringUtils;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class LargeMapScreen extends BaseScreen {
	public Color4I backgroundColor = Color4I.rgb(0x202225);

	public RegionMapPanel regionPanel;
	public int zoom = 256;
	public MapDimension dimension;
	public int scrollWidth = 0;
	public int scrollHeight = 0;
	public int prevMouseX, prevMouseY;
	public int grabbed = 0;
	public boolean movedToPlayer = false;
	public Button claimChunksButton;
	public Button dimensionButton;
	public Button waypointManagerButton;
	public Button settingsButton;
	public Button serverSettingsButton;
	public Button clearDeathpointsButton;
	private boolean needIconRefresh;
	private final int minZoom;

	public LargeMapScreen() {
		regionPanel = new RegionMapPanel(this);

		var dim = MapDimension.getCurrent();
		if (dim == null) {
			FTBChunks.LOGGER.warn("Closed large map screen to prevent map dimension manager crash");
			this.closeGui(false);
		}

		dimension = dim;
		regionPanel.setScrollX(0D);
		regionPanel.setScrollY(0D);

		minZoom = determineMinZoom();
	}

	@Override
	public void onClosed() {
		super.onClosed();

		int autoRelease = FTBChunksClientConfig.AUTORELEASE_ON_MAP_CLOSE.get();
		if (autoRelease > 0 && dimension != null) {
			dimension.manager.scheduleRegionPurge(dimension);
		}
	}

	public int getRegionButtonSize() {
		return zoom * 2;
	}

	public void addZoom(double up) {
		int z = zoom;

		if (up > 0D) {
			zoom *= 2;
		} else {
			zoom /= 2;
		}

		zoom = Mth.clamp(zoom, minZoom, 1024);

		if (zoom != z) {
			grabbed = 0;
			double sx = regionPanel.regionX;
			double sy = regionPanel.regionZ;
			regionPanel.resetScroll();
			regionPanel.scrollTo(sx, sy);

			Minecraft.getInstance().mouseHandler.mouseGrabbed = true;
			Minecraft.getInstance().mouseHandler.releaseMouse();
		}
	}

	@Override
	public void addWidgets() {
		add(regionPanel);

		add(claimChunksButton = new SimpleTooltipButton(this, Component.translatable("ftbchunks.gui.claimed_chunks"), Icons.MAP,
				(b, m) -> new ChunkScreen().openGui(),
				Component.literal("[C]").withStyle(ChatFormatting.GRAY)));

		add(waypointManagerButton = new SimpleTooltipButton(this, Component.translatable("ftbchunks.gui.waypoints"), Icons.COMPASS,
				(b, m) -> new WaypointEditorScreen().openGui(),
				Component.literal("[").append(FTBChunksClient.waypointManagerKey.getTranslatedKeyMessage()).append(Component.literal("]")).withStyle(ChatFormatting.GRAY)));

		add(clearDeathpointsButton = new SimpleButton(this, Component.translatable("ftbchunks.gui.clear_deathpoints"), Icons.CLOSE,
				(b, m) -> {
					WaypointManager wpm = MapManager.inst.getDimension(dimension.dimension).getWaypointManager();
					if (wpm.removeIf(wp -> wp.type == WaypointType.DEATH)) {
						refreshWidgets();
					}
				}) {
			@Override
			public boolean shouldDraw() {
				return super.shouldDraw() && MapManager.inst.getDimension(dimension.dimension).getWaypointManager().hasDeathpoint();
			}

			@Override
			public boolean isEnabled() {
				return shouldDraw();
			}
		});

        /*
		add(syncButton = new SimpleButton(this, new TranslationTextComponent("ftbchunks.gui.sync"), Icons.REFRESH, (b, m) -> {
			dimension.sync();
		}));
		 */

		Component dimName = Component.literal(dimension.dimension.location().getPath().replace('_', ' '));
		add(dimensionButton = new SimpleButton(this, dimName, Icons.GLOBE,
				(b, m) -> cycleVisibleDimension(m)));

		add(settingsButton = new SimpleTooltipButton(this, Component.translatable("ftbchunks.gui.settings"), Icons.SETTINGS,
				(b, m) -> FTBChunksClientConfig.openSettings(new ScreenWrapper(this)),
				Component.literal("[S]").withStyle(ChatFormatting.GRAY))
		);

		if (Minecraft.getInstance().player.hasPermissions(2)) {
			add(serverSettingsButton = new SimpleTooltipButton(this, Component.translatable("ftbchunks.gui.settings.server"),
					Icons.SETTINGS.withTint(Color4I.rgb(0xA040FF)),
					(b, m) -> FTBChunksClientConfig.openServerSettings(new ScreenWrapper(this)),
					Component.literal("[Ctrl + S]").withStyle(ChatFormatting.GRAY)
			));
		}
	}

	private void cycleVisibleDimension(MouseButton m) {
		try {
			List<MapDimension> list = new ArrayList<>(dimension.manager.getDimensions().values());
			int i = list.indexOf(dimension);

			if (i != -1) {
				dimension = list.get(MathUtils.mod(i + (m.isLeft() ? 1 : -1), list.size()));
				refreshWidgets();
				movedToPlayer = false;
			}
		} catch (Exception ignored) {
		}
	}

	@Override
	public void alignWidgets() {
		// alliesButton.setPosAndSize(1, 19, 16, 16);
		// syncButton.setPosAndSize(1, 55, 16, 16);

		claimChunksButton.setPosAndSize(1, 1, 16, 16);
		waypointManagerButton.setPosAndSize(1, 19, 16, 16);
		clearDeathpointsButton.setPosAndSize(1, 37, 16, 16);

		dimensionButton.setPosAndSize(1, height - 36, 16, 16);
		settingsButton.setPosAndSize(1, height - 18, 16, 16);
		if (serverSettingsButton != null) {
			serverSettingsButton.setPosAndSize(width - 18, height - 18, 16, 16);
		}
	}

	@Override
	public boolean onInit() {
		return setFullscreen();
	}

	@Override
	public boolean mousePressed(MouseButton button) {
		if (super.mousePressed(button)) {
			return true;
		}

		if (button.isLeft()) {
			prevMouseX = getMouseX();
			prevMouseY = getMouseY();
			return true;
		} else if (button.isRight()) {
			final BlockPos pos = new BlockPos(regionPanel.blockX, regionPanel.blockY, regionPanel.blockZ);
			List<ContextMenuItem> list = new ArrayList<>();
			list.add(new ContextMenuItem(Component.translatable("ftbchunks.gui.add_waypoint"), Icons.ADD, () -> {
				StringConfig name = new StringConfig();
				new EditConfigFromStringScreen<>(name, set -> {
					if (set) {
						Waypoint w = new Waypoint(dimension, pos.getX(), pos.getY(), pos.getZ());
						w.name = name.value;
						w.color = Color4I.hsb(MathUtils.RAND.nextFloat(), 1F, 1F).rgba();
						dimension.getWaypointManager().add(w);
						refreshWidgets();
					}

					openGui();
				}).openGui();
			}));
			openContextMenu(list);
			return true;
		}

		return false;
	}

	@Override
	public boolean keyPressed(Key key) {
		if (FTBChunksClient.openMapKey.matches(key.keyCode, key.scanCode) || key.escOrInventory()) {
			if (key.esc() && contextMenu != null) {
				closeContextMenu();
			} else {
				closeGui(false);
			}
			return true;
		} else if (key.is(GLFW.GLFW_KEY_SPACE)) {
			movedToPlayer = false;
			return true;
		} else if (super.keyPressed(key)) {
			return true;
		} else if (key.is(GLFW.GLFW_KEY_T)) {
			new TeleportFromMapPacket(regionPanel.blockX, regionPanel.blockY + 1, regionPanel.blockZ, regionPanel.blockY == HeightUtils.UNKNOWN, dimension.dimension).sendToServer();
			closeGui(false);
			return true;
		} else if (key.is(GLFW.GLFW_KEY_G) && InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_F3)) {
			FTBChunksClientConfig.CHUNK_GRID.toggle();
			FTBChunksClientConfig.saveConfig();
			dimension.manager.updateAllRegions(false);
			return true;
		} else if (key.is(GLFW.GLFW_KEY_C)) {
			claimChunksButton.onClicked(MouseButton.LEFT);
			return true;
		} else if (key.is(GLFW.GLFW_KEY_S)) {
			if (Screen.hasControlDown()) {
				if (serverSettingsButton != null) {
					serverSettingsButton.onClicked(MouseButton.LEFT);
				}
			} else {
				settingsButton.onClicked(MouseButton.LEFT);
			}
			return true;
		} else if (FTBChunksClient.waypointManagerKey.matches(key.keyCode, key.scanCode)) {
			waypointManagerButton.onClicked(MouseButton.LEFT);
		}

		return false;
	}

	@Override
	public void tick() {
		super.tick();

		if (needIconRefresh) {
			regionPanel.refreshWidgets();
			needIconRefresh = false;
		}
	}

	@Override
	public boolean drawDefaultBackground(PoseStack matrixStack) {
		if (!movedToPlayer) {
			Player p = Minecraft.getInstance().player;
			regionPanel.resetScroll();
			regionPanel.scrollTo(p.getX() / 512D, p.getZ() / 512D);
			movedToPlayer = true;
		}

		backgroundColor.draw(matrixStack, 0, 0, width, height);
		return false;
	}

	@Override
	public void drawBackground(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
		if (grabbed != 0) {
			int mx = getMouseX();
			int my = getMouseY();

			if (scrollWidth > regionPanel.width) {
				regionPanel.setScrollX(Math.max(Math.min(regionPanel.getScrollX() + (prevMouseX - mx), scrollWidth - regionPanel.width), 0));
			}

			if (scrollHeight > regionPanel.height) {
				regionPanel.setScrollY(Math.max(Math.min(regionPanel.getScrollY() + (prevMouseY - my), scrollHeight - regionPanel.height), 0));
			}

			prevMouseX = mx;
			prevMouseY = my;
		}

		if (scrollWidth <= regionPanel.width) {
			regionPanel.setScrollX((scrollWidth - regionPanel.width) / 2D);
		}

		if (scrollHeight <= regionPanel.height) {
			regionPanel.setScrollY((scrollHeight - regionPanel.height) / 2D);
		}

		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder buffer = tessellator.getBuilder();
		int r = 70;
		int g = 70;
		int b = 70;
		int a = 100;

		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
		RenderSystem.disableTexture();
		buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

		int s = getRegionButtonSize();
		double ox = -regionPanel.getScrollX() % s;
		double oy = -regionPanel.getScrollY() % s;

		for (int gx = 0; gx <= (w / s) + 1; gx++) {
			buffer.vertex(x + ox + gx * s, y, 0).color(r, g, b, a).endVertex();
			buffer.vertex(x + ox + gx * s, y + h, 0).color(r, g, b, a).endVertex();
		}

		for (int gy = 0; gy <= (h / s) + 1; gy++) {
			buffer.vertex(x, y + oy + gy * s, 0).color(r, g, b, a).endVertex();
			buffer.vertex(x + w, y + oy + gy * s, 0).color(r, g, b, a).endVertex();
		}

		tessellator.end();
		RenderSystem.enableTexture();
	}

	@Override
	public void drawForeground(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
		String coords = "X: " + regionPanel.blockX + ", Y: " + (regionPanel.blockY == HeightUtils.UNKNOWN ? "??" : regionPanel.blockY) + ", Z: " + regionPanel.blockZ;

		if (regionPanel.blockY != HeightUtils.UNKNOWN) {
			MapRegion region = dimension.getRegion(XZ.regionFromBlock(regionPanel.blockX, regionPanel.blockZ));
			MapRegionData data = region.getData();

			if (data != null) {
				int waterLightAndBiome = data.waterLightAndBiome[regionPanel.blockIndex] & 0xFFFF;
				ResourceKey<Biome> biome = dimension.manager.getBiomeKey(waterLightAndBiome);
				Block block = dimension.manager.getBlock(data.getBlockIndex(regionPanel.blockIndex));
				coords = coords + " | " + I18n.get("biome." + biome.location().getNamespace() + "." + biome.location().getPath()) + " | " + I18n.get(block.getDescriptionId());

				if ((waterLightAndBiome & (1 << 15)) != 0) {
					coords += " (in water)";
				}
			}
		}

		int coordsw = theme.getStringWidth(coords) / 2;

		backgroundColor.withAlpha(150).draw(matrixStack, x + (w - coordsw) / 2, y + h - 6, coordsw + 4, 6);
		matrixStack.pushPose();
		matrixStack.translate(x + (w - coordsw) / 2F + 2F, y + h - 5, 0F);
		matrixStack.scale(0.5F, 0.5F, 1F);
		theme.drawString(matrixStack, coords, 0, 0, Theme.SHADOW);
		matrixStack.popPose();

		if (FTBChunksClientConfig.DEBUG_INFO.get()) {
			long memory = MapManager.inst.estimateMemoryUsage();

			String memoryUsage = "Estimated Memory Usage: " + StringUtils.formatDouble00(memory / 1024D / 1024D) + " MB";
			int memoryUsagew = theme.getStringWidth(memoryUsage) / 2;

			backgroundColor.withAlpha(150).draw(matrixStack, x + (w - memoryUsagew) - 2, y, memoryUsagew + 4, 6);

			matrixStack.pushPose();
			matrixStack.translate(x + (w - memoryUsagew) - 1F, y + 1, 0F);
			matrixStack.scale(0.5F, 0.5F, 1F);
			theme.drawString(matrixStack, memoryUsage, 0, 0, Theme.SHADOW);
			matrixStack.popPose();
		}

		if (zoom == minZoom && zoom > 1) {
			Component zoomWarn = Component.translatable("ftbchunks.zoom_warning");
			matrixStack.pushPose();
			matrixStack.translate(x + w / 2F, y + 1, 0F);
			matrixStack.scale(0.5F, 0.5F, 1F);
			theme.drawString(matrixStack, zoomWarn, 0, 0, Color4I.rgb(0xF0C000), Theme.CENTERED);
			matrixStack.popPose();
		}
	}

	public static void refreshIconsIfOpen() {
		if (Minecraft.getInstance().screen instanceof ScreenWrapper sw && sw.getGui() instanceof LargeMapScreen lms) {
			lms.needIconRefresh = true;
		}
	}

	private int determineMinZoom() {
		if (!FTBChunksClientConfig.MAX_ZOOM_CONSTRAINT.get()) {
			return 1;
		}

		// limit the possible zoom-out based on number of regions known about (i.e. which *could* be loaded,
		// taking up ~4MB RAM per region)

		// possible memory that could be used
		long potentialUsage = dimension.getLoadedRegions().size() * MapManager.MEMORY_PER_REGION;

		// note: this not necessarily the amount of memory that can be allocated, but this is a
		// fairly fuzzy check anyway
		long allocatedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		long freeMem = Runtime.getRuntime().maxMemory() - allocatedMemory;

		long ratio = freeMem / Math.max(1, potentialUsage);

		FTBChunks.LOGGER.debug("large map: free mem = {}, potential usage = {}, ratio = {}", freeMem, potentialUsage, ratio);
		if (ratio < 8) {
			return 64;
		} else if (ratio < 16) {
			return 32;
		} else if (ratio < 32) {
			return 16;
		} else if (ratio < 64) {
			return 8;
		} else {
			return 1;
		}
	}

	private static class SimpleTooltipButton extends SimpleButton {
		private final List<Component> tooltipLines;

		public SimpleTooltipButton(Panel panel, Component text, Icon icon, Callback c, Component tooltipLine) {
			super(panel, text, icon, c);
			this.tooltipLines = List.of(tooltipLine);
		}

		@Override
		public void addMouseOverText(TooltipList list) {
			super.addMouseOverText(list);
			tooltipLines.forEach(list::add);
		}
	}
}
