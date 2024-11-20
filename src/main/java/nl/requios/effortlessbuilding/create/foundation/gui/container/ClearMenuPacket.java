//package nl.requios.effortlessbuilding.create.foundation.gui.container;
//
//import net.minecraft.network.FriendlyByteBuf;
//import net.minecraft.server.level.ServerPlayer;
//import net.neoforged.neoforge.network.NetworkEvent.Context;
//import nl.requios.effortlessbuilding.create.foundation.networking.SimplePacketBase;
//
//public class ClearMenuPacket extends SimplePacketBase {
//
//	public ClearMenuPacket() {}
//
//	public ClearMenuPacket(FriendlyByteBuf buffer) {}
//
//	@Override
//	public void write(FriendlyByteBuf buffer) {}
//
//	@Override
//	public boolean handle(Context context) {
//		context.enqueueWork(() -> {
//			ServerPlayer player = context.getSender();
//			if (player == null)
//				return;
//			if (!(player.containerMenu instanceof IClearableMenu))
//				return;
//			((IClearableMenu) player.containerMenu).clearContents();
//		});
//		return true;
//	}
//
//}
