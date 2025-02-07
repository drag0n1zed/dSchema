package dev.ftb.mods.ftbchunks;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.Block;

public class FTBCUtils {
	@ExpectPlatform
	public static boolean isRail(Block block) {
		throw new AssertionError();
	}


	/**
	 * Used after various events have been cancelled server-side; client may already have updated the held item for the
	 * player, but it needs to be brought back in sync with the server.
	 *
	 * @param sp   the player
	 * @param hand the hand being used
	 */
	public static void forceHeldItemSync(ServerPlayer sp, InteractionHand hand) {
		if (sp.connection != null) {
			switch (hand) {
				case MAIN_HAND -> sp.connection.send(new ClientboundContainerSetSlotPacket(-2, 0, sp.getInventory().selected, sp.getItemInHand(hand)));
				case OFF_HAND -> sp.connection.send(new ClientboundContainerSetSlotPacket(-2, 0, Inventory.SLOT_OFFHAND, sp.getItemInHand(hand)));
			}
		}
	}
}