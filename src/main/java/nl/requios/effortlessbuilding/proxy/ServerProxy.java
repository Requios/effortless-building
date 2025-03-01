package nl.requios.effortlessbuilding.proxy;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import nl.requios.effortlessbuilding.network.message.TranslatedLogPacket;

public class ServerProxy {
	//Only physical server! Singleplayer server is seen as clientproxy

	public static void logTranslate(Player player, String prefix, String translationKey, String suffix, boolean actionBar) {
		((ServerPlayer)player).connection.send(new TranslatedLogPacket(prefix, translationKey, suffix, actionBar));
	}
}
