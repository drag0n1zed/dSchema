package io.github.drag0n1zed.universal.vanilla.core;

import io.github.drag0n1zed.universal.api.core.fluid.Fluid;

public record MinecraftFluid(net.minecraft.world.level.material.Fluid refs) implements Fluid {

    public static Fluid ofNullable(net.minecraft.world.level.material.Fluid refs) {
        if (refs == null) return null;
        return new MinecraftFluid(refs);
    }

}
