package nl.requios.effortlessbuilding.render;

import net.createmod.catnip.data.Pair;
import net.createmod.catnip.outliner.Outliner;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import nl.requios.effortlessbuilding.*;
import nl.requios.effortlessbuilding.buildmode.BuildModeEnum;
import nl.requios.effortlessbuilding.create.AllSpecialTextures;
import nl.requios.effortlessbuilding.create.CreateClient;
import nl.requios.effortlessbuilding.systems.BuilderChain;
import nl.requios.effortlessbuilding.utilities.BlockEntry;
import nl.requios.effortlessbuilding.utilities.BlockSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class BlockPreviews {
	private final List<PlacedBlocksEntry> placedBlocksList = new ArrayList<>();

	public void onTick() {
		var player = Minecraft.getInstance().player;

		drawPlacedBlocks();
		drawLookAtPreview(player);
		drawOutlineAtBreakPosition(player);
	}

	public void drawPlacedBlocks() {
		//Render placed blocks with appear animation
		if (ClientConfig.visuals.showBlockPreviews.get()) {
			for (PlacedBlocksEntry placed : placedBlocksList) {

				int totalTime = placed.breaking ? ClientConfig.visuals.breakAnimationLength.get() : ClientConfig.visuals.appearAnimationLength.get();
				if (totalTime <= 0) continue;

				float dissolve = (ClientEvents.ticksInGame - placed.time) / (float) totalTime;
				renderBlockPreviews(placed.blocks, placed.breaking, dissolve);
			}
		}

		//Expire
		placedBlocksList.removeIf(placed -> {
			int totalTime = placed.breaking ? ClientConfig.visuals.breakAnimationLength.get() : ClientConfig.visuals.appearAnimationLength.get();
			return placed.time + totalTime < ClientEvents.ticksInGame;
		});
	}

	public void drawLookAtPreview(Player player) {
		var blocks = EffortlessBuildingClient.BUILDER_CHAIN.getBlocks();
		if (blocks.size() == 0) return;
		
		if (EffortlessBuildingClient.BUILD_MODES.getBuildMode() == BuildModeEnum.DISABLED &&
			blocks.size() == 1) return;
		if (EffortlessBuildingClient.BUILDER_CHAIN.getBuildingState() == BuilderChain.BuildingState.IDLE &&
			ClientConfig.visuals.onlyShowBlockPreviewsWhenBuilding.get() &&
			blocks.size() == 1) return;

		var coordinates = blocks.getCoordinates();
		var state = EffortlessBuildingClient.BUILDER_CHAIN.getPretendBuildingState();

		//Dont fade out the outline if we are still determining where to place
		//Every outline with same ID will not fade out (because it gets replaced)
		Object outlineID = "single";
		if (blocks.size() > 1) outlineID = blocks.firstPos;

		if (state != BuilderChain.BuildingState.BREAKING) {
			//Use fancy shader if config allows, otherwise outlines
			if (ClientConfig.visuals.showBlockPreviews.get() && blocks.size() < ClientConfig.visuals.maxBlockPreviews.get()) {
				renderBlockPreviews(blocks, false, 0f);

				Outliner.getInstance().showCluster(outlineID, coordinates)
						.withFaceTexture(AllSpecialTextures.CHECKERED)
						.disableLineNormals()
						.lineWidth(1 / 32f)
						.colored(new Color(1f, 1f, 1f, 1f));
			} else {
				//Thicker outline without block previews
				Outliner.getInstance().showCluster(outlineID, coordinates)
						.withFaceTexture(AllSpecialTextures.HIGHLIGHT_CHECKERED)
						.disableLineNormals()
						.lineWidth(1 / 16f)
						.colored(new Color(1f, 1f, 1f, 1f));
			}

		} else {
			//Breaking
			Outliner.getInstance().showCluster(outlineID, coordinates)
					.withFaceTexture(AllSpecialTextures.THIN_CHECKERED)
					.disableLineNormals()
					.lineWidth(1 / 16f)
					.colored(new Color(0.8f, 0.1f, 0.1f, 1f));
		}

		//Display block count and dimensions in actionbar
		if (state != BuilderChain.BuildingState.IDLE) {

			//Find min and max values (not simply firstPos and secondPos because that doesn't work with circles)
			int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
			int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
			int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
			for (BlockPos pos : coordinates) {
				if (pos.getX() < minX) minX = pos.getX();
				if (pos.getX() > maxX) maxX = pos.getX();
				if (pos.getY() < minY) minY = pos.getY();
				if (pos.getY() > maxY) maxY = pos.getY();
				if (pos.getZ() < minZ) minZ = pos.getZ();
				if (pos.getZ() > maxZ) maxZ = pos.getZ();
			}
			BlockPos dim = new BlockPos(maxX - minX + 1, maxY - minY + 1, maxZ - minZ + 1);

			String dimensions = "(";
			if (dim.getX() > 1) dimensions += dim.getX() + "x";
			if (dim.getZ() > 1) dimensions += dim.getZ() + "x";
			if (dim.getY() > 1) dimensions += dim.getY() + "x";
			dimensions = dimensions.substring(0, dimensions.length() - 1);
			if (dimensions.length() > 1) dimensions += ")";

			String msg = blocks.size() + " blocks " + dimensions;
			EffortlessBuilding.log(player, msg, true);
		}
	}

	public void drawOutlineAtBreakPosition(Player player) {
		if (EffortlessBuildingClient.BUILD_MODES.getBuildMode() == BuildModeEnum.DISABLED) return;

		BuilderChain builderChain = EffortlessBuildingClient.BUILDER_CHAIN;
		BlockPos pos = builderChain.getStartPosForBreaking();
		if (pos == null) return;

		var abilitiesState = builderChain.getAbilitiesState();
		if (ClientConfig.visuals.onlyShowBlockPreviewsWhenBuilding.get()) {
			if (abilitiesState == BuilderChain.AbilitiesState.NONE) return;
		} else {
			if (abilitiesState != BuilderChain.AbilitiesState.CAN_BREAK) return;
		}

		//Only render if further than normal reach
		if (EffortlessBuildingClient.BUILDER_CHAIN.getLookingAtNear() != null) return;

		AABB aabb = new AABB(pos);
		if (player.level().isLoaded(pos)) {
			var blockState = player.level().getBlockState(pos);
			if (!blockState.isAir()) {
				aabb = blockState.getShape(player.level(), pos).bounds().move(pos);
			}
		}

		Outliner.getInstance().showAABB("break", aabb)
				.disableLineNormals()
				.lineWidth(1 / 64f)
				.colored(0x222222);
	}

	protected void renderBlockPreviews(BlockSet blocks, boolean breaking, float dissolve) {

		for (BlockEntry blockEntry : blocks) {
			renderBlockPreview(blockEntry, breaking, dissolve, blocks.firstPos, blocks.lastPos);
		}
	}

	protected void renderBlockPreview(BlockEntry blockEntry, boolean breaking, float dissolve, BlockPos firstPos, BlockPos lastPos) {
		if (blockEntry.newBlockState == null) return;

		var blockPos = blockEntry.blockPos;
		var blockState = blockEntry.newBlockState;

		float scale = 0.5f;
		float alpha = 0.7f;
		if (dissolve > 0f) {
			float animationLength = 0.8f;

			double firstToSecond = lastPos.distSqr(firstPos);
			double place = 0;
			if (firstToSecond > 0.5) {
				double placeFromFirst = firstPos.distSqr(blockPos) / firstToSecond;
				double placeFromSecond = lastPos.distSqr(blockPos) / firstToSecond;
				place = (placeFromFirst + (1.0 - placeFromSecond)) / 2.0;
			} //else only one block

			//Scale place so we start first animation at 0 and end last animation at 1
			place *= 1f - animationLength;
			float diff = dissolve - (float) place;
			float t = diff / animationLength;
			t = Mth.clamp(t, 0, 1);
			//Now we got a usable t value for this block

//			t = (float) Mth.smoothstep(t);
			t = gain(t, 0.5f);

			if (!breaking) {
				scale = 0.5f + (t * 0.3f);
				alpha = 0.7f + (t * 0.3f);
			} else {
				t = 1f - t;
				scale = 0.5f + (t * 0.5f);
				alpha = 0.7f + (t * 0.3f);
			}
			alpha = Mth.clamp(alpha, 0, 1);
		}

		CreateClient.GHOST_BLOCKS.showGhostState(blockPos.toShortString(), blockState)
				.at(blockPos)
				.alpha(alpha)
				.scale(scale)
				.colored(blockEntry.invalid ? Color.RED : Color.WHITE);
	}

	//k=1 is the identity curve, k<1 produces the classic gain() shape, and k>1 produces "s" shaped curves. The curves are symmetric (and inverse) for k=a and k=1/a.
	//https://iquilezles.org/articles/functions/
	private float gain(float x, float k)
	{
        float a = (float) (0.5 * Math.pow(2.0 * ((x < 0.5) ? x : 1.0 - x), k));
		return (x < 0.5) ? a : (1.0f - a);
	}

	public void onBlocksPlaced(BlockSet blocks) {
		if (!ClientConfig.visuals.showBlockPreviews.get()) return;
		if (blocks.size() <= 1 || blocks.size() > ClientConfig.visuals.maxBlockPreviews.get()) return;

		placedBlocksList.add(new PlacedBlocksEntry(ClientEvents.ticksInGame, false, new BlockSet(blocks)));

		Outliner.getInstance().keep(Pair.of(blocks.firstPos, ClientConfig.visuals.appearAnimationLength.get()));
	}

	public void onBlocksBroken(BlockSet blocks) {
		if (!ClientConfig.visuals.showBlockPreviews.get()) return;
		if (blocks.size() <= 1 || blocks.size() > ClientConfig.visuals.maxBlockPreviews.get()) return;

		placedBlocksList.add(new PlacedBlocksEntry(ClientEvents.ticksInGame, true, new BlockSet(blocks)));

		Outliner.getInstance().keep(Pair.of(blocks.firstPos, ClientConfig.visuals.breakAnimationLength.get()));
	}

	private void sortOnDistanceToPlayer(List<BlockPos> coordinates, Player player) {

		Collections.sort(coordinates, (lhs, rhs) -> {
			// -1 - less than, 1 - greater than, 0 - equal
			double lhsDistanceToPlayer = Vec3.atLowerCornerOf(lhs).subtract(player.getEyePosition(1f)).lengthSqr();
			double rhsDistanceToPlayer = Vec3.atLowerCornerOf(rhs).subtract(player.getEyePosition(1f)).lengthSqr();
			return (int) Math.signum(lhsDistanceToPlayer - rhsDistanceToPlayer);
		});

	}

	public static class PlacedBlocksEntry {
		float time;
		boolean breaking;
		BlockSet blocks;

		public PlacedBlocksEntry(float time, boolean breaking, BlockSet blocks) {
			this.time = time;
			this.breaking = breaking;
			this.blocks = blocks;
		}
	}
}
