//package nl.requios.effortlessbuilding.item;
//
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.core.Direction;
//import net.neoforged.neoforge.common.capabilities.Capability;
//import net.neoforged.neoforge.common.capabilities.Capabilities;
//import net.neoforged.neoforge.common.util.LazyOptional;
//import net.neoforged.neoforge.items.IItemHandler;
//import net.neoforged.neoforge.items.ItemStackHandler;
//import net.neoforged.neoforge.common.capabilities.ICapabilitySerializable;
//
//import javax.annotation.Nonnull;
//import javax.annotation.Nullable;
//
//public class ItemHandlerCapabilityProvider implements ICapabilitySerializable<CompoundTag> { TODO: Reimplement the ItemHandler stuff
//	IItemHandler itemHandler;
//
//	public ItemHandlerCapabilityProvider(int size) {
//		itemHandler = new ItemStackHandler(size);
//	}
//
//	@Nonnull
//	@Override
//	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
//		return Capabilities.ITEM_HANDLER.orEmpty(cap, LazyOptional.of(() -> itemHandler));
//	}
//
//	@Override
//	public CompoundTag serializeNBT() {
//		return ((ItemStackHandler) itemHandler).serializeNBT();
//	}
//
//	@Override
//	public void deserializeNBT(CompoundTag nbt) {
//		((ItemStackHandler) itemHandler).deserializeNBT(nbt);
//	}
//}
