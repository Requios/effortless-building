package nl.requios.effortlessbuilding.proxy;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import nl.requios.effortlessbuilding.EffortlessBuilding;

@OnlyIn(Dist.CLIENT)
public class ClientProxy {

	public static void logTranslate(Player player, String prefix, String translationKey, String suffix, boolean actionBar) {
		EffortlessBuilding.log(player, prefix + I18n.get(translationKey) + suffix, actionBar);
	}
}
