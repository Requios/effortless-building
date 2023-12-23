package nl.requios.effortlessbuilding.buildmodifier;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import nl.requios.effortlessbuilding.capability.CapabilityHandler;
import nl.requios.effortlessbuilding.utilities.BlockEntry;
import nl.requios.effortlessbuilding.utilities.BlockSet;

public class RadialMirror extends BaseModifier {

	public Vec3 position = new Vec3(0.5, 64.5, 0.5);
	public int slices = 4;
	public boolean alternate = false;
	public int radius = 20;
	public boolean drawLines = true;
	public boolean drawPlanes = false;

	public RadialMirror() {
		super();
		var player = Minecraft.getInstance().player;
		if (player != null)
			position = Vec3.atLowerCornerOf(Minecraft.getInstance().player.blockPosition());
	}

	@Override
	public void findCoordinates(BlockSet blocks, Player player) {
		if (!enabled) return;

		var originalBlocks = new BlockSet(blocks);
		for (BlockEntry blockEntry : originalBlocks) {
			if (!isWithinRange(blockEntry.blockPos)) continue;

			performRadialMirror(blocks, blockEntry);
		}
	}

	@Override
	public void onPowerLevelChanged(int powerLevel) {
		radius = CapabilityHandler.getMaxMirrorRadius(Minecraft.getInstance().player, false);
	}

	public void performRadialMirror(BlockSet blocks, BlockEntry blockEntry) {

		//get angle between slices
		double sliceAngle = 2 * Math.PI / slices;

		//Get start vector relative to mirror center
		Vec3 relStartVec = Vec3.atCenterOf(blockEntry.blockPos).subtract(position);

		double startAngleToCenter = Mth.atan2(relStartVec.x, relStartVec.z); //between -PI and PI
		//TODO change to Abs if alternative?
		if (startAngleToCenter < 0) startAngleToCenter += Math.PI;
		double startAngleInSlice = startAngleToCenter % sliceAngle;

		for (int i = 1; i < slices; i++) {
			double curAngle = sliceAngle * i;

			//alternate mirroring of slices
			boolean doAlternate = alternate && i % 2 == 1;
			if (doAlternate) {
				curAngle = curAngle - startAngleInSlice + (sliceAngle - startAngleInSlice);
			}

			Vec3 relNewVec = relStartVec.yRot((float) curAngle);
			BlockPos newBlockPos = BlockPos.containing(position.add(relNewVec));

			if (blocks.containsKey(newBlockPos)) continue;

			BlockEntry newBlockEntry = new BlockEntry(newBlockPos);
			newBlockEntry.copyRotationSettingsFrom(blockEntry);

			//rotate block
			double angleToCenter = Mth.atan2(relNewVec.x, relNewVec.z); //between -PI and PI
			rotateBlockEntry(newBlockEntry, angleToCenter, doAlternate);
			
			blocks.add(newBlockEntry);
		}
	}

	private void rotateBlockEntry(BlockEntry blockEntry, double angleToCenter, boolean alternate) {

		if (angleToCenter < -0.751 * Math.PI || angleToCenter > 0.749 * Math.PI) {
			blockEntry.rotation = blockEntry.rotation.getRotated(Rotation.CLOCKWISE_180);
			if (alternate) {
//				blockEntry.mirrorX = !blockEntry.mirrorX;
			}
		} else if (angleToCenter < -0.251 * Math.PI) {
			blockEntry.rotation = blockEntry.rotation.getRotated(Rotation.CLOCKWISE_90);
			if (alternate) {
//				blockEntry.mirrorZ = !blockEntry.mirrorZ;
			}
		} else if (angleToCenter > 0.249 * Math.PI) {
			blockEntry.rotation = blockEntry.rotation.getRotated(Rotation.COUNTERCLOCKWISE_90);
			if (alternate) {
//				blockEntry.mirrorZ = !blockEntry.mirrorZ;
			}
		} else {
			if (alternate) {
//				blockEntry.mirrorX = !blockEntry.mirrorX;
			}
		}
	}

	private static BlockState rotateOriginalBlockState(Player player, BlockPos startPos, double startAngleToCenter, BlockState blockState) {
		BlockState newBlockState = blockState;

		if (startAngleToCenter < -0.751 * Math.PI || startAngleToCenter > 0.749 * Math.PI) {
			newBlockState = blockState.rotate(player.level(), startPos, Rotation.CLOCKWISE_180);
		} else if (startAngleToCenter < -0.251 * Math.PI) {
			newBlockState = blockState.rotate(player.level(), startPos, Rotation.COUNTERCLOCKWISE_90);
		} else if (startAngleToCenter > 0.249 * Math.PI) {
			newBlockState = blockState.rotate(player.level(), startPos, Rotation.CLOCKWISE_90);
		}

		return newBlockState;
	}

	private static BlockState rotateBlockState(Player player, BlockPos startPos, Vec3 relVec, BlockState blockState, boolean alternate) {
		BlockState newBlockState;
		double angleToCenter = Mth.atan2(relVec.x, relVec.z); //between -PI and PI

		if (angleToCenter < -0.751 * Math.PI || angleToCenter > 0.749 * Math.PI) {
			newBlockState = blockState.rotate(player.level(), startPos, Rotation.CLOCKWISE_180);
			if (alternate) {
				newBlockState = newBlockState.mirror(Mirror.FRONT_BACK);
			}
		} else if (angleToCenter < -0.251 * Math.PI) {
			newBlockState = blockState.rotate(player.level(), startPos, Rotation.CLOCKWISE_90);
			if (alternate) {
				newBlockState = newBlockState.mirror(Mirror.LEFT_RIGHT);
			}
		} else if (angleToCenter > 0.249 * Math.PI) {
			newBlockState = blockState.rotate(player.level(), startPos, Rotation.COUNTERCLOCKWISE_90);
			if (alternate) {
				newBlockState = newBlockState.mirror(Mirror.LEFT_RIGHT);
			}
		} else {
			newBlockState = blockState;
			if (alternate) {
				newBlockState = newBlockState.mirror(Mirror.FRONT_BACK);
			}
		}

		return newBlockState;
	}

	public boolean isWithinRange(BlockPos startPos) {
		return (new Vec3(startPos.getX() + 0.5, startPos.getY() + 0.5, startPos.getZ() + 0.5).subtract(position).lengthSqr() < radius * radius);
	}

	public int getReach() {
		return radius * 2;
	}

	@Override
	public CompoundTag serializeNBT() {
		var compound = super.serializeNBT();
		compound.putDouble("positionX", position.x);
		compound.putDouble("positionY", position.y);
		compound.putDouble("positionZ", position.z);
		compound.putInt("slices", slices);
		compound.putBoolean("alternate", alternate);
		compound.putInt("radius", radius);
		compound.putBoolean("drawLines", drawLines);
		compound.putBoolean("drawPlanes", drawPlanes);
		return compound;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		super.deserializeNBT(nbt);
		position = new Vec3(nbt.getDouble("positionX"), nbt.getDouble("positionY"), nbt.getDouble("positionZ"));
		slices = nbt.getInt("slices");
		alternate = nbt.getBoolean("alternate");
		radius = nbt.getInt("radius");
		drawLines = nbt.getBoolean("drawLines");
		drawPlanes = nbt.getBoolean("drawPlanes");
	}
}
