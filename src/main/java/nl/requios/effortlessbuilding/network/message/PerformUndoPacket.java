package nl.requios.effortlessbuilding.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import nl.requios.effortlessbuilding.EffortlessBuilding;

public record PerformUndoPacket() implements CustomPacketPayload {

	public static final StreamCodec<FriendlyByteBuf, PerformUndoPacket> CODEC = CustomPacketPayload.codec(
			PerformUndoPacket::write,
			PerformUndoPacket::new);
	public static final Type<PerformUndoPacket> ID = new Type<>(EffortlessBuilding.asResource("perform_undo"));

	public PerformUndoPacket(FriendlyByteBuf buf) {
		this();
	}

	public void write(FriendlyByteBuf buf) {}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}

	public static class Handler {
		public static void handle(final PerformUndoPacket packet, final IPayloadContext context) {
			context.enqueueWork(() -> {
				if (context.player() instanceof ServerPlayer player) {
					EffortlessBuilding.UNDO_REDO.undo(player);
				}
			}).exceptionally(e -> {
				// Handle exception
				context.disconnect(Component.translatable("effortlessbuilding.networking.perform_undo.failed", e.getMessage()));
				return null;
			});
		}
	}
}
