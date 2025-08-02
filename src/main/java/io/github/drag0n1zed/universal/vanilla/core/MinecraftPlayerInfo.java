package io.github.drag0n1zed.universal.vanilla.core;

import io.github.drag0n1zed.universal.api.core.PlayerInfo;
import io.github.drag0n1zed.universal.api.core.PlayerProfile;
import io.github.drag0n1zed.universal.api.core.PlayerSkin;
import io.github.drag0n1zed.universal.api.text.Text;

public record MinecraftPlayerInfo(
        net.minecraft.client.multiplayer.PlayerInfo refs
) implements PlayerInfo {

    @Override
    public PlayerProfile getProfile() {
        return new MinecraftPlayerProfile(refs.getProfile());
    }

    @Override
    public Text getDisplayName() {
        return MinecraftText.ofNullable(refs.getTabListDisplayName());
    }

    @Override
    public PlayerSkin getSkin() {
        return new PlayerSkin(
                MinecraftResourceLocation.ofNullable(refs.getSkinLocation()),
                MinecraftResourceLocation.ofNullable(refs.getCapeLocation()),
                MinecraftResourceLocation.ofNullable(refs.getElytraLocation()),
                PlayerSkin.Model.byName(refs.getModelName())
        );
    }
}
