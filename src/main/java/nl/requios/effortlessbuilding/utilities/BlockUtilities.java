package nl.requios.effortlessbuilding.utilities;

import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

//Common
public class BlockUtilities {

    public static boolean isNullOrAir(BlockState blockState) {
        return blockState == null || blockState.isAir();
    }

    @Deprecated //Use BlockEntry.setItemStackAndFindNewBlockState instead
    public static BlockState getBlockState(Player player, InteractionHand hand, ItemStack blockItemStack, BlockEntry blockEntry, Vec3 relativeHitVec, Direction sideHit) {
        Block block = Block.byItem(blockItemStack.getItem());
        Vec3 hitVec = relativeHitVec.add(Vec3.atLowerCornerOf(blockEntry.blockPos));
        var blockHitResult = new BlockHitResult(hitVec, sideHit, blockEntry.blockPos, false);
        return block.getStateForPlacement(new BlockPlaceContext(player, hand, blockItemStack, blockHitResult));
    }


    public static BlockState getVerticalMirror(BlockState blockState) {
        //Stairs
        if (blockState.getBlock() instanceof StairBlock) {
            if (blockState.getValue(StairBlock.HALF) == Half.BOTTOM) {
                return blockState.setValue(StairBlock.HALF, Half.TOP);
            } else {
                return blockState.setValue(StairBlock.HALF, Half.BOTTOM);
            }
        }

        //Slabs
        if (blockState.getBlock() instanceof SlabBlock) {
            if (blockState.getValue(SlabBlock.TYPE) == SlabType.DOUBLE) {
                return blockState;
            } else if (blockState.getValue(SlabBlock.TYPE) == SlabType.BOTTOM) {
                return blockState.setValue(SlabBlock.TYPE, SlabType.TOP);
            } else {
                return blockState.setValue(SlabBlock.TYPE, SlabType.BOTTOM);
            }
        }

        //Buttons, endrod, observer, piston
        if (blockState.getBlock() instanceof DirectionalBlock) {
            if (blockState.getValue(DirectionalBlock.FACING) == Direction.DOWN) {
                return blockState.setValue(DirectionalBlock.FACING, Direction.UP);
            } else if (blockState.getValue(DirectionalBlock.FACING) == Direction.UP) {
                return blockState.setValue(DirectionalBlock.FACING, Direction.DOWN);
            }
        }

        //Dispenser, dropper
        if (blockState.getBlock() instanceof DispenserBlock) {
            if (blockState.getValue(DispenserBlock.FACING) == Direction.DOWN) {
                return blockState.setValue(DispenserBlock.FACING, Direction.UP);
            } else if (blockState.getValue(DispenserBlock.FACING) == Direction.UP) {
                return blockState.setValue(DispenserBlock.FACING, Direction.DOWN);
            }
        }

        return blockState;
    }

}
