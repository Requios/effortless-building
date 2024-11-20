package nl.requios.effortlessbuilding.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.utilities.BlockSet;

/**
 * Sends a message to the server to place multiple blocks
 */
public record ServerPlaceBlocksPacket(BlockSet blocks, long placeTime) implements CustomPacketPayload {
	public static final ResourceLocation ID = new ResourceLocation(EffortlessBuilding.MODID, "server_place_blocks");

	public ServerPlaceBlocksPacket(FriendlyByteBuf buf) {
		this(BlockSet.decode(buf), buf.readLong());
	}

	public void write(FriendlyByteBuf buf) {
		BlockSet.encode(buf, blocks);
		buf.writeLong(placeTime);
	}

	@Override
	public ResourceLocation id() {
		return ID;
	}

	public static class Handler {
		public static void handle(final ServerPlaceBlocksPacket packet, final PlayPayloadContext context) {
			context.workHandler().submitAsync(() -> {
				if (context.player().isPresent()) {
					Player player = context.player().get();
					EffortlessBuilding.logger.info("Place block packet received from client. Placing blocks...");
					EffortlessBuilding.SERVER_BLOCK_PLACER.placeBlocksDelayed(player, packet.blocks(), packet.placeTime());
				}
			}).exceptionally(e -> {
				// Handle exception
				context.packetHandler().disconnect(Component.translatable("effortlessbuilding.networking.server_place_blocks.failed", e.getMessage()));
				return null;
			});
		}
	}
}
