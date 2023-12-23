package nl.requios.effortlessbuilding.systems;

import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.player.Player;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.ServerConfig;
import nl.requios.effortlessbuilding.utilities.*;

import java.util.*;

// Receives block placement requests from the client and places them
public class ServerBlockPlacer {
    private boolean isPlacingOrBreakingBlocks = false;

//region Delays
    private final Set<DelayedEntry> delayedEntries = Collections.synchronizedSet(new HashSet<>());
    private final Set<DelayedEntry> delayedEntriesView = Collections.unmodifiableSet(delayedEntries);

    public void placeBlocksDelayed(Player player, BlockSet blocks, long placeTime) {

        if (!checkAndNotifyAllowedToUseMod(player)) return;
        if (!validateBlockSet(player, blocks)) return;

        delayedEntries.add(new DelayedEntry(player, blocks, placeTime));
    }
    
    public void tick() {

        //Iterator to prevent concurrent modification exception
        for (var iterator = delayedEntries.iterator(); iterator.hasNext(); ) {
            DelayedEntry entry = iterator.next();
            long gameTime = entry.player.level.getGameTime();
            if (gameTime >= entry.placeTime) {
                applyBlockSet(entry.player, entry.blocks);
                iterator.remove();
            }
        }
    }

    public Set<DelayedEntry> getDelayedEntries() {
        return delayedEntriesView;
    }

    public record DelayedEntry(Player player, BlockSet blocks, long placeTime) {}
//endregion

    public void breakBlocks(Player player, BlockSet blocks) {
        applyBlockSet(player, blocks);
    }

    public void applyBlockSet(Player player, BlockSet blocks) {

        if (!checkAndNotifyAllowedToUseMod(player)) return;
        if (!validateBlockSet(player, blocks)) return;

        EffortlessBuilding.ITEM_USAGE_TRACKER.initialize();
        var undoSet = new BlockSet();
        for (BlockEntry block : blocks) {
            if (blocks.skipFirst && block.blockPos == blocks.firstPos) continue;

            if (applyBlockEntry(player, block)) {
                undoSet.add(block);
            }
        }

        //Remove items from inventory
        //(Adding items is done during BlockPlacerHelper.breakBlock)
        EffortlessBuilding.ITEM_USAGE_TRACKER.calculateMissingItems(player);
        if (!player.isCreative()) {
            InventoryHelper.removeFromInventory(player, EffortlessBuilding.ITEM_USAGE_TRACKER.placed);
        }

        EffortlessBuilding.UNDO_REDO.addUndo(player, undoSet);
    }

    public void undoBlockSet(Player player, BlockSet blocks) {

        if (!EffortlessBuilding.UNDO_REDO.isAllowedToUndo(player)) return;

        EffortlessBuilding.ITEM_USAGE_TRACKER.initialize();
        var redoSet = new BlockSet();
        for (BlockEntry block : blocks) {
            if (blocks.skipFirst && block.blockPos == blocks.firstPos) continue;

            if (undoBlockEntry(player, block)) {
                redoSet.add(block);
            }
        }

        //Remove items from inventory
        //(Adding items is done during BlockPlacerHelper.breakBlock)
        EffortlessBuilding.ITEM_USAGE_TRACKER.calculateMissingItems(player);
        if (!player.isCreative()) {
            InventoryHelper.removeFromInventory(player, EffortlessBuilding.ITEM_USAGE_TRACKER.placed);
        }

        EffortlessBuilding.UNDO_REDO.addRedo(player, redoSet);
    }

    private boolean applyBlockEntry(Player player, BlockEntry block) {

        block.existingBlockState = player.level.getBlockState(block.blockPos);
        boolean breaking = BlockUtilities.isNullOrAir(block.newBlockState);
        if (!validateBlockEntry(player, block, breaking)) return false;

        boolean success;
        isPlacingOrBreakingBlocks = true;
        if (breaking) {
            success = BlockPlacerHelper.breakBlock(player, block);
        } else {
            //If we have the item in our inventory, place it
            if (EffortlessBuilding.ITEM_USAGE_TRACKER.increaseUsageCount(block.item, 1, player)) {
                success = BlockPlacerHelper.placeBlock(player, block);
            } else {
                success = false;
                //Not having the item at this point would be a bit weird, so we notify the player
                //It could mean the client/server are out of sync, or the inventory changed during the short delay period
                EffortlessBuilding.log(player, ChatFormatting.RED + block.item.toString() + " not found in inventory.");
            }
        }
        isPlacingOrBreakingBlocks = false;
        return success;
    }

    private boolean undoBlockEntry(Player player, BlockEntry block) {

        boolean breaking = BlockUtilities.isNullOrAir(block.existingBlockState);

        var tempBlockEntry = new BlockEntry(block.blockPos);
        var temp = block.existingBlockState;
        tempBlockEntry.existingBlockState = block.newBlockState;
        tempBlockEntry.newBlockState = temp;

        if (!validateBlockEntry(player, tempBlockEntry, breaking)) return false;

        //Update newBlockState for future redo's
        block.newBlockState = player.level.getBlockState(block.blockPos);

        boolean success;
        isPlacingOrBreakingBlocks = true;
        if (breaking) {
            success = BlockPlacerHelper.breakBlock(player, tempBlockEntry);
        } else {
            //If we have the item in our inventory, place it
            if (EffortlessBuilding.ITEM_USAGE_TRACKER.increaseUsageCount(tempBlockEntry.item, 1, player)) {
                success = BlockPlacerHelper.placeBlock(player, tempBlockEntry);
            } else {
                success = false;
                //Not having the item at this point would be a bit weird, so we notify the player
                EffortlessBuilding.log(player, ChatFormatting.RED + tempBlockEntry.item.toString() + " not found in inventory.");
            }
        }
        isPlacingOrBreakingBlocks = false;

        return success;
    }

    private boolean checkAndNotifyAllowedToUseMod(Player player) {

        if (!player.getAbilities().mayBuild) {
            EffortlessBuilding.log(player, ChatFormatting.RED + "You are not allowed to build.");
            return false;
        }

        if (!isAllowedToUseMod(player)) {
            EffortlessBuilding.log(player, ChatFormatting.RED + "You are not allowed to use Effortless Building.");
            return false;
        }
        return true;
    }

    private boolean isAllowedToUseMod(Player player) {

        if (!ServerConfig.validation.allowInSurvival.get() && !player.isCreative()) return false;

        if (ServerConfig.validation.useWhitelist.get()) {
            return ServerConfig.validation.whitelist.get().contains(player.getGameProfile().getName());
        }

        return true;
    }

    private boolean validateBlockSet(Player player, BlockSet blocks) {

        if (blocks.isEmpty()) {
            EffortlessBuilding.log(player, ChatFormatting.RED + "No blocks to place.");
            return false;
        }
        if (blocks.skipFirst && blocks.size() == 1 && blocks.iterator().next().blockPos == blocks.firstPos) {
            EffortlessBuilding.log(player, ChatFormatting.RED + "No blocks to place because the first block was skipped.");
            return false;
        }
        if (blocks.size() > ServerConfig.validation.maxBlocksPlacedAtOnce.get()) {
            EffortlessBuilding.log(player, ChatFormatting.RED + "Too many blocks to place. Max: " + ServerConfig.validation.maxBlocksPlacedAtOnce.get());
            return false;
        }

        //Dont allow mixing breaking and placing blocks
        if (isMixedPlacingAndBreaking(player, blocks)) {
            EffortlessBuilding.log(player, ChatFormatting.RED + "Cannot mix breaking and placing blocks.");
            return false;
        }

        return true;
    }

    private boolean isMixedPlacingAndBreaking(Player player, BlockSet blocks) {

        //First determine if we are breaking or placing
        var iterator = blocks.iterator();

        //Get any block from the set, skip first if we have to
        var anyBlock = iterator.next();
        if (blocks.skipFirst && anyBlock.blockPos == blocks.firstPos) {
            anyBlock = iterator.next();
        }

        boolean breaking = anyBlock.newBlockState == null || anyBlock.newBlockState.isAir();

        while (iterator.hasNext()) {
            var block = iterator.next();
            if (block.newBlockState == null || block.newBlockState.isAir()) {
                if (!breaking) return true;
            } else {
                if (breaking) return true;
            }
        }

        return false;
    }

    private boolean validateBlockEntry(Player player, BlockEntry block, boolean breaking) {

        if (!player.level.isLoaded(block.blockPos)) return false;

        if (breaking && BlockUtilities.isNullOrAir(block.existingBlockState)) return false;



        return true;
    }
    
    public boolean isPlacingOrBreakingBlocks() {
        return isPlacingOrBreakingBlocks;
    }
}
