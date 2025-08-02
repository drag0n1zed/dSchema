package io.github.drag0n1zed.universal.forge.platform;

import io.github.drag0n1zed.universal.api.platform.ClientEntrance;
import io.github.drag0n1zed.universal.api.platform.Entrance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
@Mod("effortless")
public class ForgeInitializer {
    public ForgeInitializer() {
        Entrance.getInstance();

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            ClientEntrance.getInstance();
        });
    }

}
