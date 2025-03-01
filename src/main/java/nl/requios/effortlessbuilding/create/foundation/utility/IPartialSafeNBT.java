package nl.requios.effortlessbuilding.create.foundation.utility;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

public interface IPartialSafeNBT {
	/**
	 * This will always be called from the logical server
	 */
	void writeSafe(CompoundTag compound, HolderLookup.Provider registries);
}
