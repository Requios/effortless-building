package nl.requios.effortlessbuilding.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import nl.requios.effortlessbuilding.EffortlessBuilding;

/**
 * Send packet to client to translate and log the containing message
 */
public record TranslatedLogPacket(String prefix, String translationKey, String suffix, boolean actionBar) implements CustomPacketPayload {
	public static final ResourceLocation ID = new ResourceLocation(EffortlessBuilding.MODID, "translated_log");

	public TranslatedLogPacket(FriendlyByteBuf buf) {
		this(buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readBoolean());
	}

	public TranslatedLogPacket(String prefix, String translationKey, String suffix, boolean actionBar) {
		this.prefix = prefix;
		this.translationKey = translationKey;
		this.suffix = suffix;
		this.actionBar = actionBar;
	}

	public void write(FriendlyByteBuf buf) {
		buf.writeUtf(prefix);
		buf.writeUtf(translationKey);
		buf.writeUtf(suffix);
		buf.writeBoolean(actionBar);
	}

	@Override
	public ResourceLocation id() {
		return ID;
	}

	public String getPrefix() {
		return prefix;
	}

	public String getTranslationKey() {
		return translationKey;
	}

	public String getSuffix() {
		return suffix;
	}

	public boolean isActionBar() {
		return actionBar;
	}

	public static class Handler {
		public static void handle(final TranslatedLogPacket packet, final PlayPayloadContext context) {
			context.workHandler().submitAsync(() -> {
				if (context.player().isPresent()) {
					Player player = context.player().get();
					EffortlessBuilding.logTranslate(player, packet.prefix(), packet.translationKey(), packet.suffix(), packet.actionBar());
				}
			}).exceptionally(e -> {
				// Handle exception
				context.packetHandler().disconnect(Component.translatable("effortlessbuilding.networking.translated_log.failed", e.getMessage()));
				return null;
			});
		}
	}
}
