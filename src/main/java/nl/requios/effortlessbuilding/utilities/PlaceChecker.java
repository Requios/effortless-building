package nl.requios.effortlessbuilding.utilities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import nl.requios.effortlessbuilding.EffortlessBuildingClient;

@OnlyIn(Dist.CLIENT)
public class PlaceChecker {

    //SchematicPrinter::shouldPlaceBlock
    public static boolean shouldPlaceBlock(Level world, BlockEntry blockEntry) {
        if (world == null || blockEntry == null)
            return false;

        var pos = blockEntry.blockPos;
        var state = blockEntry.newBlockState;
        BlockEntity tileEntity = null;

        if (state == null)
            return false;

        BlockState toReplace = world.getBlockState(pos);
        BlockEntity toReplaceTE = world.getBlockEntity(pos);
        BlockState toReplaceOther = null;

        if (state.hasProperty(BlockStateProperties.BED_PART) && state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
            && state.getValue(BlockStateProperties.BED_PART) == BedPart.FOOT)
            toReplaceOther = world.getBlockState(pos.relative(state.getValue(BlockStateProperties.HORIZONTAL_FACING)));
        if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)
            && state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER)
            toReplaceOther = world.getBlockState(pos.above());

//        if (!world.isLoaded(pos))
//            return false;
//        if (!world.getWorldBorder().isWithinBounds(pos))
//            return false;
        if (toReplace == state)
            return false;
        if (toReplace.getDestroySpeed(world, pos) == -1
            || (toReplaceOther != null && toReplaceOther.getDestroySpeed(world, pos) == -1))
            return false;
        if (EffortlessBuildingClient.BUILD_SETTINGS.shouldProtectTileEntities() && toReplaceOther != null && toReplaceOther.hasBlockEntity())
            return false;

        boolean isNormalCube = state.isRedstoneConductor(world, pos);
        return shouldPlace(world, pos, state, tileEntity, toReplace, toReplaceOther, isNormalCube);
    }

    //SchematicannonTileEntity::shouldPlace
    private static boolean shouldPlace(Level level, BlockPos pos, BlockState state, BlockEntity tileEntity, BlockState toReplace,
                                  BlockState toReplaceOther, boolean isNormalCube) {
//        if (!replaceTileEntities
//            && (toReplace.hasBlockEntity() || (toReplaceOther != null && toReplaceOther.hasBlockEntity())))
//            return false;

        if (shouldIgnoreBlockState(state))
            return false;

//        boolean placingAir = state.isAir();
//
//        if (replaceMode == 3)
//            return true;
//        if (replaceMode == 2 && !placingAir)
//            return true;
//        if (replaceMode == 1 && (isNormalCube || (!toReplace.isRedstoneConductor(level, pos)
//            && (toReplaceOther == null || !toReplaceOther.isRedstoneConductor(level, pos)))) && !placingAir)
//            return true;
//        if (replaceMode == 0 && !toReplace.isRedstoneConductor(level, pos)
//            && (toReplaceOther == null || !toReplaceOther.isRedstoneConductor(level, pos)) && !placingAir)
//            return true;
//
        return true;
    }

    //SchematicannonTileEntity::shouldIgnoreBlockState
    private static boolean shouldIgnoreBlockState(BlockState state) {
        // Block doesn't have a mapping (Water, lava, etc)
        if (state.getBlock() == Blocks.STRUCTURE_VOID)
            return true;

//        ItemRequirement requirement = ItemRequirement.of(state, te);
//        if (requirement.isEmpty())
//            return false;
//        if (requirement.isInvalid())
//            return false;

        // Block doesn't need to be placed twice (Doors, beds, double plants)
        if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)
            && state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER)
            return true;
        if (state.hasProperty(BlockStateProperties.BED_PART)
            && state.getValue(BlockStateProperties.BED_PART) == BedPart.HEAD)
            return true;
        if (state.getBlock() instanceof PistonHeadBlock)
            return true;
//        if (AllBlocks.BELT.has(state))
//            return state.getValue(BeltBlock.PART) == BeltPart.MIDDLE;

        return false;
    }
}
