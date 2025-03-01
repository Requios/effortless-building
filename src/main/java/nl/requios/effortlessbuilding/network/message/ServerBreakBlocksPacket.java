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
 * Sends a message to the server to break multiple blocks
 */
public record ServerBreakBlocksPacket(BlockSet blocks) implements CustomPacketPayload {

	public static final StreamCodec<FriendlyByteBuf, ServerBreakBlocksPacket> CODEC = CustomPacketPayload.codec(
			ServerBreakBlocksPacket::write,
			ServerBreakBlocksPacket::new);
	public static final Type<ServerBreakBlocksPacket> ID = new Type<>(EffortlessBuilding.asResource("server_break_blocks"));

	public ServerBreakBlocksPacket(FriendlyByteBuf buf) {
		this(BlockSet.decode(buf));
	}

	public void write(FriendlyByteBuf buf) {
		BlockSet.encode(buf, blocks);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}

	public static class Handler {
		public static void handle(final ServerBreakBlocksPacket packet, final IPayloadContext context) {
			context.enqueueWork(() -> {
				if (context.player() instanceof ServerPlayer player) {
					EffortlessBuilding.SERVER_BLOCK_PLACER.breakBlocks(player, packet.blocks());
				}
			}).exceptionally(e -> {
				// Handle exception
				context.disconnect(Component.translatable("effortlessbuilding.networking.server_break_blocks.failed", e.getMessage()));
				return null;
			});
		}
	}
}
