package nl.requios.effortlessbuilding;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.network.PacketDistributor;
import nl.requios.effortlessbuilding.capability.CapabilityHandler;
import nl.requios.effortlessbuilding.capability.IPowerLevel;
import nl.requios.effortlessbuilding.capability.PowerLevelCapability;
import nl.requios.effortlessbuilding.compatibility.CompatHelper;
import nl.requios.effortlessbuilding.network.ModifierSettingsPacket;
import nl.requios.effortlessbuilding.network.PacketHandler;
import nl.requios.effortlessbuilding.network.PowerLevelPacket;
import nl.requios.effortlessbuilding.systems.ServerBuildState;
import nl.requios.effortlessbuilding.utilities.PowerLevelCommand;

@EventBusSubscriber
public class CommonEvents {

	//Mod Bus Events
	@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
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

		IPowerLevel powerLevel = player.getCapability(CapabilityHandler.POWER_LEVEL_CAPABILITY).orElse(null);
		if (powerLevel == null) return; //Should never be null but just to be sure

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

		PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new ModifierSettingsPacket(player));

		IPowerLevel powerLevel = player.getCapability(CapabilityHandler.POWER_LEVEL_CAPABILITY).orElse(null);
		if (powerLevel == null) return; //Should never be null but just to be sure

		PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new PowerLevelPacket(powerLevel.getPowerLevel()));
	}

	@SubscribeEvent
	public static void registerCaps(AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof Player) {
			event.addCapability(CapabilityHandler.POWER_LEVEL_CAP, new PowerLevelCapability());
		}
	}

	@SubscribeEvent
	public static void onClone(PlayerEvent.Clone event) {
		// If not dead, player is returning from the End
		if (!event.isWasDeath()) return;

		Player original = event.getOriginal();
		Player clone = event.getEntity();

		// Copy the power level from the original player to the clone
		original.getCapability(CapabilityHandler.POWER_LEVEL_CAPABILITY).ifPresent(dataOriginal ->
				clone.getCapability(CapabilityHandler.POWER_LEVEL_CAPABILITY).ifPresent(dataClone -> {
					dataClone.setPowerLevel(dataOriginal.getPowerLevel());
				})
		);
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
