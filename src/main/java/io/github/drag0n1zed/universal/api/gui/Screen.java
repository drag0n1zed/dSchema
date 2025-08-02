package io.github.drag0n1zed.universal.api.gui;

import io.github.drag0n1zed.universal.api.text.Text;

public interface Screen extends ContainerWidget {

    Text getScreenTitle();

    void init(int width, int height);

    void onAttach();

    void onDetach();

    boolean isPauseGame();

    void attach();

    void detach();

}

