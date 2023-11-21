package nl.requios.effortlessbuilding.buildmode;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import nl.requios.effortlessbuilding.EffortlessBuildingClient;
import nl.requios.effortlessbuilding.capability.CapabilityHandler;
import nl.requios.effortlessbuilding.utilities.BlockEntry;
import nl.requios.effortlessbuilding.utilities.BlockSet;

import java.util.ArrayList;
import java.util.List;

public abstract class ThreeClicksBuildMode extends BaseBuildMode {
	protected BlockEntry firstBlockEntry;
	protected BlockEntry secondBlockEntry;

	//Finds height after floor has been chosen in buildmodes with 3 clicks
	@Override
	public void initialize() {
		super.initialize();
		firstBlockEntry = null;
		secondBlockEntry = null;
	}

	@Override
	public boolean onClick(BlockSet blocks) {
		super.onClick(blocks);

		if (clicks == 1) {
			//First click, remember starting position
			firstBlockEntry = EffortlessBuildingClient.BUILDER_CHAIN.getStartPos();

			//If clicking in air, reset and try again
			if (firstBlockEntry == null) clicks = 0;

		} else if (clicks == 2) {
			//Second click, find second position

			if (blocks.size() == 0) {
				clicks = 0;
				return false;
			}

			var player = Minecraft.getInstance().player;
			var secondPos = findSecondPos(player, firstBlockEntry.blockPos, true);
			secondBlockEntry = new BlockEntry(secondPos);
		} else {
			//Third click, place blocks
			clicks = 0;
			return true;
		}
		return false;
	}

	@Override
	public void findCoordinates(BlockSet blocks) {
		if (clicks == 0) return;

		if (clicks == 1) {
			var player = Minecraft.getInstance().player;
			var firstPos = firstBlockEntry.blockPos;
			var secondPos = findSecondPos(player, firstBlockEntry.blockPos, true);
			if (secondPos == null) return;

			//Limit amount of blocks we can place per row
			int axisLimit = CapabilityHandler.getMaxBlocksPerAxis(player, false);

			int x1 = firstPos.getX(), x2 = secondPos.getX();
			int y1 = firstPos.getY(), y2 = secondPos.getY();
			int z1 = firstPos.getZ(), z2 = secondPos.getZ();

			//limit axis
			if (x2 - x1 >= axisLimit) x2 = x1 + axisLimit - 1;
			if (x1 - x2 >= axisLimit) x2 = x1 - axisLimit + 1;
			if (y2 - y1 >= axisLimit) y2 = y1 + axisLimit - 1;
			if (y1 - y2 >= axisLimit) y2 = y1 - axisLimit + 1;
			if (z2 - z1 >= axisLimit) z2 = z1 + axisLimit - 1;
			if (z1 - z2 >= axisLimit) z2 = z1 - axisLimit + 1;

			blocks.clear();
			for (BlockPos pos : getIntermediateBlocks(player, x1, y1, z1, x2, y2, z2)) {
				if (blocks.containsKey(pos)) continue;
				blocks.add(new BlockEntry(pos));
			}
			blocks.firstPos = firstPos;
			blocks.lastPos = secondPos;
		} else {
			var player = Minecraft.getInstance().player;
			BlockPos firstPos = firstBlockEntry.blockPos;
			BlockPos secondPos = secondBlockEntry.blockPos;
			BlockPos thirdPos = findThirdPos(player, firstPos, secondPos, true);
			if (thirdPos == null) return;

			//Limit amount of blocks you can place per row
			int axisLimit = CapabilityHandler.getMaxBlocksPerAxis(player, false);

			int x1 = firstPos.getX(), x2 = secondPos.getX(), x3 = thirdPos.getX();
			int y1 = firstPos.getY(), y2 = secondPos.getY(), y3 = thirdPos.getY();
			int z1 = firstPos.getZ(), z2 = secondPos.getZ(), z3 = thirdPos.getZ();

			//limit axis
			if (x2 - x1 >= axisLimit) x2 = x1 + axisLimit - 1;
			if (x1 - x2 >= axisLimit) x2 = x1 - axisLimit + 1;
			if (y2 - y1 >= axisLimit) y2 = y1 + axisLimit - 1;
			if (y1 - y2 >= axisLimit) y2 = y1 - axisLimit + 1;
			if (z2 - z1 >= axisLimit) z2 = z1 + axisLimit - 1;
			if (z1 - z2 >= axisLimit) z2 = z1 - axisLimit + 1;

			if (x3 - x1 >= axisLimit) x3 = x1 + axisLimit - 1;
			if (x1 - x3 >= axisLimit) x3 = x1 - axisLimit + 1;
			if (y3 - y1 >= axisLimit) y3 = y1 + axisLimit - 1;
			if (y1 - y3 >= axisLimit) y3 = y1 - axisLimit + 1;
			if (z3 - z1 >= axisLimit) z3 = z1 + axisLimit - 1;
			if (z1 - z3 >= axisLimit) z3 = z1 - axisLimit + 1;

			blocks.clear();
			for (BlockPos pos : getFinalBlocks(player, x1, y1, z1, x2, y2, z2, x3, y3, z3)) {
				if (blocks.containsKey(pos)) continue;
				blocks.add(new BlockEntry(pos));
			}
			blocks.firstPos = firstPos;
			blocks.lastPos = thirdPos;
		}
	}

	public static BlockPos findHeight(Player player, BlockPos secondPos, boolean skipRaytrace) {
		Vec3 look = BuildModes.getPlayerLookVec(player);
		Vec3 start = new Vec3(player.getX(), player.getY() + player.getEyeHeight(), player.getZ());

		List<HeightCriteria> criteriaList = new ArrayList<>(3);

		//X
		Vec3 xBound = BuildModes.findXBound(secondPos.getX(), start, look);
		criteriaList.add(new HeightCriteria(xBound, secondPos, start));

		//Z
		Vec3 zBound = BuildModes.findZBound(secondPos.getZ(), start, look);
		criteriaList.add(new HeightCriteria(zBound, secondPos, start));

		//Remove invalid criteria
		int reach = CapabilityHandler.getBuildModeReach(player);
		criteriaList.removeIf(criteria -> !criteria.isValid(start, look, reach, player, skipRaytrace));

		//If none are valid, return empty list of blocks
		if (criteriaList.isEmpty()) return null;

		//If only 1 is valid, choose that one
		HeightCriteria selected = criteriaList.get(0);

		//If multiple are valid, choose based on criteria
		if (criteriaList.size() > 1) {
			//Select the one that is closest (from wall position to its line counterpart)
			for (int i = 1; i < criteriaList.size(); i++) {
				HeightCriteria criteria = criteriaList.get(i);
				if (criteria.distToLineSq < 2.0 && selected.distToLineSq < 2.0) {
					//Both very close to line, choose closest to player
					if (criteria.distToPlayerSq < selected.distToPlayerSq)
						selected = criteria;
				} else {
					//Pick closest to line
					if (criteria.distToLineSq < selected.distToLineSq)
						selected = criteria;
				}
			}
		}
		return BlockPos.containing(selected.lineBound);
	}

//	protected abstract BlockEntry findSecondPos(List<BlockEntry> blocks);

	//Finds the place of the second block pos
//	@Deprecated
	protected abstract BlockPos findSecondPos(Player player, BlockPos firstPos, boolean skipRaytrace);

	//Finds the place of the third block pos
	protected abstract BlockPos findThirdPos(Player player, BlockPos firstPos, BlockPos secondPos, boolean skipRaytrace);

	//After first and second pos are known, we want to visualize the blocks in a way (like floor for cube)
	protected abstract List<BlockPos> getIntermediateBlocks(Player player, int x1, int y1, int z1, int x2, int y2, int z2);

	//After first, second and third pos are known, we want all the blocks
	protected abstract List<BlockPos> getFinalBlocks(Player player, int x1, int y1, int z1, int x2, int y2, int z2, int x3, int y3, int z3);

	static class HeightCriteria {
		Vec3 planeBound;
		Vec3 lineBound;
		double distToLineSq;
		double distToPlayerSq;

		HeightCriteria(Vec3 planeBound, BlockPos secondPos, Vec3 start) {
			this.planeBound = planeBound;
			this.lineBound = toLongestLine(this.planeBound, secondPos);
			this.distToLineSq = this.lineBound.subtract(this.planeBound).lengthSqr();
			this.distToPlayerSq = this.planeBound.subtract(start).lengthSqr();
		}

		//Make it from a plane into a line, on y axis only
		private Vec3 toLongestLine(Vec3 boundVec, BlockPos secondPos) {
			BlockPos bound = BlockPos.containing(boundVec);
			return new Vec3(secondPos.getX(), bound.getY(), secondPos.getZ());
		}

		//check if its not behind the player and its not too close and not too far
		//also check if raytrace from player to block does not intersect blocks
		public boolean isValid(Vec3 start, Vec3 look, int reach, Player player, boolean skipRaytrace) {

			return BuildModes.isCriteriaValid(start, look, reach, player, skipRaytrace, lineBound, planeBound, distToPlayerSq);
		}
	}
}
