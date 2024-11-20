package nl.requios.effortlessbuilding.network.message;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.EffortlessBuildingClient;

/**
 * Sync build modifiers between server and client, for saving and loading.
 */
public record ModifierSettingsPacket(CompoundTag modifiersTag) implements CustomPacketPayload {
	public static final ResourceLocation ID = new ResourceLocation(EffortlessBuilding.MODID, "modifier_settings");

	private static final String DATA_KEY = EffortlessBuilding.MODID + ":buildModifiers";

	public ModifierSettingsPacket(FriendlyByteBuf buf) {
		this(buf.readNbt());
	}

	public ModifierSettingsPacket(Player player) {
		this(player.getPersistentData().getCompound(DATA_KEY));
	}

	public void write(FriendlyByteBuf buf) {
		buf.writeNbt(modifiersTag);
	}

	@Override
	public ResourceLocation id() {
		return ID;
	}

	public static class ServerHandler {
		public static void handleServer(final ModifierSettingsPacket packet, final PlayPayloadContext context) {
			context.workHandler().submitAsync(() -> {
				if (context.player().isPresent()) {
					Player player = context.player().get();
					//To server, save to persistent player data
					player.getPersistentData().put(DATA_KEY, packet.modifiersTag());
				}
			}).exceptionally(e -> {
				// Handle exception
				context.packetHandler().disconnect(Component.translatable("effortlessbuilding.networking.modifier_settings.failed", e.getMessage()));
				return null;
			});
		}
	}

	public static class ClientHandler {
		public static void handleClient(final ModifierSettingsPacket packet, final PlayPayloadContext context) {
			context.workHandler().submitAsync(() -> {
				if (context.player().isPresent()) {
					Player player = context.player().get();
					//To client, load into system
					EffortlessBuildingClient.BUILD_MODIFIERS.deserializeNBT(packet.modifiersTag());
				}
			}).exceptionally(e -> {
				// Handle exception
				context.packetHandler().disconnect(Component.translatable("effortlessbuilding.networking.modifier_settings.failed", e.getMessage()));
				return null;
			});
		}
	}
}
