package io.github.drag0n1zed.universal.api.platform;

import io.github.drag0n1zed.universal.api.gui.Screen;

public interface ClientManager {

    Client getRunningClient();

    void setRunningClient(Client client);

    void pushScreen(Screen screen);

    void popScreen(Screen screen);

}
