package io.github.drag0n1zed.universal.api.platform;

import java.util.List;

import io.github.drag0n1zed.universal.api.core.Interaction;
import io.github.drag0n1zed.universal.api.core.Player;
import io.github.drag0n1zed.universal.api.core.PlayerInfo;
import io.github.drag0n1zed.universal.api.core.World;
import io.github.drag0n1zed.universal.api.gui.Screen;
import io.github.drag0n1zed.universal.api.gui.Typeface;
import io.github.drag0n1zed.universal.api.renderer.Camera;
import io.github.drag0n1zed.universal.api.renderer.Window;
import io.github.drag0n1zed.universal.api.sound.SoundManager;

public interface Client extends PlatformReference {

    Window getWindow();

    Camera getCamera();

    Screen getPanel();

    void setPanel(Screen screen);

    Player getPlayer();

    List<PlayerInfo> getOnlinePlayers();

    Typeface getTypeface();

    World getWorld();

    boolean isLoaded();

    Interaction getLastInteraction();

    String getClipboard();

    void setClipboard(String content);

    SoundManager getSoundManager();

    void sendChat(String chat);

    void sendCommand(String command);

    void execute(Runnable runnable);

    Options getOptions();

    ParticleEngine getParticleEngine();

    boolean isLocalServer();

    boolean hasSinglePlayerServer();

}
