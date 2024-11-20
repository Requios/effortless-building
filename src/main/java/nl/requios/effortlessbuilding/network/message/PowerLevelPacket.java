package nl.requios.effortlessbuilding.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.attachment.PowerLevel;

/**
 * Sync power level from server to client
 */
public record PowerLevelPacket(int powerLevel) implements CustomPacketPayload {
	public static final ResourceLocation ID = new ResourceLocation(EffortlessBuilding.MODID, "power_level");

	public PowerLevelPacket(FriendlyByteBuf buf) {
		this(buf.readInt());
	}

	public void write(FriendlyByteBuf buf) {
		buf.writeInt(powerLevel);
	}

	@Override
	public ResourceLocation id() {
		return ID;
	}

	public static class Handler {
		public static void handle(final PowerLevelPacket packet, final PlayPayloadContext context) {
			context.workHandler().submitAsync(() -> {
				if (context.player().isPresent()) {
					Player player = context.player().get();
					PowerLevel currentLevel = player.getData(EffortlessBuilding.POWER_LEVEL.get());
					currentLevel.setPowerLevel(packet.powerLevel);
					player.setData(EffortlessBuilding.POWER_LEVEL.get(), currentLevel);
				}
			}).exceptionally(e -> {
				// Handle exception
				context.packetHandler().disconnect(Component.translatable("effortlessbuilding.networking.power_level.failed", e.getMessage()));
				return null;
			});
		}
	}
}
