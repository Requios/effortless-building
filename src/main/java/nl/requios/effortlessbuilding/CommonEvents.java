package nl.requios.effortlessbuilding;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.common.Mod.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import nl.requios.effortlessbuilding.attachment.PowerLevel;
import nl.requios.effortlessbuilding.compatibility.CompatHelper;
import nl.requios.effortlessbuilding.network.message.ModifierSettingsPacket;
import nl.requios.effortlessbuilding.network.message.PowerLevelPacket;
import nl.requios.effortlessbuilding.systems.ServerBuildState;
import nl.requios.effortlessbuilding.utilities.PowerLevelCommand;

@EventBusSubscriber
public class CommonEvents {

	//Mod Bus Events
//	@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
	public static class ModBusEvents {


	}

	@SubscribeEvent
	public static void registerCommands(RegisterCommandsEvent event) {
		PowerLevelCommand.register(event.getDispatcher());
	}

	@SubscribeEvent
	public static void onTick(TickEvent.LevelTickEvent event) {
		if (event.phase != TickEvent.Phase.START) return;
		if (event.side == LogicalSide.CLIENT) return;
		if (event.level.dimension() != Level.OVERWORLD) return;

		EffortlessBuilding.SERVER_BLOCK_PLACER.tick();
	}

	//Cancel event if necessary. Nothing more, rest is handled on mouseclick
	@SubscribeEvent
	public static void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
		if (event.getLevel().isClientSide()) return; //Never called clientside anyway, but just to be sure
		if (!(event.getEntity() instanceof Player player)) return;
		if (event.getEntity() instanceof FakePlayer) return;

		//Don't cancel event if our custom logic is breaking blocks
		if (EffortlessBuilding.SERVER_BLOCK_PLACER.isPlacingOrBreakingBlocks()) return;

		if (!ServerBuildState.isLikeVanilla(player)) {

			//Only cancel if itemblock in hand
			//Fixed issue with e.g. Create Wrench shift-rightclick disassembling being cancelled.
			if (isPlayerHoldingBlock(player)) {
				event.setCanceled(true);
				//TODO Notify client to not decrease itemstack
			}
		}
	}

	//Cancel event if necessary. Nothing more, rest is handled on mouseclick
	@SubscribeEvent
	public static void onBlockBroken(BlockEvent.BreakEvent event) {
		if (event.getLevel().isClientSide()) return;
		Player player = event.getPlayer();
		if (player instanceof FakePlayer) return;

		//Don't cancel event if our custom logic is breaking blocks
		if (EffortlessBuilding.SERVER_BLOCK_PLACER.isPlacingOrBreakingBlocks()) return;

		PowerLevel powerLevel = player.getData(EffortlessBuilding.POWER_LEVEL);
		if (!ServerBuildState.isLikeVanilla(player) && powerLevel.canBreakFar(player)) {
			event.setCanceled(true);
		}
	}

	private static boolean isPlayerHoldingBlock(Player player) {
		ItemStack currentItemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
		return currentItemStack.getItem() instanceof BlockItem ||
				(CompatHelper.isItemBlockProxy(currentItemStack) && !player.isShiftKeyDown());
	}

	@SubscribeEvent
	public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof FakePlayer) return;
		Player player = event.getEntity();
		if (player.getCommandSenderWorld().isClientSide) return;

		ServerBuildState.handleNewPlayer(player);

		((ServerPlayer)player).connection.send(new ModifierSettingsPacket(player));

		PowerLevel powerLevel = player.getData(EffortlessBuilding.POWER_LEVEL);
		((ServerPlayer)player).connection.send(new PowerLevelPacket(powerLevel.getPowerLevel()));
	}

	@SubscribeEvent
	public static void onClone(PlayerEvent.Clone event) {
		// If not dead, player is returning from the End
		if (!event.isWasDeath()) return;

		Player original = event.getOriginal();
		Player clone = event.getEntity();

		// Copy the power level from the original player to the clone
		if (event.isWasDeath() && event.getOriginal().hasData(EffortlessBuilding.POWER_LEVEL)) {
			event.getEntity().setData(EffortlessBuilding.POWER_LEVEL, event.getOriginal().getData(EffortlessBuilding.POWER_LEVEL));
		}
	}

	@SubscribeEvent
	public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
		if (event.getEntity() instanceof FakePlayer) return;
		Player player = event.getEntity();
		if (player.getCommandSenderWorld().isClientSide) {
			EffortlessBuilding.log("PlayerLoggedOutEvent triggers on client side");
			return;
		}

		EffortlessBuilding.UNDO_REDO.clear(player);
	}

	@SubscribeEvent
	public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
		if (event.getEntity() instanceof FakePlayer) return;
		Player player = event.getEntity();
		if (player.getCommandSenderWorld().isClientSide) {
			EffortlessBuilding.log("PlayerRespawnEvent triggers on client side");
			return;
		}

		//TODO check if this is needed
		ServerBuildState.handleNewPlayer(player);
	}

	@SubscribeEvent
	public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
		if (event.getEntity() instanceof FakePlayer) return;
		Player player = event.getEntity();
		if (player.getCommandSenderWorld().isClientSide) {
			EffortlessBuilding.log("PlayerChangedDimensionEvent triggers on client side");
			return;
		}

		//Undo redo has no dimension data, so clear it
		EffortlessBuilding.UNDO_REDO.clear(player);

		//TODO disable build mode and modifiers?
	}
}
