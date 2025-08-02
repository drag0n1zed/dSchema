package io.github.drag0n1zed.universal.vanilla.platform;

import java.util.List;
import java.util.stream.Collectors;

import io.github.drag0n1zed.universal.api.core.Interaction;
import io.github.drag0n1zed.universal.api.core.Player;
import io.github.drag0n1zed.universal.api.core.PlayerInfo;
import io.github.drag0n1zed.universal.api.core.World;
import io.github.drag0n1zed.universal.api.gui.Screen;
import io.github.drag0n1zed.universal.api.gui.Typeface;
import io.github.drag0n1zed.universal.api.platform.Client;
import io.github.drag0n1zed.universal.api.platform.Options;
import io.github.drag0n1zed.universal.api.platform.ParticleEngine;
import io.github.drag0n1zed.universal.api.renderer.Camera;
import io.github.drag0n1zed.universal.api.renderer.Window;
import io.github.drag0n1zed.universal.api.sound.SoundManager;
import io.github.drag0n1zed.universal.vanilla.core.MinecraftConvertor;
import io.github.drag0n1zed.universal.vanilla.core.MinecraftPlayer;
import io.github.drag0n1zed.universal.vanilla.core.MinecraftPlayerInfo;
import io.github.drag0n1zed.universal.vanilla.core.MinecraftWorld;
import io.github.drag0n1zed.universal.vanilla.gui.MinecraftProxyScreen;
import io.github.drag0n1zed.universal.vanilla.gui.MinecraftScreen;
import io.github.drag0n1zed.universal.vanilla.gui.MinecraftTypeface;
import io.github.drag0n1zed.universal.vanilla.renderer.MinecraftCamera;
import io.github.drag0n1zed.universal.vanilla.renderer.MinecraftWindow;
import io.github.drag0n1zed.universal.vanilla.sound.MinecraftParticleEngine;
import io.github.drag0n1zed.universal.vanilla.sound.MinecraftSoundManager;
import net.minecraft.client.Minecraft;

public record MinecraftClient(
        Minecraft refs
) implements Client {

    @Override
    public Window getWindow() {
        return new MinecraftWindow(refs.getWindow());
    }

    @Override
    public Camera getCamera() {
        return new MinecraftCamera(refs.gameRenderer.getMainCamera());
    }

    @Override
    public Screen getPanel() {
        if (refs.screen == null) {
            return null;
        }
        if (refs.screen instanceof MinecraftProxyScreen proxyScreen) {
            return proxyScreen.getProxy();
        }
        return new MinecraftScreen(refs.screen);
    }

    @Override
    public void setPanel(Screen screen) {
        if (screen == null) {
            refs.setScreen(null);
            return;
        }
        if (screen instanceof MinecraftScreen minecraftScreen) {
            refs.setScreen(minecraftScreen.refs());
            return;
        }
        refs.setScreen(new MinecraftProxyScreen(screen));

    }

    @Override
    public Player getPlayer() {
        return MinecraftPlayer.ofNullable(refs.player);
    }

    @Override
    public List<PlayerInfo> getOnlinePlayers() {
        if (refs.getConnection() == null) return List.of();
        return refs.getConnection().getOnlinePlayers().stream().map(MinecraftPlayerInfo::new).collect(Collectors.toList());
    }

    @Override
    public Typeface getTypeface() {
        return new MinecraftTypeface(refs.font);
    }

    @Override
    public World getWorld() {
        return MinecraftWorld.ofNullable(refs.level);
    }

    @Override
    public boolean isLoaded() {
        return getWorld() != null;
    }

    @Override
    public Interaction getLastInteraction() {
        return MinecraftConvertor.fromPlatformInteraction(refs.hitResult);
    }

    @Override
    public String getClipboard() {
        return refs.keyboardHandler.getClipboard();
    }

    @Override
    public void setClipboard(String content) {
        refs.keyboardHandler.setClipboard(content);
    }

    @Override
    public SoundManager getSoundManager() {
        return new MinecraftSoundManager(refs.getSoundManager());
    }

    @Override
    public void sendChat(String chat) {
        refs.getConnection().sendChat(chat);
    }

    @Override
    public void sendCommand(String command) {
        refs.getConnection().sendCommand(command);
    }

    @Override
    public void execute(Runnable runnable) {
        refs.execute(runnable);
    }

    @Override
    public Options getOptions() {
        return new MinecraftOptions(refs.options);
    }

    @Override
    public ParticleEngine getParticleEngine() {
        return new MinecraftParticleEngine(refs.particleEngine);
    }

    @Override
    public boolean isLocalServer() {
        return refs.isLocalServer();
    }

    @Override
    public boolean hasSinglePlayerServer() {
        return refs.hasSingleplayerServer();
    }
}
