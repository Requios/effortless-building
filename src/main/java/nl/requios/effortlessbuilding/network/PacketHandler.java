package nl.requios.effortlessbuilding.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;
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

	public static void setupPackets(final RegisterPayloadHandlerEvent event) {
		final IPayloadRegistrar registrar = event.registrar(EffortlessBuilding.MODID);

		registrar.play(IsUsingBuildModePacket.ID, IsUsingBuildModePacket::new, handler -> handler
				.server(IsUsingBuildModePacket.Handler::handle));
		registrar.play(IsQuickReplacingPacket.ID, IsQuickReplacingPacket::new, handler -> handler
				.server(IsQuickReplacingPacket.Handler::handle));
		registrar.play(ServerPlaceBlocksPacket.ID, ServerPlaceBlocksPacket::new, handler -> handler
				.server(ServerPlaceBlocksPacket.Handler::handle));
		registrar.play(ServerBreakBlocksPacket.ID, ServerBreakBlocksPacket::new, handler -> handler
				.server(ServerBreakBlocksPacket.Handler::handle));
		registrar.play(PerformUndoPacket.ID, PerformUndoPacket::new, handler -> handler
				.server(PerformUndoPacket.Handler::handle));
		registrar.play(PerformRedoPacket.ID, PerformRedoPacket::new, handler -> handler
				.server(PerformRedoPacket.Handler::handle));

		registrar.play(ModifierSettingsPacket.ID, ModifierSettingsPacket::new, handler -> handler
				.server(ModifierSettingsPacket.ServerHandler::handleServer)
				.server(ModifierSettingsPacket.ClientHandler::handleClient));

		registrar.play(PowerLevelPacket.ID, PowerLevelPacket::new, handler -> handler
				.client(PowerLevelPacket.Handler::handle));
		registrar.play(TranslatedLogPacket.ID, TranslatedLogPacket::new, handler -> handler
				.client(TranslatedLogPacket.Handler::handle));
	}
}
