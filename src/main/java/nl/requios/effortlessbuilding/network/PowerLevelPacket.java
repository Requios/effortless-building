package nl.requios.effortlessbuilding.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import nl.requios.effortlessbuilding.capability.CapabilityHandler;

import java.util.function.Supplier;

/**
 * Sync power level from server to client
 */
public class PowerLevelPacket {

	private int powerLevel;

	public PowerLevelPacket() {
	}

	public PowerLevelPacket(int powerLevel) {
		this.powerLevel = powerLevel;
	}

	public static void encode(PowerLevelPacket message, FriendlyByteBuf buf) {
		buf.writeInt(message.powerLevel);
	}

	public static PowerLevelPacket decode(FriendlyByteBuf buf) {
		return new PowerLevelPacket(buf.readInt());
	}

	public static class Handler {
		public static void handle(PowerLevelPacket message, Supplier<NetworkEvent.Context> ctx) {
			NetworkEvent.Context context = ctx.get();
			if (context.getDirection().getReceptionSide().isClient()) {
				context.enqueueWork(new Runnable() {
					// Use anon - lambda causes classloading issues
					@Override
					public void run() {
						Player player = Minecraft.getInstance().player;
						if (player != null) {
							player.getCapability(CapabilityHandler.POWER_LEVEL_CAPABILITY, null)
									.ifPresent(levelCap -> {
										levelCap.setPowerLevel(message.powerLevel);
									});
						}
					}
				});
			}
			context.setPacketHandled(true);
		}
	}
}
