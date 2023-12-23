package nl.requios.effortlessbuilding.systems;

import net.minecraft.world.entity.player.Player;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.capability.CapabilityHandler;

public class ServerBuildState {
    private static final String IS_USING_BUILD_MODE_KEY = EffortlessBuilding.MODID + ":isUsingBuildMode";
    private static final String IS_QUICK_REPLACING_KEY = EffortlessBuilding.MODID + ":isQuickReplacing";

    public static void handleNewPlayer(Player player) {
        setIsUsingBuildMode(player, false);
        setIsQuickReplacing(player, false);
    }

    public static boolean isUsingBuildMode(Player player) {
        return player.getPersistentData().contains(IS_USING_BUILD_MODE_KEY);
    }

    public static void setIsUsingBuildMode(Player player, boolean isUsingBuildMode) {
        if (isUsingBuildMode) {
            player.getPersistentData().putBoolean(IS_USING_BUILD_MODE_KEY, true);
        } else {
            player.getPersistentData().remove(IS_USING_BUILD_MODE_KEY);
        }
    }

    public static boolean isQuickReplacing(Player player) {
        if (!CapabilityHandler.canReplaceBlocks(player)) return false;
        return player.getPersistentData().contains(IS_QUICK_REPLACING_KEY);
    }

    public static void setIsQuickReplacing(Player player, boolean isQuickReplacing) {
        if (isQuickReplacing) {
            player.getPersistentData().putBoolean(IS_QUICK_REPLACING_KEY, true);
        } else {
            player.getPersistentData().remove(IS_QUICK_REPLACING_KEY);
        }
    }

    public static boolean isLikeVanilla(Player player) {
        return !isUsingBuildMode(player) && !isQuickReplacing(player);
    }
}
