package nl.requios.effortlessbuilding.buildmode;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import nl.requios.effortlessbuilding.EffortlessBuildingClient;
import nl.requios.effortlessbuilding.capability.CapabilityHandler;
import nl.requios.effortlessbuilding.utilities.BlockEntry;
import nl.requios.effortlessbuilding.utilities.BlockSet;

import java.util.List;

public abstract class TwoClicksBuildMode extends BaseBuildMode {

	protected BlockEntry firstBlockEntry;

	@Override
	public void initialize() {
		super.initialize();
		firstBlockEntry = null;
	}

	@Override
	public boolean onClick(BlockSet blocks) {
		super.onClick(blocks);

		if (clicks == 1) {
			//First click, remember starting position
			firstBlockEntry = EffortlessBuildingClient.BUILDER_CHAIN.getStartPos();

			//If clicking in air, reset and try again
			if (firstBlockEntry == null) clicks = 0;

		} else {
			//Second click, place blocks
			clicks = 0;
			return true;
		}
		return false;
	}

	@Override
	public void findCoordinates(BlockSet blocks) {
		if (clicks == 0) return;

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
		for (BlockPos pos : getAllBlocks(player, x1, y1, z1, x2, y2, z2)) {
			if (blocks.containsKey(pos)) continue;
			blocks.add(new BlockEntry(pos));
		}
		blocks.firstPos = firstPos;
		blocks.lastPos = secondPos;
	}

	//Finds the place of the second block pos based on criteria (floor must be on same height as first click, wall on same plane etc)
	protected abstract BlockPos findSecondPos(Player player, BlockPos firstPos, boolean skipRaytrace);

	//After first and second pos are known, we want all the blocks
	protected abstract List<BlockPos> getAllBlocks(Player player, int x1, int y1, int z1, int x2, int y2, int z2);
}
