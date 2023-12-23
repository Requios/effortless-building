package nl.requios.effortlessbuilding.systems;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import nl.requios.effortlessbuilding.ClientConfig;
import nl.requios.effortlessbuilding.ClientEvents;
import nl.requios.effortlessbuilding.EffortlessBuildingClient;
import nl.requios.effortlessbuilding.buildmode.BuildModeEnum;
import nl.requios.effortlessbuilding.capability.CapabilityHandler;
import nl.requios.effortlessbuilding.compatibility.CompatHelper;
import nl.requios.effortlessbuilding.item.AbstractRandomizerBagItem;
import nl.requios.effortlessbuilding.network.PacketHandler;
import nl.requios.effortlessbuilding.network.ServerBreakBlocksPacket;
import nl.requios.effortlessbuilding.network.ServerPlaceBlocksPacket;
import nl.requios.effortlessbuilding.utilities.*;

import java.util.HashSet;

// Receives block placed events, then finds additional blocks we want to place through various systems,
// and then sends them to the server to be placed
// Uses chain of responsibility pattern
@OnlyIn(Dist.CLIENT)
public class BuilderChain {

    private final BlockSet blocks = new BlockSet();
    private Item previousHeldItem;
    private int soundTime = 0;
    private BlockEntry startPosForPlacing;
    private BlockPos startPosForBreaking;
    private BlockHitResult lookingAtNear;
    //Can be near or far depending on abilities
    //Only updated when we are in IDLE state
    private BlockHitResult lookingAt;

    public enum BuildingState {
        IDLE,
        PLACING,
        BREAKING
    }

    //What we are currently doing
    private BuildingState buildingState = BuildingState.IDLE;

    public enum AbilitiesState {
        CAN_PLACE_AND_BREAK,
        CAN_BREAK,
        NONE
    }

    //Whether we can place or break blocks, determined by what we are looking at and what we are holding
    private AbilitiesState abilitiesState = AbilitiesState.CAN_PLACE_AND_BREAK;

    public void onRightClick() {

        if (abilitiesState != AbilitiesState.CAN_PLACE_AND_BREAK || buildingState == BuildingState.BREAKING) {
            cancel();
            return;
        }

        if (buildingState == BuildingState.IDLE) {
            buildingState = BuildingState.PLACING;
        }

        var player = Minecraft.getInstance().player;
        var buildMode = EffortlessBuildingClient.BUILD_MODES.getBuildMode();

        //Find out if we should place blocks now
        if (buildMode.instance.onClick(blocks)) {
            buildingState = BuildingState.IDLE;

            if (!blocks.isEmpty()) {
                EffortlessBuildingClient.BLOCK_PREVIEWS.onBlocksPlaced(blocks);
                ClientBlockUtilities.playSoundIfFurtherThanNormal(player, blocks.getLastBlockEntry(), false);
                player.swing(InteractionHand.MAIN_HAND);

                blocks.skipFirst = buildMode == BuildModeEnum.DISABLED;
                long placeTime = player.level().getGameTime();
                if (blocks.size() > 1) placeTime += ClientConfig.visuals.appearAnimationLength.get();
                PacketHandler.INSTANCE.sendToServer(new ServerPlaceBlocksPacket(blocks, placeTime));
            }
        }
    }

    public void onLeftClick() {

        if (abilitiesState == AbilitiesState.NONE || buildingState == BuildingState.PLACING) {
            cancel();
            return;
        }

        var player = Minecraft.getInstance().player;
        if (player == null) return;
        if (!CapabilityHandler.canBreakFar(player)) return;

        if (buildingState == BuildingState.IDLE){
            buildingState = BuildingState.BREAKING;

            //Use new start position for breaking, because we assumed the player was gonna place
            blocks.setStartPos(new BlockEntry(startPosForBreaking));
            EffortlessBuildingClient.BUILD_MODIFIERS.findCoordinates(blocks, player);
            EffortlessBuildingClient.BUILDER_FILTER.filterOnCoordinates(blocks, player);
            findExistingBlockStates(player.level());
            EffortlessBuildingClient.BUILDER_FILTER.filterOnExistingBlockStates(blocks, player);
        }

        var buildMode = EffortlessBuildingClient.BUILD_MODES.getBuildMode();

        //Find out if we should break blocks now
        if (buildMode.instance.onClick(blocks)) {
            buildingState = BuildingState.IDLE;

            if (!blocks.isEmpty()) {
                EffortlessBuildingClient.BLOCK_PREVIEWS.onBlocksBroken(blocks);
                ClientBlockUtilities.playSoundIfFurtherThanNormal(player, blocks.getLastBlockEntry(), true);
                player.swing(InteractionHand.MAIN_HAND);
                blocks.skipFirst = buildMode == BuildModeEnum.DISABLED;
                PacketHandler.INSTANCE.sendToServer(new ServerBreakBlocksPacket(blocks));
            }
        }
    }

    public void onTick() {

        var previousCoordinates = new HashSet<>(blocks.getCoordinates());
        blocks.clear();
        startPosForPlacing = null;
        startPosForBreaking = null;
        lookingAtNear = null;

        var mc = Minecraft.getInstance();
        var player = mc.player;
        var world = mc.level;

        abilitiesState = determineAbilities(mc, player, world);
        if (abilitiesState == AbilitiesState.NONE) return;

        var buildMode = EffortlessBuildingClient.BUILD_MODES.getBuildMode();

        if (buildingState == BuildingState.IDLE) {
            //Find start position
            BlockEntry startEntry = findStartPosition(player, buildMode);
            if (startEntry != null) {
                blocks.setStartPos(startEntry);
            } else {
                //We aren't placing or breaking blocks, and we have no start position
                abilitiesState = AbilitiesState.NONE;
                return;
            }
        }

        EffortlessBuildingClient.BUILD_MODES.findCoordinates(blocks, player);
        EffortlessBuildingClient.BUILD_MODIFIERS.findCoordinates(blocks, player);
        EffortlessBuildingClient.BUILDER_FILTER.filterOnCoordinates(blocks, player);

        if (buildMode == BuildModeEnum.DISABLED && blocks.size() <= 1) {
            abilitiesState = AbilitiesState.NONE;
            return;
        }

        findExistingBlockStates(world);
        EffortlessBuildingClient.BUILDER_FILTER.filterOnExistingBlockStates(blocks, player);

        var heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);
        findNewBlockStates(player, heldItem); //includes filtering on new blockstates

        //Check if any changes are made
        if (previousHeldItem != heldItem.getItem() || !previousCoordinates.equals(blocks.getCoordinates())) {
            onBlocksChanged(player);
        }

        previousHeldItem = heldItem.getItem();
    }

    //Whether we can place or break blocks, determined by what we are looking at and what we are holding
    private AbilitiesState determineAbilities(Minecraft mc, Player player, Level world) {

        var hitResult = Minecraft.getInstance().hitResult;
        if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
            lookingAtNear = (BlockHitResult) hitResult;
        }

        var itemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        boolean blockInHand = CompatHelper.isItemBlockProxy(itemStack);
        boolean lookingAtInteractiveObject = ClientBlockUtilities.determineIfLookingAtInteractiveObject(mc, world);
        boolean isShiftKeyDown = player.isShiftKeyDown();

        if (lookingAtInteractiveObject && !isShiftKeyDown && EffortlessBuildingClient.BUILD_SETTINGS.shouldProtectTileEntities())
            return AbilitiesState.NONE;

        if (!blockInHand)
            return AbilitiesState.CAN_BREAK;

        return AbilitiesState.CAN_PLACE_AND_BREAK;
    }

    private BlockEntry findStartPosition(Player player, BuildModeEnum buildMode) {

        int maxReach = CapabilityHandler.getPlacementReach(player, false);

        //Determine if we should look far or nearby
        boolean shouldLookAtNear = buildMode == BuildModeEnum.DISABLED || maxReach < 3;
        if (shouldLookAtNear) {
            lookingAt = lookingAtNear;
        } else {
            lookingAt = ClientBlockUtilities.getLookingAtFar(player);
        }
        if (lookingAt == null || lookingAt.getType() == HitResult.Type.MISS) return null;

        var startPos = lookingAt.getBlockPos();

        //Check if out of reach
        if (!shouldLookAtNear && player.blockPosition().distSqr(startPos) > maxReach * maxReach) return null;

        startPosForBreaking = startPos;

        if (abilitiesState == AbilitiesState.CAN_PLACE_AND_BREAK) {
            //Calculate start position for placing

            //Offset in direction of sidehit if not quickreplace and not replaceable
            boolean shouldOffsetStartPosition = EffortlessBuildingClient.BUILD_SETTINGS.shouldOffsetStartPosition();
            boolean replaceable = player.level().getBlockState(startPos).canBeReplaced();
            boolean becomesDoubleSlab = SurvivalHelper.doesBecomeDoubleSlab(player, startPos);
            if (!shouldOffsetStartPosition && !replaceable && !becomesDoubleSlab) {
                startPos = startPos.relative(lookingAt.getDirection());
            }

        } else {
            //We can only break

            //Do not break far if we are not allowed to
            if (!shouldLookAtNear && !CapabilityHandler.canBreakFar(player)) return null;
        }

        var blockEntry = new BlockEntry(startPos);
        startPosForPlacing = blockEntry;
        return blockEntry;
    }

    private void findExistingBlockStates(Level world) {
        for (BlockEntry blockEntry : blocks) {
            blockEntry.existingBlockState = world.getBlockState(blockEntry.blockPos);
        }
    }

    private void findNewBlockStates(Player player, ItemStack heldItem) {
        if (buildingState == BuildingState.BREAKING) return;

        var originalDirection = player.getDirection();
        var clickedFace = lookingAt.getDirection();
        Vec3 relativeHitVec = lookingAt.getLocation().subtract(Vec3.atLowerCornerOf(lookingAt.getBlockPos()));

        //Keep track of itemstack usage
        EffortlessBuildingClient.ITEM_USAGE_TRACKER.initialize();

        if (CompatHelper.isItemBlockProxy(heldItem, false)) {
            AbstractRandomizerBagItem.resetRandomness();
        }

        var iter = blocks.entrySet().iterator();
        while (iter.hasNext()) {
            var blockEntry = iter.next().getValue();

            //Determine itemstack
            ItemStack itemStack = determineItemStack(player, heldItem);
            if (itemStack == null || itemStack.isEmpty()) {
                iter.remove();
                continue;
            }

            //Find new blockstate
            blockEntry.setItemAndFindNewBlockState(itemStack, player.level(), originalDirection, clickedFace, relativeHitVec);

            //Filter on new blockstate
            if (EffortlessBuildingClient.BUILDER_FILTER.filterOnNewBlockState(blockEntry, player)) {
                iter.remove();
                continue;
            }

            //Increase itemstack usage if not filtered out
            //Mark invalid if the player does not have enough of that item
            blockEntry.invalid = !EffortlessBuildingClient.ITEM_USAGE_TRACKER.increaseUsageCount(itemStack.getItem(), 1, player);
        }

        EffortlessBuildingClient.ITEM_USAGE_TRACKER.calculateMissingItems(player);
    }

    private ItemStack determineItemStack(Player player, ItemStack heldItem) {
        if (heldItem.getItem() instanceof BlockItem) {
            return heldItem;
        }

        if (CompatHelper.isItemBlockProxy(heldItem, false)) {
            return CompatHelper.getItemBlockFromStack(heldItem);
        }

        return null;
    }

    private void onBlocksChanged(Player player) {

        //Renew randomness of randomizer bag
        AbstractRandomizerBagItem.renewRandomness();

        //Play sound (max once every tick)
        if (blocks.size() > 1 && soundTime < ClientEvents.ticksInGame) {
            soundTime = ClientEvents.ticksInGame;

            if (blocks.getLastBlockEntry() != null && blocks.getLastBlockEntry().newBlockState != null) {
                var lastBlockState = blocks.getLastBlockEntry().newBlockState;
                SoundType soundType = lastBlockState.getBlock().getSoundType(lastBlockState, player.level(), blocks.lastPos, player);
                SoundEvent soundEvent = buildingState == BuildingState.BREAKING ? soundType.getBreakSound() : soundType.getPlaceSound();
                player.level().playSound(player, player.blockPosition(), soundEvent, SoundSource.BLOCKS, 0.3f, 0.8f);
            }
        }
    }

    public void cancel() {
        if (buildingState == BuildingState.IDLE) return;
        buildingState = BuildingState.IDLE;
        EffortlessBuildingClient.BUILD_MODES.onCancel();
        Minecraft.getInstance().player.playSound(SoundEvents.UI_TOAST_OUT, 4, 1);
    }

    public BlockSet getBlocks() {
        return blocks;
    }

    public BuildingState getBuildingState() {
        return buildingState;
    }

    public AbilitiesState getAbilitiesState() {
        return abilitiesState;
    }

    public BuildingState getPretendBuildingState() {
        if (buildingState != BuildingState.IDLE) return buildingState;
        if (abilitiesState == AbilitiesState.CAN_PLACE_AND_BREAK) return BuildingState.PLACING;
        if (abilitiesState == AbilitiesState.CAN_BREAK) return BuildingState.BREAKING;
        return BuildingState.IDLE;
    }

    public BlockEntry getStartPosForPlacing() {
        return startPosForPlacing;
    }

    public BlockPos getStartPosForBreaking() {
        return startPosForBreaking;
    }

    public BlockEntry getStartPos() {
        if (getPretendBuildingState() == BuildingState.BREAKING) return new BlockEntry(getStartPosForBreaking());
        return getStartPosForPlacing();
    }

    public BlockHitResult getLookingAtNear() {
        return lookingAtNear;
    }
}
