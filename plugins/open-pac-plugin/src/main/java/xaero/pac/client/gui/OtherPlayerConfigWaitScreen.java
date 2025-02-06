package xaero.pac.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.client.player.config.IPlayerConfigClientStorage;
import xaero.pac.client.player.config.IPlayerConfigClientStorageManager;
import xaero.pac.client.player.config.IPlayerConfigStringableOptionClientStorage;
import xaero.pac.client.player.config.PlayerConfigClientStorage;
import xaero.pac.common.packet.config.ServerboundOtherPlayerConfigPacket;

import java.util.ArrayList;

public class OtherPlayerConfigWaitScreen extends XPACScreen {

	private final Component message;
	private final String otherPlayerName;
	private Listener listener;

	public OtherPlayerConfigWaitScreen(Screen escape, Screen parent, String otherPlayerName) {
		super(escape, parent, Component.literal(""));
		this.otherPlayerName = otherPlayerName;
		message = Component.translatable("gui.xaero_pac_ui_other_player_config_waiting", otherPlayerName);
	}

	@Override
	protected void init() {
		super.init();
		addRenderableWidget(Button.builder(Component.translatable("gui.xaero_pac_ui_other_player_config_waiting_cancel"), this::onCancelButton).bounds(width / 2 - 100, this.height / 6 + 168, 200, 20).build());
		startListening();
	}

	protected void onCancelButton(Button b) {
		goBack();
	}

	private void startListening() {
		if(listener == null) {
			listener = new Listener();
			listener.start();
		}
	}

	public Listener getListener() {
		return listener;
	}

	public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partial) {
		super.renderBackground(guiGraphics, mouseX, mouseY, partial);
		guiGraphics.drawCenteredString(font, message, width / 2, height / 6 + 64, -1);
	}

	public final class Listener {

		public void start() {
			ServerboundOtherPlayerConfigPacket packet = new ServerboundOtherPlayerConfigPacket(otherPlayerName);
			OpenPartiesAndClaims.INSTANCE.getPacketHandler().sendToServer(packet);
		}

		public void onConfigDataSyncDone(IPlayerConfigClientStorage<IPlayerConfigStringableOptionClientStorage<?>> configData) {
			IPlayerConfigClientStorageManager<IPlayerConfigClientStorage<IPlayerConfigStringableOptionClientStorage<?>>> manager = OpenPartiesAndClaims.INSTANCE.getClientDataInternal().getPlayerConfigStorageManager();
			minecraft.setScreen(
					PlayerConfigScreen.Builder
					.begin(ArrayList::new)
					.setParent(parent)
					.setEscape(escape)
					.setTitle(Component.translatable("gui.xaero_pac_ui_other_player_config", otherPlayerName))
					.setData((PlayerConfigClientStorage)(Object)configData)
					.setManager(manager)
					.setDefaultPlayerConfigData((PlayerConfigClientStorage)(Object) manager.getDefaultPlayerConfig())
					.setOtherPlayerName(otherPlayerName)
					.build()
					);
		}

	}

}
