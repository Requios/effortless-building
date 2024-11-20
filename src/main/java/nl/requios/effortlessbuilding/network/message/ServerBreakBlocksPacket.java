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
 * Sends a message to the server to break multiple blocks
 */
public record ServerBreakBlocksPacket(BlockSet blocks) implements CustomPacketPayload {
	public static final ResourceLocation ID = new ResourceLocation(EffortlessBuilding.MODID, "server_break_blocks");

	public ServerBreakBlocksPacket(FriendlyByteBuf buf) {
		this(BlockSet.decode(buf));
	}

	public void write(FriendlyByteBuf buf) {
		BlockSet.encode(buf, blocks);
	}

	@Override
	public ResourceLocation id() {
		return ID;
	}

	public static class Handler {
		public static void handle(final ServerBreakBlocksPacket packet, final PlayPayloadContext context) {
			context.workHandler().submitAsync(() -> {
				if (context.player().isPresent()) {
					Player player = context.player().get();
					EffortlessBuilding.SERVER_BLOCK_PLACER.breakBlocks(player, packet.blocks());
				}
			}).exceptionally(e -> {
				// Handle exception
				context.packetHandler().disconnect(Component.translatable("effortlessbuilding.networking.server_break_blocks.failed", e.getMessage()));
				return null;
			});
		}
	}
}
