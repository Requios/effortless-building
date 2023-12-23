package nl.requios.effortlessbuilding.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import nl.requios.effortlessbuilding.CommonConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PowerLevelCapability implements IPowerLevel, ICapabilitySerializable<CompoundTag> {
    public static final int MAX_POWER_LEVEL = 3; //Common access

    private int powerLevel = 0;

    @Override
    public int getPowerLevel() {
        return this.powerLevel;
    }

    @Override
    public int getNextPowerLevel() {
        return Math.min(getPowerLevel() + 1, MAX_POWER_LEVEL);
    }

    @Override
    public void setPowerLevel(int powerLevel) {
        this.powerLevel = powerLevel;
    }

    @Override
    public boolean canIncreasePowerLevel() {
        return getPowerLevel() < MAX_POWER_LEVEL;
    }

    @Override
    public void increasePowerLevel() {
        if (canIncreasePowerLevel()) {
            setPowerLevel(getPowerLevel() + 1);
        }
    }

    @Override
    public int getPlacementReach(Player player, boolean nextPowerLevel) {
        if (player.isCreative()) return CommonConfig.reach.creative.get();
        return switch (nextPowerLevel ? getNextPowerLevel() : getPowerLevel()) {
            case 1 -> CommonConfig.reach.level1.get();
            case 2 -> CommonConfig.reach.level2.get();
            case 3 -> CommonConfig.reach.level3.get();
            default -> CommonConfig.reach.level0.get();
        };
    }

    //How far away we can detect the second and third click of build modes (distance to player)
    @Override
    public int getBuildModeReach(Player player) {
        //A bit further than placement reach, so you can build lines when looking to the side without having to move.
        return getPlacementReach(player, false) + 6;
    }

    @Override
    public int getMaxBlocksPlacedAtOnce(Player player, boolean nextPowerLevel) {
        if (player.isCreative()) return CommonConfig.maxBlocksPlacedAtOnce.creative.get();
        return switch (nextPowerLevel ? getNextPowerLevel() : getPowerLevel()) {
            case 1 -> CommonConfig.maxBlocksPlacedAtOnce.level1.get();
            case 2 -> CommonConfig.maxBlocksPlacedAtOnce.level2.get();
            case 3 -> CommonConfig.maxBlocksPlacedAtOnce.level3.get();
            default -> CommonConfig.maxBlocksPlacedAtOnce.level0.get();
        };
    }

    @Override
    public int getMaxBlocksPerAxis(Player player, boolean nextPowerLevel) {
        if (player.isCreative()) return CommonConfig.maxBlocksPerAxis.creative.get();
        return switch (nextPowerLevel ? getNextPowerLevel() : getPowerLevel()) {
            case 1 -> CommonConfig.maxBlocksPerAxis.level1.get();
            case 2 -> CommonConfig.maxBlocksPerAxis.level2.get();
            case 3 -> CommonConfig.maxBlocksPerAxis.level3.get();
            default -> CommonConfig.maxBlocksPerAxis.level0.get();
        };
    }

    @Override
    public int getMaxMirrorRadius(Player player, boolean nextPowerLevel) {
        if (player.isCreative()) return CommonConfig.maxMirrorRadius.creative.get();
        return switch (getPowerLevel() + (nextPowerLevel ? 1 : 0)) {
            case 1 -> CommonConfig.maxMirrorRadius.level1.get();
            case 2 -> CommonConfig.maxMirrorRadius.level2.get();
            case 3 -> CommonConfig.maxMirrorRadius.level3.get();
            default -> CommonConfig.maxMirrorRadius.level0.get();
        };
    }

    @Override
    public boolean isDisabled(Player player) {
        return getMaxBlocksPlacedAtOnce(player, false) <= 0 || getMaxBlocksPerAxis(player, false) <= 0;
    }

    @Override
    public boolean canBreakFar(Player player) {
        return player.getAbilities().instabuild;
    }

    @Override
    public boolean canReplaceBlocks(Player player) {
        return player.getAbilities().instabuild;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return CapabilityHandler.POWER_LEVEL_CAPABILITY.orEmpty(cap, LazyOptional.of(() -> this));
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("powerLevel", getPowerLevel());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        setPowerLevel(nbt.getInt("powerLevel"));
    }
}
