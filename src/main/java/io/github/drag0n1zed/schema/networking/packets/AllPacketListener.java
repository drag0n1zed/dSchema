package io.github.drag0n1zed.schema.networking.packets;

import io.github.drag0n1zed.universal.api.core.Player;
import io.github.drag0n1zed.universal.api.networking.PacketListener;
import io.github.drag0n1zed.schema.networking.packets.player.PlayerBuildPacket;
import io.github.drag0n1zed.schema.networking.packets.player.PlayerBuildTooltipPacket;
import io.github.drag0n1zed.schema.networking.packets.player.PlayerCommandPacket;
import io.github.drag0n1zed.schema.networking.packets.player.PlayerPermissionCheckPacket;
import io.github.drag0n1zed.schema.networking.packets.player.PlayerSettingsPacket;
import io.github.drag0n1zed.schema.networking.packets.player.PlayerSnapshotCapturePacket;
import io.github.drag0n1zed.schema.networking.packets.player.PlayerSnapshotSharePacket;
import io.github.drag0n1zed.schema.networking.packets.session.SessionConfigPacket;
import io.github.drag0n1zed.schema.networking.packets.session.SessionPacket;

public interface AllPacketListener extends PacketListener {

    void handle(PlayerCommandPacket packet, Player player);

    void handle(PlayerSettingsPacket packet, Player player);

    void handle(PlayerBuildPacket packet, Player player);

    void handle(PlayerPermissionCheckPacket packet, Player player);

    void handle(PlayerBuildTooltipPacket packet, Player player);

    void handle(SessionPacket packet, Player player);

    void handle(SessionConfigPacket packet, Player player);

    void handle(PlayerSnapshotCapturePacket packet, Player player);

    void handle(PlayerSnapshotSharePacket packet, Player player);

}
