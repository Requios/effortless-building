package nl.requios.effortlessbuilding.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.network.message.IsQuickReplacingPacket;
import nl.requios.effortlessbuilding.network.message.IsUsingBuildModePacket;
import nl.requios.effortlessbuilding.network.message.ModifierSettingsPacket;
import nl.requios.effortlessbuilding.network.message.PerformRedoPacket;
import nl.requios.effortlessbuilding.network.message.PerformUndoPacket;
import nl.requios.effortlessbuilding.network.message.PowerLevelPacket;
import nl.requios.effortlessbuilding.network.message.ServerBreakBlocksPacket;
import nl.requios.effortlessbuilding.network.message.ServerPlaceBlocksPacket;
import nl.requios.effortlessbuilding.network.message.TranslatedLogPacket;

public class PacketHandler {

	public static void setupPackets(final RegisterPayloadHandlersEvent event) {
		final PayloadRegistrar registrar = event.registrar(EffortlessBuilding.MODID);

		registrar.playToServer(IsUsingBuildModePacket.ID, IsUsingBuildModePacket.CODEC, IsUsingBuildModePacket.Handler::handle);
		registrar.playToServer(IsQuickReplacingPacket.ID, IsQuickReplacingPacket.CODEC, IsQuickReplacingPacket.Handler::handle);
		registrar.playToServer(ServerPlaceBlocksPacket.ID, ServerPlaceBlocksPacket.CODEC, ServerPlaceBlocksPacket.Handler::handle);
		registrar.playToServer(ServerBreakBlocksPacket.ID, ServerBreakBlocksPacket.CODEC, ServerBreakBlocksPacket.Handler::handle);
		registrar.playToServer(PerformUndoPacket.ID, PerformUndoPacket.CODEC, PerformUndoPacket.Handler::handle);
		registrar.playToServer(PerformRedoPacket.ID, PerformRedoPacket.CODEC, PerformRedoPacket.Handler::handle);

		registrar.playBidirectional(ModifierSettingsPacket.ID, ModifierSettingsPacket.CODEC,
				new DirectionalPayloadHandler<>(
						ModifierSettingsPacket.ClientHandler::handleClient,
						ModifierSettingsPacket.ServerHandler::handleServer
				)
		);

		registrar.playToClient(PowerLevelPacket.ID, PowerLevelPacket.CODEC, PowerLevelPacket.Handler::handle);
		registrar.playToClient(TranslatedLogPacket.ID, TranslatedLogPacket.CODEC, TranslatedLogPacket.Handler::handle);
	}
}
