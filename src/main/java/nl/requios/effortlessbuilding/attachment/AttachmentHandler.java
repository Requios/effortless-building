package nl.requios.effortlessbuilding.attachment;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import nl.requios.effortlessbuilding.CommonConfig;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.network.message.PowerLevelPacket;

public class AttachmentHandler {
    public static final ResourceLocation POWER_LEVEL_CAP = EffortlessBuilding.asResource("power_level");

    public static void syncToClient(Player player) {
        PowerLevel powerLevel = player.getData(EffortlessBuilding.POWER_LEVEL);
        if (powerLevel == null) return; //Should never be null but just to be sure

        ((ServerPlayer)player).connection.send(new PowerLevelPacket(powerLevel.getPowerLevel()));
    }

    //Helper methods to reduce boilerplate code
    public static boolean canReplaceBlocks(Player player) {
        if (player != null) {
            PowerLevel powerLevel = player.getData(EffortlessBuilding.POWER_LEVEL);
            if (powerLevel != null) {
                return powerLevel.canReplaceBlocks(player);
            }
        }
        return false;
    }

    public static int getMaxBlocksPerAxis(Player player, boolean nextPowerLevel) {
        if (player != null) {
            PowerLevel powerLevel = player.getData(EffortlessBuilding.POWER_LEVEL);
            if (powerLevel != null) {
                return powerLevel.getMaxBlocksPerAxis(player, nextPowerLevel);
            }
        }
        return CommonConfig.maxBlocksPerAxis.level0.get();
    }

    public static int getMaxBlocksPlacedAtOnce(Player player, boolean nextPowerLevel) {
        if (player != null) {
            PowerLevel powerLevel = player.getData(EffortlessBuilding.POWER_LEVEL);
            if (powerLevel != null) {
                return powerLevel.getMaxBlocksPlacedAtOnce(player, nextPowerLevel);
            }
        }
        return CommonConfig.maxBlocksPlacedAtOnce.level0.get();
    }

    public static int getMaxMirrorRadius(Player player, boolean nextPowerLevel) {
        if (player != null) {
            PowerLevel powerLevel = player.getData(EffortlessBuilding.POWER_LEVEL);
            if (powerLevel != null) {
                return powerLevel.getMaxMirrorRadius(player, nextPowerLevel);
            }
        }
        return CommonConfig.maxMirrorRadius.level0.get();
    }

    public static int getBuildModeReach(Player player) {
        if (player != null) {
            PowerLevel powerLevel = player.getData(EffortlessBuilding.POWER_LEVEL);
            if (powerLevel != null) {
                return powerLevel.getBuildModeReach(player);
            }
        }
        return CommonConfig.maxMirrorRadius.level0.get() + 6;
    }

    public static int getPlacementReach(Player player, boolean nextPowerLevel) {
        if (player != null) {
            PowerLevel powerLevel = player.getData(EffortlessBuilding.POWER_LEVEL);
            if (powerLevel != null) {
                return powerLevel.getPlacementReach(player, nextPowerLevel);
            }
        }
        return CommonConfig.reach.level0.get();
    }

    public static int getPowerLevel(Player player) {
        if (player != null) {
            PowerLevel powerLevel = player.getData(EffortlessBuilding.POWER_LEVEL);
            if (powerLevel != null) {
                return powerLevel.getPowerLevel();
            }
        }
        return 0;
    }

    public static int getNextPowerLevel(Player player) {
        if (player != null) {
            PowerLevel powerLevel = player.getData(EffortlessBuilding.POWER_LEVEL);
            if (powerLevel != null) {
                return powerLevel.getNextPowerLevel();
            }
        }
        return 0;
    }

    public static boolean canIncreasePowerLevel(Player player) {
        if (player != null) {
            PowerLevel powerLevel = player.getData(EffortlessBuilding.POWER_LEVEL);
            if (powerLevel != null) {
                return powerLevel.canIncreasePowerLevel();
            }
        }
        return false;
    }

    public static boolean isDisabled(Player player) {
        if (player != null) {
            PowerLevel powerLevel = player.getData(EffortlessBuilding.POWER_LEVEL);
            if (powerLevel != null) {
                return powerLevel.isDisabled(player);
            }
        }
        return false;
    }

    public static boolean canBreakFar(Player player) {
        if (player != null) {
            PowerLevel powerLevel = player.getData(EffortlessBuilding.POWER_LEVEL);
            if (powerLevel != null) {
                return powerLevel.canBreakFar(player);
            }
        }
        return false;
    }
}
