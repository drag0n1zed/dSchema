package io.github.drag0n1zed.universal.vanilla.core;

import java.util.UUID;

import io.github.drag0n1zed.universal.api.core.BlockInteraction;
import io.github.drag0n1zed.universal.api.core.GameMode;
import io.github.drag0n1zed.universal.api.core.InteractionHand;
import io.github.drag0n1zed.universal.api.core.Inventory;
import io.github.drag0n1zed.universal.api.core.ItemStack;
import io.github.drag0n1zed.universal.api.core.Player;
import io.github.drag0n1zed.universal.api.core.PlayerProfile;
import io.github.drag0n1zed.universal.api.core.Stat;
import io.github.drag0n1zed.universal.api.core.World;
import io.github.drag0n1zed.universal.api.math.Vector3d;
import io.github.drag0n1zed.universal.api.platform.Client;
import io.github.drag0n1zed.universal.api.platform.Server;
import io.github.drag0n1zed.universal.api.text.Text;
import io.github.drag0n1zed.universal.vanilla.platform.MinecraftClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.server.level.ServerPlayer;

public record MinecraftPlayer(net.minecraft.world.entity.player.Player refs) implements Player {

    public static Player ofNullable(net.minecraft.world.entity.player.Player refs) {
        if (refs == null) return null;
        return new MinecraftPlayer(refs);
    }

    @Override
    public UUID getId() {
        return refs.getUUID();
    }

    @Override
    public PlayerProfile getProfile() {
        return new MinecraftPlayerProfile(refs.getGameProfile());
    }

    @Override
    public Inventory getInventory() {
        return new MinecraftInventory(refs.getInventory());
    }

    @Override
    public boolean isDeadOrDying() {
        return refs.isDeadOrDying();
    }

    @Override
    public Client getClient() {
        return new MinecraftClient(Minecraft.getInstance());
    }

    @Override
    public Server getServer() {
        return new MinecraftServer(refs.getServer());
    }

    @Override
    public World getWorld() {
        return MinecraftWorld.ofNullable(refs.level());
    }

    @Override
    public Text getDisplayName() {
        return new MinecraftText(refs.getDisplayName());
    }

    @Override
    public Vector3d getPosition() {
        return MinecraftConvertor.fromPlatformVector3d(refs.position());
    }

    @Override
    public void drop(ItemStack itemStack, boolean dropAround, boolean includeThrowerName) {
        refs.drop(itemStack.reference(), dropAround, includeThrowerName);
    }

    @Override
    public void sendMessage(Text message, boolean actionBar) {
        refs.displayClientMessage(message.reference(), actionBar);
    }

    @Override
    public void swing(InteractionHand hand) {
        refs.swing(MinecraftConvertor.toPlatformInteractionHand(hand));
    }

    @Override
    public GameMode getGameMode() {
        if (refs instanceof ServerPlayer serverPlayer) {
            return switch (serverPlayer.gameMode.getGameModeForPlayer()) {
                case SURVIVAL -> GameMode.SURVIVAL;
                case CREATIVE -> GameMode.CREATIVE;
                case ADVENTURE -> GameMode.ADVENTURE;
                case SPECTATOR -> GameMode.SPECTATOR;
            };
        }
        if (refs instanceof AbstractClientPlayer localPlayer) {
            return switch (localPlayer.getPlayerInfo().getGameMode()) {
                case SURVIVAL -> GameMode.SURVIVAL;
                case CREATIVE -> GameMode.CREATIVE;
                case ADVENTURE -> GameMode.ADVENTURE;
                case SPECTATOR -> GameMode.SPECTATOR;
            };
        }
        return null;
    }

    @Override
    public BlockInteraction raytrace(double maxDistance, float deltaTick, boolean includeFluids) {
        return (BlockInteraction) MinecraftConvertor.fromPlatformInteraction(refs.pick(maxDistance, deltaTick, includeFluids));
    }

    @Override
    public float getEyeHeight() {
        return refs.getEyeHeight();
    }

    @Override
    public void setPosition(Vector3d position) {
        refs.setPos(MinecraftConvertor.toPlatformVector3d(position));
    }

    @Override
    public float getXRot() {
        return refs.getXRot();
    }

    @Override
    public void setXRot(float xRot) {
        refs.setXRot(xRot);
    }

    @Override
    public float getYRot() {
        return refs.getYRot();
    }

    @Override
    public void setYRot(float yRot) {
        refs.setYRot(yRot);
    }

    @Override
    public void awardStat(Stat<?> stat, int increment) {
        refs.awardStat((net.minecraft.stats.Stat<?>) stat.reference(), increment);
    }

    @Override
    public void resetStat(Stat<?> stat) {
        refs.resetStat(stat.reference());
    }

}
