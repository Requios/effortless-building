package nl.requios.effortlessbuilding.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.utilities.BlockSet;

/**
 * Sends a message to the server to place multiple blocks
 */
public record ServerPlaceBlocksPacket(BlockSet blocks, long placeTime) implements CustomPacketPayload {

	public static final StreamCodec<FriendlyByteBuf, ServerPlaceBlocksPacket> CODEC = CustomPacketPayload.codec(
			ServerPlaceBlocksPacket::write,
			ServerPlaceBlocksPacket::new);
	public static final Type<ServerPlaceBlocksPacket> ID = new Type<>(EffortlessBuilding.asResource("server_place_blocks"));

	public ServerPlaceBlocksPacket(FriendlyByteBuf buf) {
		this(BlockSet.decode(buf), buf.readLong());
	}

	public void write(FriendlyByteBuf buf) {
		BlockSet.encode(buf, blocks);
		buf.writeLong(placeTime);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}

	public static class Handler {
		public static void handle(final ServerPlaceBlocksPacket packet, final IPayloadContext context) {
			context.enqueueWork(() -> {
				if (context.player() instanceof ServerPlayer player) {
					EffortlessBuilding.logger.info("Place block packet received from client. Placing blocks...");
					EffortlessBuilding.SERVER_BLOCK_PLACER.placeBlocksDelayed(player, packet.blocks(), packet.placeTime());
				}
			}).exceptionally(e -> {
				// Handle exception
				context.disconnect(Component.translatable("effortlessbuilding.networking.server_place_blocks.failed", e.getMessage()));
				return null;
			});
		}
	}
}
