package nl.requios.effortlessbuilding.capability;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

@AutoRegisterCapability
public interface IPowerLevel {

    int getPowerLevel();

    int getNextPowerLevel();

    void setPowerLevel(int powerLevel);

    boolean canIncreasePowerLevel();

    void increasePowerLevel();


    int getPlacementReach(Player player, boolean nextPowerLevel);

    int getBuildModeReach(Player player);


    int getMaxBlocksPlacedAtOnce(Player player, boolean nextPowerLevel);


    int getMaxBlocksPerAxis(Player player, boolean nextPowerLevel);


    int getMaxMirrorRadius(Player player, boolean nextPowerLevel);

    boolean isDisabled(Player player);

    boolean canBreakFar(Player player);

    boolean canReplaceBlocks(Player player);
}
