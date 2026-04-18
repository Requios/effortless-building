package nl.requios.effortlessbuilding.utilities;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.item.AbstractRandomizerBagItem;

import java.util.Map;

public class InventoryHelper {

	@Deprecated //Use BlockHelper.findAndRemoveInInventory instead
	public static ItemStack findItemStackInInventory(Player player, Block block) {
		for (ItemStack invStack : player.getInventory().items) {
			if (!invStack.isEmpty() && invStack.getItem() instanceof BlockItem &&
				((BlockItem) invStack.getItem()).getBlock().equals(block)) {
				return invStack;
			}
		}
		return ItemStack.EMPTY;
	}

	public static int findTotalBlocksInInventory(Player player, Block block) {
		int total = 0;
		for (ItemStack invStack : player.getInventory().items) {
			if (!invStack.isEmpty() && invStack.getItem() instanceof BlockItem &&
				((BlockItem) invStack.getItem()).getBlock().equals(block)) {
				total += invStack.getCount();
			}
		}
		return total;
	}

	public static int findTotalItemsInInventory(Player player, Item item) {
		// If holding a bag, only count items inside that bag
		ItemStack heldItem = player.getMainHandItem();
		if (!heldItem.isEmpty() && heldItem.getItem() instanceof AbstractRandomizerBagItem) {
			return countItemInBag(heldItem, item);
		}

		int total = 0;
		for (ItemStack invStack : player.getInventory().items) {
			if (!invStack.isEmpty() && invStack.getItem().equals(item)) {
				total += invStack.getCount();
			}
		}
		return total;
	}

	public static void removeFromInventory(Player player, Map<Item, Integer> items) {
		for (Item item : items.keySet()) {
			int count = items.get(item);
			removeFromInventory(player, item, count);
		}
	}

	public static void removeFromInventory(Player player, Item item, int amount) {
		if (player.isCreative()) return;

		// If holding a bag, only consume from that bag
		ItemStack heldItem = player.getMainHandItem();
		if (!heldItem.isEmpty() && heldItem.getItem() instanceof AbstractRandomizerBagItem) {
			int removed = removeFromBag(heldItem, item, amount);
			if (removed != amount) {
				EffortlessBuilding.logError(player.getDisplayName().getString() + " tried to remove " + amount + " " + item + " from bag but only removed " + removed);
			}
			return;
		}

		//From BlockHelper.findAndRemoveInInventory
		int amountFound = 0;

		{
			// Try held Item first
			int preferredSlot = player.getInventory().selected;
			ItemStack itemstack = player.getInventory()
					.getItem(preferredSlot);
			int count = itemstack.getCount();
			if (itemstack.getItem() == item && count > 0) {
				int taken = Math.min(count, amount - amountFound);
				player.getInventory()
						.setItem(preferredSlot, new ItemStack(itemstack.getItem(), count - taken));
				amountFound += taken;
			}
		}

		// Search inventory
		for (int i = 0; i < player.getInventory()
				.getContainerSize(); ++i) {
			if (amountFound == amount)
				break;

			ItemStack itemstack = player.getInventory()
					.getItem(i);
			int count = itemstack.getCount();
			if (itemstack.getItem() == item && count > 0) {
				int taken = Math.min(count, amount - amountFound);
				player.getInventory()
						.setItem(i, new ItemStack(itemstack.getItem(), count - taken));
				amountFound += taken;
			}
		}

		if (amountFound != amount) {
			EffortlessBuilding.logError(player.getDisplayName().getString() + " tried to remove " + amount + " " + item + " from inventory but only removed " + amountFound);
		}
	}

	private static int countItemInBag(ItemStack bag, Item item) {
		IItemHandler handler = bag.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
		if (handler == null) return 0;
		int total = 0;
		for (int i = 0; i < handler.getSlots(); i++) {
			ItemStack stack = handler.getStackInSlot(i);
			if (!stack.isEmpty() && stack.getItem() == item) {
				total += stack.getCount();
			}
		}
		return total;
	}

	private static int removeFromBag(ItemStack bag, Item item, int amount) {
		IItemHandler handler = bag.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
		if (handler == null) return 0;
		int removed = 0;
		for (int i = 0; i < handler.getSlots() && removed < amount; i++) {
			ItemStack stack = handler.getStackInSlot(i);
			if (!stack.isEmpty() && stack.getItem() == item) {
				int taken = Math.min(amount - removed, stack.getCount());
				handler.extractItem(i, taken, false);
				removed += taken;
			}
		}
		return removed;
	}
}
