package dev.huskuraft.effortless.fabric.platform;

import dev.huskuraft.effortless.api.platform.Entrance;
import dev.huskuraft.effortless.fabric.events.common.PlatformLifecycleEvents;
import net.fabricmc.api.ModInitializer;

public class FabricInitializer implements ModInitializer {

    @Override
    public void onInitialize() {
        Entrance.getInstance();

        PlatformLifecycleEvents.COMMON_START.invoker().onLaunch();
    }

}
