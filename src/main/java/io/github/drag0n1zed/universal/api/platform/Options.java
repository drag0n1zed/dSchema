package io.github.drag0n1zed.universal.api.platform;

import io.github.drag0n1zed.universal.api.input.KeyBinding;

public interface Options extends PlatformReference {

    KeyBinding keyUp();

    KeyBinding keyLeft();

    KeyBinding keyDown();

    KeyBinding keyRight();

    KeyBinding keyJump();

    KeyBinding keyShift();

    KeyBinding keySprint();

    KeyBinding keyInventory();

    KeyBinding keySwapOffhand();

    KeyBinding keyDrop();

    KeyBinding keyUse();

    KeyBinding keyAttack();

    KeyBinding keyPickItem();

    KeyBinding keyChat();

    KeyBinding keyPlayerList();

    KeyBinding keyCommand();

    KeyBinding keySocialInteractions();

    KeyBinding keyScreenshot();

    KeyBinding keyTogglePerspective();

    KeyBinding keySmoothCamera();

    KeyBinding keyFullscreen();

    KeyBinding keySpectatorOutlines();

    KeyBinding keyAdvancements();

    KeyBinding keySaveHotbarActivator();

    KeyBinding keyLoadHotbarActivator();

    KeyBinding[] keyHotbarSlots();

    int renderDistance();

}
