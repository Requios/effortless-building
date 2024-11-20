//package nl.requios.effortlessbuilding.create.foundation.gui.container;
//
//import net.minecraft.network.FriendlyByteBuf;
//import net.minecraft.server.level.ServerPlayer;
//import net.minecraft.world.item.ItemStack;
//import net.neoforged.neoforge.network.NetworkEvent.Context;
//import nl.requios.effortlessbuilding.create.foundation.networking.SimplePacketBase;
//
//public class GhostItemSubmitPacket extends SimplePacketBase {
//
//	private final ItemStack item;
//	private final int slot;
//
//	public GhostItemSubmitPacket(ItemStack item, int slot) {
//		this.item = item;
//		this.slot = slot;
//	}
//
//	public GhostItemSubmitPacket(FriendlyByteBuf buffer) {
//		item = buffer.readItem();
//		slot = buffer.readInt();
//	}
//
//	@Override
//	public void write(FriendlyByteBuf buffer) {
//		buffer.writeItem(item);
//		buffer.writeInt(slot);
//	}
//
//	@Override
//	public boolean handle(Context context) {
//		context.enqueueWork(() -> {
//			ServerPlayer player = context.getSender();
//			if (player == null)
//				return;
//
//			if (player.containerMenu instanceof GhostItemMenu<?> menu) {
//				menu.ghostInventory.setStackInSlot(slot, item);
//				menu.getSlot(36 + slot).setChanged();
//			}
//		});
//		return true;
//	}
//
//}
