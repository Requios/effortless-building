package nl.requios.effortlessbuilding.buildmodifier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import nl.requios.effortlessbuilding.utilities.BlockEntry;
import nl.requios.effortlessbuilding.utilities.BlockSet;

public class Array extends BaseModifier {

	public Vec3i offset = BlockPos.ZERO;
	public int count = 5;

	@Override
	public void findCoordinates(BlockSet blocks, Player player) {
		if (!enabled || offset.getX() == 0 && offset.getY() == 0 && offset.getZ() == 0) return;

		var originalBlocks = new BlockSet(blocks);
		for (BlockEntry blockEntry : originalBlocks) {
			var pos = blockEntry.blockPos;
			for (int i = 0; i < count; i++) {
				pos = pos.offset(offset);
				if (blocks.containsKey(pos)) continue;

				var newBlockEntry = new BlockEntry(pos);
				newBlockEntry.copyRotationSettingsFrom(blockEntry);
				blocks.add(newBlockEntry);
			}
		}
	}

	@Override
	public void onPowerLevelChanged(int powerLevel) {

	}

	public int getReach() {
		//find largest offset
		int x = Math.abs(offset.getX());
		int y = Math.abs(offset.getY());
		int z = Math.abs(offset.getZ());
		int largestOffset = Math.max(Math.max(x, y), z);

		return largestOffset * count;
	}

	@Override
	public CompoundTag serializeNBT() {
		var compound = super.serializeNBT();
		compound.putIntArray("offset", new int[]{offset.getX(), offset.getY(), offset.getZ()});
		compound.putInt("count", count);
		return compound;
	}

	@Override
	public void deserializeNBT(CompoundTag compound) {
		super.deserializeNBT(compound);
		int[] offsetArray = compound.getIntArray("offset");
		offset = new Vec3i(offsetArray[0], offsetArray[1], offsetArray[2]);
		count = compound.getInt("count");
	}
}
