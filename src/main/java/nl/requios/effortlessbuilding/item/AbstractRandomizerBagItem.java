package nl.requios.effortlessbuilding.item;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.network.NetworkHooks;
import nl.requios.effortlessbuilding.systems.ServerBuildState;
import nl.requios.effortlessbuilding.utilities.BlockEntry;
import nl.requios.effortlessbuilding.utilities.BlockPlacerHelper;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class AbstractRandomizerBagItem extends Item {

    private static long currentSeed = 1337;
    private static final Random rand = new Random(currentSeed);

    public AbstractRandomizerBagItem() {
        super(new Item.Properties().stacksTo(1));
    }

    public abstract int getInventorySize();

    public abstract MenuProvider getContainerProvider(ItemStack item);

    /**
     * Get the inventory of a randomizer bag by checking the capability.
     */
    public IItemHandler getBagInventory(ItemStack bag) {
        return bag.getCapability(ForgeCapabilities.ITEM_HANDLER, null).orElse(null);
    }

    /**
     * Pick a random slot from the bag. Empty slots will never get chosen.
     */
    public ItemStack pickRandomStack(IItemHandler bagInventory) {
        //Find how many stacks are non-empty, and save them in a list
        int nonempty = 0;
        List<ItemStack> nonEmptyStacks = new ArrayList<>();
        List<Integer> originalSlots = new ArrayList<>(getInventorySize());
        for (int i = 0; i < bagInventory.getSlots(); i++) {
            ItemStack stack = bagInventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                nonempty++;
                nonEmptyStacks.add(stack);
                originalSlots.add(i);
            }
        }

        if (nonEmptyStacks.size() != originalSlots.size())
            throw new Error("NonEmptyStacks and OriginalSlots not same size");

        if (nonempty == 0) return ItemStack.EMPTY;

        //Pick random slot
        int randomSlot = rand.nextInt(nonempty);
        if (randomSlot < 0 || randomSlot > bagInventory.getSlots()) return ItemStack.EMPTY;

        int originalSlot = originalSlots.get(randomSlot);
        if (originalSlot < 0 || originalSlot > bagInventory.getSlots()) return ItemStack.EMPTY;

        return bagInventory.getStackInSlot(originalSlot);
    }

    public ItemStack findStack(IItemHandler bagInventory, Item item) {
        for (int i = 0; i < bagInventory.getSlots(); i++) {
            ItemStack stack = bagInventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() == item) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    public static void resetRandomness() {
        rand.setSeed(currentSeed);
    }

    public static void renewRandomness() {
        currentSeed = Calendar.getInstance().getTimeInMillis();
        rand.setSeed(currentSeed);
    }

    /**
     * Consume a specified amount of an item from the bag inventory.
     */
    protected void consumeFromBag(IItemHandler bagInventory, Item item, int amount) {
        int remaining = amount;
        for (int i = 0; i < bagInventory.getSlots() && remaining > 0; i++) {
            ItemStack stack = bagInventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() == item) {
                int taken = Math.min(remaining, stack.getCount());
                bagInventory.extractItem(i, taken, false);
                remaining -= taken;
            }
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Player player = ctx.getPlayer();
        Level world = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        Direction facing = ctx.getClickedFace();
        ItemStack item = ctx.getItemInHand();
        Vec3 hitVec = ctx.getClickLocation();

        if (player == null) return InteractionResult.FAIL;

        if (ctx.getPlayer() != null && ctx.getPlayer().isShiftKeyDown()) { //ctx.isPlacerSneaking()
            if (world.isClientSide) return InteractionResult.SUCCESS;
            //Open inventory
            NetworkHooks.openScreen((ServerPlayer) player, getContainerProvider(item));
        } else {
            if (world.isClientSide) return InteractionResult.SUCCESS;

            //---Only place manually if in normal vanilla mode---
            if (!ServerBuildState.isLikeVanilla(player)) {
                return InteractionResult.FAIL;
            }

            //Use item
            //Get bag inventory
            ItemStack bag = ctx.getItemInHand();
            IItemHandler bagInventory = getBagInventory(bag);
            if (bagInventory == null)
                return InteractionResult.FAIL;

            ItemStack toPlace = pickRandomStack(bagInventory);
            if (toPlace.isEmpty()) return InteractionResult.FAIL;

            //Check if bag has enough items
            if (!player.isCreative() && toPlace.getCount() <= 0) return InteractionResult.FAIL;

            if (!world.getBlockState(pos).getBlock().canBeReplaced(world.getBlockState(pos), Fluids.EMPTY)) {
                pos = pos.relative(facing);
            }

            BlockPlaceContext blockItemUseContext = new BlockPlaceContext(new UseOnContext(player, ctx.getHand(), new BlockHitResult(hitVec, facing, pos, false)));
            BlockState blockState = Block.byItem(toPlace.getItem()).getStateForPlacement(blockItemUseContext);

            var blockEntry = new BlockEntry(pos, blockState, toPlace.getItem());
            
            //Place block directly and consume from bag instead of player inventory
            if (BlockPlacerHelper.placeBlock(player, blockEntry)) {
                if (!player.isCreative()) {
                    consumeFromBag(bagInventory, toPlace.getItem(), 1);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack bag = player.getItemInHand(hand);

        if (player.isShiftKeyDown()) {
            if (world.isClientSide) return new InteractionResultHolder<>(InteractionResult.SUCCESS, bag);
            //Open inventory
            NetworkHooks.openScreen((ServerPlayer) player, getContainerProvider(bag));
        } else {
            //Use item
            //Get bag inventory
            IItemHandler bagInventory = getBagInventory(bag);
            if (bagInventory == null)
                return new InteractionResultHolder<>(InteractionResult.FAIL, bag);

            ItemStack toUse = pickRandomStack(bagInventory);
            if (toUse.isEmpty()) return new InteractionResultHolder<>(InteractionResult.FAIL, bag);

            return toUse.use(world, player, hand);
        }
        return new InteractionResultHolder<>(InteractionResult.PASS, bag);
    }

    @Override
    public int getUseDuration(ItemStack p_77626_1_) {
        return 1;
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ItemHandlerCapabilityProvider(getInventorySize(), stack);
    }

    @Nullable
    @Override
    public CompoundTag getShareTag(ItemStack stack) {
        CompoundTag baseTag = stack.getTag();
        CompoundTag capTag = serializeBagInventory(stack);
        CompoundTag combined = baseTag != null ? baseTag.copy() : new CompoundTag();
        if (capTag != null) {
            combined.put("BagInventory", capTag);
        }
        return combined;
    }

    @Override
    public void readShareTag(ItemStack stack, @Nullable CompoundTag nbt) {
        if (nbt != null && nbt.contains("BagInventory")) {
            CompoundTag capTag = nbt.getCompound("BagInventory");
            nbt.remove("BagInventory");
            stack.setTag(nbt.isEmpty() ? null : nbt);
            // Apply capability data
            IItemHandler handler = getBagInventory(stack);
            if (handler instanceof net.minecraftforge.items.ItemStackHandler ish) {
                ish.deserializeNBT(capTag);
            }
        } else {
            super.readShareTag(stack, nbt);
        }
    }

    @Nullable
    private CompoundTag serializeBagInventory(ItemStack stack) {
        IItemHandler handler = getBagInventory(stack);
        if (handler instanceof net.minecraftforge.items.ItemStackHandler ish) {
            return ish.serializeNBT();
        }
        return null;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal(ChatFormatting.BLUE + "Rightclick" + ChatFormatting.GRAY + " to place a random block"));
        tooltip.add(Component.literal(ChatFormatting.BLUE + "Sneak + rightclick" + ChatFormatting.GRAY + " to open inventory"));
    }
}
