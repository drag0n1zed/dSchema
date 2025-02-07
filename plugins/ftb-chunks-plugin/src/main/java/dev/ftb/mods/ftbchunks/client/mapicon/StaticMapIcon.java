package dev.ftb.mods.ftbchunks.client.mapicon;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftbchunks.api.client.icon.MapIcon;
import dev.ftb.mods.ftbchunks.client.gui.LargeMapScreen;
import dev.ftb.mods.ftbchunks.net.TeleportFromMapPacket;
import dev.ftb.mods.ftblibrary.ui.BaseScreen;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

public class StaticMapIcon extends MapIcon.SimpleMapIcon {
	public StaticMapIcon(BlockPos pos) {
		super(Vec3.atCenterOf(pos));
	}

	@Override
	public boolean onKeyPressed(BaseScreen screen, Key key) {
		return handleKeypress(this, screen, key);
	}

	static boolean handleKeypress(MapIcon icon, BaseScreen screen, Key key) {
		if (screen instanceof LargeMapScreen lms && key.is(GLFW.GLFW_KEY_T)) {
			NetworkManager.sendToServer(new TeleportFromMapPacket(BlockPos.containing(icon.getPos(1F)).above(), false, lms.currentDimension()));
			screen.closeGui(false);
			return true;
		}

		return false;
	}
}
