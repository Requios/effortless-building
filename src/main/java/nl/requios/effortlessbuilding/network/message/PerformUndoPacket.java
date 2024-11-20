package nl.requios.effortlessbuilding.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import nl.requios.effortlessbuilding.EffortlessBuilding;

public record PerformUndoPacket() implements CustomPacketPayload {
	public static final ResourceLocation ID = new ResourceLocation(EffortlessBuilding.MODID, "perform_undo");

	public PerformUndoPacket(FriendlyByteBuf buf) {
		this();
	}

	public void write(FriendlyByteBuf buf) {}

	@Override
	public ResourceLocation id() {
		return ID;
	}

	public static class Handler {
		public static void handle(final PerformUndoPacket packet, final PlayPayloadContext context) {
			context.workHandler().submitAsync(() -> {
				if (context.player().isPresent()) {
					Player player = context.player().get();
					EffortlessBuilding.UNDO_REDO.undo(player);
				}
			}).exceptionally(e -> {
				// Handle exception
				context.packetHandler().disconnect(Component.translatable("effortlessbuilding.networking.perform_undo.failed", e.getMessage()));
				return null;
			});
		}
	}
}
