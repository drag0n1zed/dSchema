package dev.huskuraft.universal.forge.platform;

import dev.huskuraft.universal.api.platform.ClientEntrance;
import dev.huskuraft.universal.api.platform.Entrance;
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
