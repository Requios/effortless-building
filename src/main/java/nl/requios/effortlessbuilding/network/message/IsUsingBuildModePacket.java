package nl.requios.effortlessbuilding.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.systems.ServerBuildState;

public record IsUsingBuildModePacket(boolean isUsingBuildMode) implements CustomPacketPayload {
	public static final ResourceLocation ID = new ResourceLocation(EffortlessBuilding.MODID, "is_using_build_mode");

	public IsUsingBuildModePacket(FriendlyByteBuf buf) {
		this(buf.readBoolean());
	}

	public void write(FriendlyByteBuf buf) {
		buf.writeBoolean(isUsingBuildMode);
	}

	@Override
	public ResourceLocation id() {
		return ID;
	}

	public static class Handler {
		public static void handle(final IsUsingBuildModePacket packet, final PlayPayloadContext context) {
			context.workHandler().submitAsync(() -> {
				if (context.player().isPresent()) {
					Player player = context.player().get();
					ServerBuildState.setIsUsingBuildMode(player, packet.isUsingBuildMode());
				}
			}).exceptionally(e -> {
				// Handle exception
				context.packetHandler().disconnect(Component.translatable("effortlessbuilding.networking.is_using_build_mode.failed", e.getMessage()));
				return null;
			});
		}
	}
}
