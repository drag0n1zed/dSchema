/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2024, Xaero <xaero1996@gmail.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of version 3 of the GNU Lesser General Public License
 * (LGPL-3.0-only) as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received copies of the GNU Lesser General Public License
 * and the GNU General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package xaero.pac.common.packet.payload;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.packet.type.PacketType;

public class PacketPayload<P> implements CustomPacketPayload {

	public final static Type<PacketPayload<?>> TYPE = new Type<>(OpenPartiesAndClaims.MAIN_CHANNEL_LOCATION);

	private final PacketType<P> packetType;
	private final P packet;

	public PacketPayload(PacketType<P> packetType, P packet) {
		this.packetType = packetType;
		this.packet = packet;
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public PacketType<P> getPacketType() {
		return packetType;
	}

	public P getPacket() {
		return packet;
	}
}
