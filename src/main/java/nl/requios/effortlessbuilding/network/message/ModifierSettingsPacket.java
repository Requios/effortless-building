package nl.requios.effortlessbuilding.network.message;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.EffortlessBuildingClient;

/**
 * Sync build modifiers between server and client, for saving and loading.
 */
public record ModifierSettingsPacket(CompoundTag modifiersTag) implements CustomPacketPayload {
	public static final StreamCodec<FriendlyByteBuf, ModifierSettingsPacket> CODEC = StreamCodec.composite(
			ByteBufCodecs.COMPOUND_TAG,
			ModifierSettingsPacket::modifiersTag,
			ModifierSettingsPacket::new);
	public static final Type<ModifierSettingsPacket> ID = new Type<>(EffortlessBuilding.asResource("modifier_settings"));

	private static final String DATA_KEY = EffortlessBuilding.MODID + ":buildModifiers";

	public ModifierSettingsPacket(Player player) {
		this(player.getPersistentData().getCompound(DATA_KEY));
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}

	public static class ServerHandler {
		public static void handleServer(final ModifierSettingsPacket packet, final IPayloadContext context) {
			context.enqueueWork(() -> {
				if (context.flow().isServerbound() && context.player() instanceof ServerPlayer player) {
					//To server, save to persistent player data
					player.getPersistentData().put(DATA_KEY, packet.modifiersTag());
				}
			}).exceptionally(e -> {
				// Handle exception
				context.disconnect(Component.translatable("effortlessbuilding.networking.modifier_settings.failed", e.getMessage()));
				return null;
			});
		}
	}

	public static class ClientHandler {
		public static void handleClient(final ModifierSettingsPacket packet, final IPayloadContext context) {
			context.enqueueWork(() -> {
				if (context.flow().isClientbound()) {
					Player player = context.player();
					//To client, load into system
					EffortlessBuildingClient.BUILD_MODIFIERS.deserializeNBT(packet.modifiersTag());
				}
			}).exceptionally(e -> {
				// Handle exception
				context.disconnect(Component.translatable("effortlessbuilding.networking.modifier_settings.failed", e.getMessage()));
				return null;
			});
		}
	}
}
