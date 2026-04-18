package nl.requios.effortlessbuilding.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemHandlerCapabilityProvider implements ICapabilitySerializable<CompoundTag> {
	private static final String NBT_KEY = "BagInventoryData";
	private final ItemStack stack;
	private final int size;
	private ItemStackHandler itemHandler;
	private final LazyOptional<IItemHandler> lazy;

	public ItemHandlerCapabilityProvider(int size, ItemStack stack) {
		this.size = size;
		this.stack = stack;
		this.itemHandler = createHandler();
		this.lazy = LazyOptional.of(() -> itemHandler);
		loadFromStack();
	}

	private ItemStackHandler createHandler() {
		return new ItemStackHandler(size) {
			@Override
			protected void onContentsChanged(int slot) {
				saveToStack();
			}
		};
	}

	private void saveToStack() {
		stack.getOrCreateTag().put(NBT_KEY, itemHandler.serializeNBT());
	}

	private void loadFromStack() {
		CompoundTag tag = stack.getTag();
		if (tag != null && tag.contains(NBT_KEY)) {
			itemHandler.deserializeNBT(tag.getCompound(NBT_KEY));
		}
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
		return ForgeCapabilities.ITEM_HANDLER.orEmpty(cap, lazy);
	}

	@Override
	public CompoundTag serializeNBT() {
		return itemHandler.serializeNBT();
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		itemHandler.deserializeNBT(nbt);
		saveToStack();
	}
}
