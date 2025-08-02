package io.github.drag0n1zed.universal.vanilla.core;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import io.github.drag0n1zed.universal.api.core.PlayerProfile;

public record MinecraftPlayerProfile(
        GameProfile refs
) implements PlayerProfile {

    @Override
    public UUID getId() {
        return refs.getId();
    }

    @Override
    public String getName() {
        return refs.getName();
    }

    @Override
    public Map<String, ? extends Collection<?>> getProperties() {
        return refs.getProperties().asMap();
    }
}
