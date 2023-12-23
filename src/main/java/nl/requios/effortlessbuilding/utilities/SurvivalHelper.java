package nl.requios.effortlessbuilding.utilities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.ForgeEventFactory;
import nl.requios.effortlessbuilding.compatibility.CompatHelper;

public class SurvivalHelper {

	//Can break using held tool? (or in creative)
	public static boolean canBreak(Level world, Player player, BlockPos pos) {

		BlockState blockState = world.getBlockState(pos);
		if (!world.getFluidState(pos).isEmpty()) return false;

		if (player.isCreative()) return true;

		return ForgeEventFactory.doPlayerHarvestCheck(player, blockState, true);
	}

	public static boolean doesBecomeDoubleSlab(Player player, BlockPos pos) {

		BlockState placedBlockState = player.level.getBlockState(pos);

		ItemStack itemstack = player.getItemInHand(InteractionHand.MAIN_HAND);
		if (CompatHelper.isItemBlockProxy(itemstack))
			itemstack = CompatHelper.getItemBlockFromStack(itemstack);

		if (itemstack.isEmpty() || !(itemstack.getItem() instanceof BlockItem) || !(((BlockItem) itemstack.getItem()).getBlock() instanceof SlabBlock))
			return false;
		SlabBlock heldSlab = (SlabBlock) ((BlockItem) itemstack.getItem()).getBlock();

		if (placedBlockState.getBlock() == heldSlab) {
			//TODO 1.13
//            IProperty<?> variantProperty = heldSlab.getVariantProperty();
//            Comparable<?> placedVariant = placedBlockState.getValue(variantProperty);
//            BlockSlab.EnumBlockHalf placedHalf = placedBlockState.getValue(BlockSlab.HALF);
//
//            Comparable<?> heldVariant = heldSlab.getTypeForItem(itemstack);
//
//            if ((facing == EnumFacing.UP && placedHalf == BlockSlab.EnumBlockHalf.BOTTOM || facing == EnumFacing.DOWN && placedHalf == BlockSlab.EnumBlockHalf.TOP) && placedVariant == heldVariant)
//            {
//                return true;
//            }
		}
		return false;
	}
}
