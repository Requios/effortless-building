//package nl.requios.effortlessbuilding.create.foundation.networking;
//
//import net.minecraft.client.Minecraft;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.network.FriendlyByteBuf;
//import net.minecraft.world.entity.Entity;
//import net.neoforged.neoforge.network.NetworkEvent.Context;
//
//import java.util.HashSet;
//
//public interface ISyncPersistentData {
//
//	void onPersistentDataUpdated();
//
//	public static class PersistentDataPacket extends SimplePacketBase {
//
//		private int entityId;
//		private Entity entity;
//		private CompoundTag readData;
//
//		public PersistentDataPacket(Entity entity) {
//			this.entity = entity;
//			this.entityId = entity.getId();
//		}
//
//		public PersistentDataPacket(FriendlyByteBuf buffer) {
//			entityId = buffer.readInt();
//			readData = buffer.readNbt();
//		}
//
//		@Override
//		public void write(FriendlyByteBuf buffer) {
//			buffer.writeInt(entityId);
//			buffer.writeNbt(entity.getPersistentData());
//		}
//
//		@Override
//		public boolean handle(Context context) {
//			context.enqueueWork(() -> {
//				Entity entityByID = Minecraft.getInstance().level.getEntity(entityId);
//				CompoundTag data = entityByID.getPersistentData();
//				new HashSet<>(data.getAllKeys()).forEach(data::remove);
//				data.merge(readData);
//				if (!(entityByID instanceof ISyncPersistentData))
//					return;
//				((ISyncPersistentData) entityByID).onPersistentDataUpdated();
//			});
//			return true;
//		}
//
//	}
//
//}
