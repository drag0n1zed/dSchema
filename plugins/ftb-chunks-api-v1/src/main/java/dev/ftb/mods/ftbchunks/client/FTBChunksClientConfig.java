package dev.ftb.mods.ftbchunks.client;

import dev.architectury.platform.Platform;
import dev.ftb.mods.ftbchunks.FTBChunks;
import dev.ftb.mods.ftbchunks.FTBChunksWorldConfig;
import dev.ftb.mods.ftbchunks.client.map.BiomeBlendMode;
import dev.ftb.mods.ftbchunks.client.map.MapManager;
import dev.ftb.mods.ftbchunks.client.map.MapMode;
import dev.ftb.mods.ftbchunks.net.ServerConfigRequestPacket;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.ui.EditConfigScreen;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftblibrary.snbt.config.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import static dev.ftb.mods.ftblibrary.snbt.config.ConfigUtil.LOCAL_DIR;
import static dev.ftb.mods.ftblibrary.snbt.config.ConfigUtil.loadDefaulted;

/**
 * @author LatvianModder
 */
public interface FTBChunksClientConfig {
	SNBTConfig CONFIG = SNBTConfig.create(FTBChunks.MOD_ID + "-client");

	DoubleValue NOISE = CONFIG.getDouble("noise", 0.05D, 0D, 0.5D).fader().comment("Noise added to map to make it look less plastic");
	DoubleValue SHADOWS = CONFIG.getDouble("shadows", 0.1D, 0D, 0.3D).fader().comment("Shadow intensity");
	BooleanValue CHUNK_GRID = CONFIG.getBoolean("chunk_grid", false).comment("Chunk grid overlay in large map");
	BooleanValue REDUCED_COLOR_PALETTE = CONFIG.getBoolean("reduced_color_palette", false).comment("Reduces color palette to 256 colors");
	DoubleValue SATURATION = CONFIG.getDouble("saturation", 1D, 0D, 1D).fader().comment("Color intensity");
	BooleanValue CLAIMED_CHUNKS_ON_MAP = CONFIG.getBoolean("claimed_chunks_on_map", true).comment("Show claimed chunks on the map");
	BooleanValue OWN_CLAIMED_CHUNKS_ON_MAP = CONFIG.getBoolean("own_claimed_chunks_on_map", true).comment("Show your own claimed chunks on the map");
	BooleanValue IN_WORLD_WAYPOINTS = CONFIG.getBoolean("in_world_waypoints", true).comment("Show waypoints in world");
	BooleanValue DEATH_WAYPOINTS = CONFIG.getBoolean("death_waypoints", true).comment("Enables creation of death waypoints");
	IntValue DEATH_WAYPOINT_AUTOREMOVE_DISTANCE = CONFIG.getInt("death_waypoint_autoremove_distance", 0, 0, Integer.MAX_VALUE).comment("Automatically remove death waypoints if closer than this many blocks away (distance of 0 disables removal)");
	BooleanValue ONLY_SURFACE_ENTITIES = CONFIG.getBoolean("only_surface_entities", true).comment("Only show entities that are on the surface");
	DoubleValue WAYPOINT_BEACON_FADE_DISTANCE = CONFIG.getDouble("waypoint_fade_distance", 12D, 1D, 200D).comment("Minimum distance before waypoint beacons start to fade");
	DoubleValue WAYPOINT_DOT_FADE_DISTANCE = CONFIG.getDouble("waypoint_dot_fade_distance", 1D, 1D, 200D).comment("Minimum distance before waypoint dots start to fade");
	DoubleValue WAYPOINT_MAX_DISTANCE = CONFIG.getDouble("waypoint_max_distance", 5000D, 1D, Integer.MAX_VALUE).comment("Maximum distance at which waypoints are drawn");
	EnumValue<MapMode> MAP_MODE = CONFIG.getEnum("map_mode", MapMode.NAME_MAP).comment("Different ways to render map");
	IntValue WATER_HEIGHT_FACTOR = CONFIG.getInt("water_height_factor", 8, 0, 128).comment("How many blocks should height checks skip in water. 0 means flat water, ignoring terrain");
	EnumValue<BiomeBlendMode> BIOME_BLEND = CONFIG.getEnum("biome_blend", BiomeBlendMode.NAME_MAP).comment("Biome blend");

	SNBTConfig MINIMAP = CONFIG.getGroup("minimap");

	BooleanValue MINIMAP_ENABLED = MINIMAP.getBoolean("enabled", !hasOtherMinimapMod()).comment("Enable minimap");
	EnumValue<MinimapPosition> MINIMAP_POSITION = MINIMAP.getEnum("position", MinimapPosition.NAME_MAP).comment("Enables minimap to show up in corner");
	DoubleValue MINIMAP_SCALE = MINIMAP.getDouble("scale", 1D, 0.25D, 4D).comment("Scale of minimap");
	DoubleValue MINIMAP_ZOOM = MINIMAP.getDouble("zoom", 1D, 1D, 4D).comment("Zoom distance of the minimap");
	BooleanValue MINIMAP_LOCKED_NORTH = MINIMAP.getBoolean("locked_north", true).comment("Minimap will not rotate");
	BooleanValue SHOW_PLAYER_WHEN_UNLOCKED = MINIMAP.getBoolean("show_player_when_unlocked", true).comment("Always show player on minimap, even when rotation not locked");
	BooleanValue MINIMAP_WAYPOINTS = MINIMAP.getBoolean("waypoints", true).comment("Show waypoints on minimap");
	BooleanValue MINIMAP_PLAYER_HEADS = MINIMAP.getBoolean("player_heads", true).comment("Show player heads on minimap");
	BooleanValue MINIMAP_ENTITIES = MINIMAP.getBoolean("entities", true).comment("Show entities on minimap");
	BooleanValue MINIMAP_ENTITY_HEADS = MINIMAP.getBoolean("entity_heads", true).comment("Show entity heads on minimap");
	BooleanValue MINIMAP_LARGE_ENTITIES = MINIMAP.getBoolean("large_entities", false).comment("Entities in minimap will be larger");
	BooleanValue MINIMAP_XYZ = MINIMAP.getBoolean("xyz", true).comment("Show XYZ under minimap");
	BooleanValue MINIMAP_BIOME = MINIMAP.getBoolean("biome", true).comment("Show biome under minimap");
	EnumValue<MinimapBlurMode> MINIMAP_BLUR_MODE = MINIMAP.getEnum("blur_mode", MinimapBlurMode.NAME_MAP).comment("Blurs minimap");
	BooleanValue MINIMAP_COMPASS = MINIMAP.getBoolean("compass", true).comment("Adds NWSE compass inside minimap");
	IntValue MINIMAP_VISIBILITY = MINIMAP.getInt("visibility", 255, 0, 255).comment("Minimap visibility");
	BooleanValue MINIMAP_ZONE = MINIMAP.getBoolean("zone", true).comment("Show zone (claimed chunk or wilderness) under minimap");
	IntValue MINIMAP_OFFSET_X = MINIMAP.getInt("position_offset_x", 0).comment("Changes the maps X offset from it's origin point. When on the Left, the map will be pushed out from the left, then from the right when on the right.");
	IntValue MINIMAP_OFFSET_Y = MINIMAP.getInt("position_offset_y", 0).comment("Changes the maps X offset from it's origin point. When on the Left, the map will be pushed out from the left, then from the right when on the right.");
	EnumValue<MinimapPosition.MinimapOffsetConditional> MINIMAP_POSITION_OFFSET_CONDITION = MINIMAP.getEnum("position_offset_condition", MinimapPosition.MinimapOffsetConditional.NAME_MAP).comment("Applied a conditional check to the offset. When set to anything other that None, the offset will apply only to the selected minimap position.", "When set to none and the maps offset is greater than 0, the offset will apply to all directions");
	BooleanValue SQUARE_MINIMAP = MINIMAP.getBoolean("square", false).comment("Draw a square minimap instead of a circular one");
	BooleanValue MINIMAP_PROPORTIONAL = MINIMAP.getBoolean("proportional", true).comment("Size minimap proportional to screen width (and scale)");

	BooleanValue DEBUG_INFO = CONFIG.getBoolean("debug_info", false).comment("Enables debug info");
	IntValue TASK_QUEUE_TICKS = CONFIG.getInt("task_queue_ticks", 4, 1, 300).excluded().comment("Advanced option. How often queued tasks will run");
	IntValue RERENDER_QUEUE_TICKS = CONFIG.getInt("rerender_queue_ticks", 60, 1, 600).excluded().comment("Advanced option. How often map render update will be queued");
	IntValue TASK_QUEUE_MAX = CONFIG.getInt("task_queue_max", 100, 1, 10000).excluded().comment("Advanced option. Max tasks that can queue up");
	IntValue WATER_VISIBILITY = CONFIG.getInt("water_visibility", 220, 0, 255).excluded().comment("Advanced option. Water visibility");
	IntValue GRASS_DARKNESS = CONFIG.getInt("grass_darkness", 50, 0, 255).excluded().comment("Advanced option. Grass darkness");
	IntValue FOLIAGE_DARKNESS = CONFIG.getInt("foliage_darkness", 50, 0, 255).excluded().comment("Advanced option. Foliage darkness");
	IntValue MINIMAP_ICON_UPDATE_TIMER = CONFIG.getInt("minimap_icon_update_timer", 500, 0, 10000).excluded().comment("Advanced option. Change how often the minimap will refresh icons");

	SNBTConfig MEMORY = CONFIG.getGroup("memory");
	IntValue REGION_RELEASE_TIME = MEMORY.getInt("region_release_time", 300, 0, Integer.MAX_VALUE).comment("Periodically release region data for non-recently-used regions to save memory (units of seconds, 0 disables releasing");
	IntValue AUTORELEASE_ON_MAP_CLOSE = MEMORY.getInt("autorelease_on_map_close", 32, 0, Integer.MAX_VALUE).comment("When the large map is closed, auto-release least recently accessed regions down to this number (0 disables releasing)");
	BooleanValue MAX_ZOOM_CONSTRAINT = MEMORY.getBoolean("max_zoom_constraint", true).comment("Constrain maximum map zoom-out based on number of explored regions and available memory");

	static boolean hasOtherMinimapMod() {
		return Platform.isModLoaded("journeymap") || Platform.isModLoaded("voxelmap") || Platform.isModLoaded("antiqueatlas") || Platform.isModLoaded("xaerominimap");
	}

	static void init() {
		loadDefaulted(CONFIG, LOCAL_DIR.resolve("ftbchunks"), FTBChunks.MOD_ID, "client-config.snbt");
	}

	static void openSettings(Screen screen) {
		ConfigGroup group = new ConfigGroup("ftbchunks");
		CONFIG.createClientConfig(group);

		EditConfigScreen gui = new EditConfigScreen(group);
		group.savedCallback = b -> {
			if (b) {
				saveConfig();
			}

			if (MapManager.inst != null) {
				MapManager.inst.updateAllRegions(false);
			}

			Minecraft.getInstance().setScreen(screen);
		};

		gui.openGui();
	}

	static void openServerSettings(Screen screen) {
		ConfigGroup group = new ConfigGroup("ftbchunks");
		FTBChunksWorldConfig.CONFIG.createClientConfig(group);

		EditConfigScreen gui = new EditConfigScreen(group);
		group.savedCallback = b -> {
			if (b) {
				SNBTCompoundTag config = new SNBTCompoundTag();
				FTBChunksWorldConfig.CONFIG.write(config);
				new ServerConfigRequestPacket(config).sendToServer();
			}
			Minecraft.getInstance().setScreen(screen);
		};
		gui.openGui();
	}

	static void saveConfig() {
		CONFIG.save(Platform.getGameFolder().resolve("local/ftbchunks/client-config.snbt"));
	}
}
