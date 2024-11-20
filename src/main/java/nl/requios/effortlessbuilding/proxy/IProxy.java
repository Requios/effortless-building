package nl.requios.effortlessbuilding.proxy;

import net.minecraft.world.entity.player.Player;

public interface IProxy {
	void logTranslate(Player player, String prefix, String translationKey, String suffix, boolean actionBar);
}
