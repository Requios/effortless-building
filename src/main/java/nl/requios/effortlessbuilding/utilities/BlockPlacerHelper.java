package nl.requios.effortlessbuilding.utilities;

import com.google.common.collect.Lists;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.items.ItemHandlerHelper;
import nl.requios.effortlessbuilding.create.foundation.utility.BlockHelper;

import java.util.List;

//Server only
public class BlockPlacerHelper {

    public static boolean breakBlock(Player player, BlockEntry blockEntry) {

        ItemStack usedTool = player.getMainHandItem();
        if (usedTool.isEmpty() || !(usedTool.getItem() instanceof DiggerItem)) {
            ItemStack offhand = player.getOffhandItem();
            if (!offhand.isEmpty() && offhand.getItem() instanceof DiggerItem) {
                usedTool = offhand;
            }
        }

        boolean brokeBlock = BlockHelper.destroyBlockAs(player.level, blockEntry.blockPos, player, usedTool, 0f, stack -> {
            if (!player.isCreative()) {
                ItemHandlerHelper.giveItemToPlayer(player, stack);
            }
        });
        return brokeBlock;
    }

    //ForgeHooks::onPlaceItemIntoWorld, removed itemstack usage
    public static boolean placeBlock(Player player, BlockEntry blockEntry) {

        Level level = player.level;
        var itemStack = new ItemStack(blockEntry.item);

        level.captureBlockSnapshots = true;
        BlockHelper.placeSchematicBlock(level, player, blockEntry.newBlockState, blockEntry.blockPos, itemStack, null);
        level.captureBlockSnapshots = false;

        //Find out if we get to keep the placed block by sending a forge event
        @SuppressWarnings("unchecked")
        List<BlockSnapshot> blockSnapshots = (List<BlockSnapshot>)level.capturedBlockSnapshots.clone();
        level.capturedBlockSnapshots.clear();
        Direction side = Direction.UP;

        boolean eventResult = false;
        if (blockSnapshots.size() > 1)
        {
            eventResult = ForgeEventFactory.onMultiBlockPlace(player, blockSnapshots, side);
        }
        else if (blockSnapshots.size() == 1)
        {
            eventResult = ForgeEventFactory.onBlockPlace(player, blockSnapshots.get(0), side);
        }

        if (eventResult)
        {
            // revert back all captured blocks
            for (BlockSnapshot blocksnapshot : Lists.reverse(blockSnapshots))
            {
                level.restoringBlockSnapshots = true;
                blocksnapshot.restore(true, false);
                level.restoringBlockSnapshots = false;
            }
        }
        else
        {
            for (BlockSnapshot snap : blockSnapshots)
            {
                int updateFlag = snap.getFlag();
                BlockState oldBlock = snap.getReplacedBlock();
                BlockState newBlock = level.getBlockState(snap.getPos());
                newBlock.onPlace(level, snap.getPos(), oldBlock, false);

                level.markAndNotifyBlock(snap.getPos(), level.getChunkAt(snap.getPos()), oldBlock, newBlock, updateFlag, 512);
            }
        }
        level.capturedBlockSnapshots.clear();
        return !eventResult;
    }
}
