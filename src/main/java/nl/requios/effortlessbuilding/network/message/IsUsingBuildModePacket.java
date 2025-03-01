package nl.requios.effortlessbuilding.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.systems.ServerBuildState;

public record IsUsingBuildModePacket(boolean isUsingBuildMode) implements CustomPacketPayload {
	public static final StreamCodec<FriendlyByteBuf, IsUsingBuildModePacket> CODEC = StreamCodec.composite(
			ByteBufCodecs.BOOL,
			IsUsingBuildModePacket::isUsingBuildMode,
			IsUsingBuildModePacket::new);
	public static final Type<IsUsingBuildModePacket> ID = new Type<>(EffortlessBuilding.asResource("is_using_build_mode"));

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}

	public static class Handler {
		public static void handle(final IsUsingBuildModePacket packet, final IPayloadContext context) {
			context.enqueueWork(() -> {
				if (context.player() instanceof ServerPlayer player) {
					ServerBuildState.setIsUsingBuildMode(player, packet.isUsingBuildMode());
				}
			}).exceptionally(e -> {
				// Handle exception
				context.disconnect(Component.translatable("effortlessbuilding.networking.is_using_build_mode.failed", e.getMessage()));
				return null;
			});
		}
	}
}
