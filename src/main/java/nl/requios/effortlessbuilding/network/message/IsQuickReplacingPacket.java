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

public record IsQuickReplacingPacket(boolean isQuickReplacing) implements CustomPacketPayload {
	public static final StreamCodec<FriendlyByteBuf, IsQuickReplacingPacket> CODEC = StreamCodec.composite(
			ByteBufCodecs.BOOL,
			IsQuickReplacingPacket::isQuickReplacing,
			IsQuickReplacingPacket::new);
	public static final Type<IsQuickReplacingPacket> ID = new Type<>(EffortlessBuilding.asResource("is_quick_replacing"));

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}

	public static class Handler {
		public static void handle(final IsQuickReplacingPacket packet, final IPayloadContext context) {
			context.enqueueWork(() -> {
				if (context.player() instanceof ServerPlayer player) {
					ServerBuildState.setIsQuickReplacing(player, packet.isQuickReplacing());
				}
			}).exceptionally(e -> {
				// Handle exception
				context.disconnect(Component.translatable("effortlessbuilding.networking.is_quick_replacing.failed", e.getMessage()));
				return null;
			});
		}
	}
}
