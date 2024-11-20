package nl.requios.effortlessbuilding.systems;

import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import nl.requios.effortlessbuilding.EffortlessBuildingClient;
import nl.requios.effortlessbuilding.compatibility.CompatHelper;
import nl.requios.effortlessbuilding.utilities.BlockEntry;
import nl.requios.effortlessbuilding.utilities.BlockSet;
import nl.requios.effortlessbuilding.utilities.PlaceChecker;

@OnlyIn(Dist.CLIENT)
public class BuilderFilter {
    public void filterOnCoordinates(BlockSet blocks, Player player) {
        var world = player.level();
        var iter = blocks.entrySet().iterator();
        while (iter.hasNext()) {
            var pos = iter.next().getValue().blockPos;
            boolean remove = false;

            if (!world.isLoaded(pos)) remove = true;
            if (!world.getWorldBorder().isWithinBounds(pos)) remove = true;

            if (remove) iter.remove();
        }
    }

    public void filterOnExistingBlockStates(BlockSet blocks, Player player) {
        var buildSettings = EffortlessBuildingClient.BUILD_SETTINGS;
        var buildingState = EffortlessBuildingClient.BUILDER_CHAIN.getPretendBuildingState();
        boolean placing = buildingState == BuilderChain.BuildingState.PLACING;

        var iter = blocks.entrySet().iterator();
        while (iter.hasNext()) {
            var blockEntry = iter.next().getValue();
            var blockState = blockEntry.existingBlockState;
            boolean remove = false;

            if (buildSettings.shouldProtectTileEntities() && blockState.hasBlockEntity()) remove = true;

            if (placing && !buildSettings.shouldReplaceFiltered()) {
                if (!buildSettings.shouldReplaceAir() && blockState.isAir()) remove = true;
                boolean isReplaceable = blockState.canBeReplaced();
//                boolean isSolid = blockState.isRedstoneConductor(player.level, blockEntry.blockPos);
                if (!buildSettings.shouldReplaceBlocks() && !isReplaceable) remove = true;
            }

            if (buildSettings.shouldReplaceFiltered()) {
                var offhandItem = player.getOffhandItem();
                if (!CompatHelper.containsBlock(offhandItem, blockState.getBlock())) remove = true;
            }

            if (remove) iter.remove();
        }


        //If the player is going to instabreak grass or a plant, only break other instabreaking things
//        boolean onlyInstaBreaking = !player.isCreative() &&
//                                    world.getBlockState(startCoordinates.get(0)).getDestroySpeed(world, startCoordinates.get(0)) == 0f;
//
//        //break all those blocks
//        for (int i = breakStartPos ? 0 : 1; i < coordinates.size(); i++) {
//            BlockPos coordinate = coordinates.get(i);
//            if (world.isLoaded(coordinate) && !world.isEmptyBlock(coordinate)) {
//                if (!onlyInstaBreaking || world.getBlockState(coordinate).getDestroySpeed(world, coordinate) == 0f) {
//                    SurvivalHelper.breakBlock(world, player, coordinate, false);
//                }
//            }
//        }
    }

    //Returns true if we should remove the entry
    public boolean filterOnNewBlockState(BlockEntry blockEntry, Player player) {
        var buildingState = EffortlessBuildingClient.BUILDER_CHAIN.getPretendBuildingState();
        boolean placing = buildingState == BuilderChain.BuildingState.PLACING;

        boolean remove = false;

        if (placing && !PlaceChecker.shouldPlaceBlock(player.level(), blockEntry)) remove = true;

        return remove;
    }
}
