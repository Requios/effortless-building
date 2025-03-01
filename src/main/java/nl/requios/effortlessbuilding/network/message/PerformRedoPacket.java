package nl.requios.effortlessbuilding.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import nl.requios.effortlessbuilding.EffortlessBuilding;

public record PerformRedoPacket() implements CustomPacketPayload {

	public static final StreamCodec<FriendlyByteBuf, PerformRedoPacket> CODEC = CustomPacketPayload.codec(
			PerformRedoPacket::write,
			PerformRedoPacket::new);
	public static final Type<PerformRedoPacket> ID = new Type<>(EffortlessBuilding.asResource("perform_redo"));

	public PerformRedoPacket(FriendlyByteBuf buf) {
		this();
	}

	public void write(FriendlyByteBuf buf) {
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}

	public static class Handler {
		public static void handle(final PerformRedoPacket packet, final IPayloadContext context) {
			context.enqueueWork(() -> {
				if (context.player() instanceof ServerPlayer player) {
					EffortlessBuilding.UNDO_REDO.redo(player);
				}
			}).exceptionally(e -> {
				// Handle exception
				context.disconnect(Component.translatable("effortlessbuilding.networking.perform_undo.failed", e.getMessage()));
				return null;
			});
		}
	}
}
