package nl.requios.effortlessbuilding.buildmodifier;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import nl.requios.effortlessbuilding.capability.CapabilityHandler;
import nl.requios.effortlessbuilding.utilities.BlockEntry;
import nl.requios.effortlessbuilding.utilities.BlockSet;

public class Mirror extends BaseModifier {
	public Vec3 position = new Vec3(0.5, 64.5, 0.5);
	public boolean mirrorX = true;
	public boolean mirrorY = false;
	public boolean mirrorZ = false;
	public int radius = 10;
	public boolean drawLines = true;
	public boolean drawPlanes = true;
	
	public Mirror() {
		super();
		var player = Minecraft.getInstance().player;
		if (player != null)
			position = Vec3.atLowerCornerOf(Minecraft.getInstance().player.blockPosition());
	}
	
	@Override
	public void findCoordinates(BlockSet blocks, Player player) {
		if (!(enabled && (mirrorX || mirrorY || mirrorZ))) return;

		var originalBlocks = new BlockSet(blocks);
		for (BlockEntry blockEntry : originalBlocks) {
			if (!isWithinRange(blockEntry.blockPos)) continue;

			if (mirrorX) performMirrorX(blocks, blockEntry);
			if (mirrorY) performMirrorY(blocks, blockEntry);
			if (mirrorZ) performMirrorZ(blocks, blockEntry);
		}
	}

	@Override
	public void onPowerLevelChanged(int powerLevel) {
		radius = CapabilityHandler.getMaxMirrorRadius(Minecraft.getInstance().player, false);
	}

	private void performMirrorX(BlockSet blocks, BlockEntry blockEntry) {
		//find mirror position
		double x = position.x + (position.x - blockEntry.blockPos.getX() - 0.5);
		BlockPos newBlockPos = BlockPos.containing(x, blockEntry.blockPos.getY(), blockEntry.blockPos.getZ());

		if (blocks.containsKey(newBlockPos)) return;

		var newBlockEntry = new BlockEntry(newBlockPos);
		newBlockEntry.copyRotationSettingsFrom(blockEntry);
		newBlockEntry.mirrorX = !newBlockEntry.mirrorX;
		blocks.add(newBlockEntry);

		if (mirrorY) performMirrorY(blocks, newBlockEntry);
		if (mirrorZ) performMirrorZ(blocks, newBlockEntry);
	}

	private void performMirrorY(BlockSet blocks, BlockEntry blockEntry) {
		//find mirror position
		double y = position.y + (position.y - blockEntry.blockPos.getY() - 0.5);
		BlockPos newBlockPos = BlockPos.containing(blockEntry.blockPos.getX(), y, blockEntry.blockPos.getZ());

		if (blocks.containsKey(newBlockPos)) return;

		var newBlockEntry = new BlockEntry(newBlockPos);
		newBlockEntry.copyRotationSettingsFrom(blockEntry);
		newBlockEntry.mirrorY = !newBlockEntry.mirrorY;
		blocks.add(newBlockEntry);

		if (mirrorZ) performMirrorZ(blocks, newBlockEntry);
	}

	private void performMirrorZ(BlockSet blocks, BlockEntry blockEntry) {
		//find mirror position
		double z = position.z + (position.z - blockEntry.blockPos.getZ() - 0.5);
		BlockPos newBlockPos = BlockPos.containing(blockEntry.blockPos.getX(), blockEntry.blockPos.getY(), z);

		if (blocks.containsKey(newBlockPos)) return;

		var newBlockEntry = new BlockEntry(newBlockPos);
		newBlockEntry.copyRotationSettingsFrom(blockEntry);
		newBlockEntry.mirrorZ = !newBlockEntry.mirrorZ;
		blocks.add(newBlockEntry);
	}

	public boolean isWithinRange(BlockPos blockPos) {
		return !(blockPos.getX() + 0.5 < position.x - radius) && !(blockPos.getX() + 0.5 > position.x + radius) &&
			!(blockPos.getY() + 0.5 < position.y - radius) && !(blockPos.getY() + 0.5 > position.y + radius) &&
			!(blockPos.getZ() + 0.5 < position.z - radius) && !(blockPos.getZ() + 0.5 > position.z + radius);
	}
	
	public int getReach() {
		return radius * 2; //Change ModifierSettings#setReachUpgrade too
	}
	
	public void toggleMirrorAxis(int index) {
		switch (index) {
			case 0 -> mirrorX = !mirrorX;
			case 1 -> mirrorY = !mirrorY;
			case 2 -> mirrorZ = !mirrorZ;
		}
	}
	
	public boolean getMirrorAxis(int index) {
		switch (index) {
			case 0 -> {
				return mirrorX;
			}
			case 1 -> {
				return mirrorY;
			}
			case 2 -> {
				return mirrorZ;
			}
		}
		return false;
	}

	@Override
	public CompoundTag serializeNBT() {
		var compound = super.serializeNBT();
		compound.putDouble("positionX", position.x);
		compound.putDouble("positionY", position.y);
		compound.putDouble("positionZ", position.z);
		compound.putBoolean("mirrorX", mirrorX);
		compound.putBoolean("mirrorY", mirrorY);
		compound.putBoolean("mirrorZ", mirrorZ);
		compound.putInt("radius", radius);
		compound.putBoolean("drawLines", drawLines);
		compound.putBoolean("drawPlanes", drawPlanes);
		return compound;
	}

	@Override
	public void deserializeNBT(CompoundTag compound) {
		super.deserializeNBT(compound);
		position = new Vec3(compound.getDouble("positionX"), compound.getDouble("positionY"), compound.getDouble("positionZ"));
		mirrorX = compound.getBoolean("mirrorX");
		mirrorY = compound.getBoolean("mirrorY");
		mirrorZ = compound.getBoolean("mirrorZ");
		radius = compound.getInt("radius");
		drawLines = compound.getBoolean("drawLines");
		drawPlanes = compound.getBoolean("drawPlanes");
	}
}
