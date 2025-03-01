package nl.requios.effortlessbuilding.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import nl.requios.effortlessbuilding.EffortlessBuilding;

/**
 * Send packet to client to translate and log the containing message
 */
public record TranslatedLogPacket(String prefix, String translationKey, String suffix,
                                  boolean actionBar) implements CustomPacketPayload {
	public static final StreamCodec<FriendlyByteBuf, TranslatedLogPacket> CODEC = StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8,
			TranslatedLogPacket::prefix,
			ByteBufCodecs.STRING_UTF8,
			TranslatedLogPacket::translationKey,
			ByteBufCodecs.STRING_UTF8,
			TranslatedLogPacket::suffix,
			ByteBufCodecs.BOOL,
			TranslatedLogPacket::actionBar,
			TranslatedLogPacket::new);

	public static final Type<TranslatedLogPacket> ID = new Type<>(EffortlessBuilding.asResource("translated_log"));

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}

	public static class Handler {
		public static void handle(final TranslatedLogPacket packet, final IPayloadContext context) {
			context.enqueueWork(() -> {
				if (context.player() != null) {
					Player player = context.player();
					EffortlessBuilding.logTranslate(player, packet.prefix(), packet.translationKey(), packet.suffix(), packet.actionBar());
				}
			}).exceptionally(e -> {
				// Handle exception
				context.disconnect(Component.translatable("effortlessbuilding.networking.translated_log.failed", e.getMessage()));
				return null;
			});
		}
	}
}
