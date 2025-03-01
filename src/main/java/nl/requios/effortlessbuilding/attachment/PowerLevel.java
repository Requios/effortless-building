package nl.requios.effortlessbuilding.attachment;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.INBTSerializable;
import nl.requios.effortlessbuilding.CommonConfig;

public class PowerLevel implements INBTSerializable<CompoundTag> {
	public static final int MAX_POWER_LEVEL = 3; //Common access

	public PowerLevel() {
	}

	private int powerLevel = 0;

	public int getPowerLevel() {
		return this.powerLevel;
	}

	public int getNextPowerLevel() {
		return Math.min(getPowerLevel() + 1, MAX_POWER_LEVEL);
	}

	public void setPowerLevel(int powerLevel) {
		this.powerLevel = powerLevel;
	}

	public boolean canIncreasePowerLevel() {
		return getPowerLevel() < MAX_POWER_LEVEL;
	}

	public void increasePowerLevel() {
		if (canIncreasePowerLevel()) {
			setPowerLevel(getPowerLevel() + 1);
		}
	}

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
	public int getBuildModeReach(Player player) {
		//A bit further than placement reach, so you can build lines when looking to the side without having to move.
		return getPlacementReach(player, false) + 6;
	}

	public int getMaxBlocksPlacedAtOnce(Player player, boolean nextPowerLevel) {
		if (player.isCreative()) return CommonConfig.maxBlocksPlacedAtOnce.creative.get();
		return switch (nextPowerLevel ? getNextPowerLevel() : getPowerLevel()) {
			case 1 -> CommonConfig.maxBlocksPlacedAtOnce.level1.get();
			case 2 -> CommonConfig.maxBlocksPlacedAtOnce.level2.get();
			case 3 -> CommonConfig.maxBlocksPlacedAtOnce.level3.get();
			default -> CommonConfig.maxBlocksPlacedAtOnce.level0.get();
		};
	}

	public int getMaxBlocksPerAxis(Player player, boolean nextPowerLevel) {
		if (player.isCreative()) return CommonConfig.maxBlocksPerAxis.creative.get();
		return switch (nextPowerLevel ? getNextPowerLevel() : getPowerLevel()) {
			case 1 -> CommonConfig.maxBlocksPerAxis.level1.get();
			case 2 -> CommonConfig.maxBlocksPerAxis.level2.get();
			case 3 -> CommonConfig.maxBlocksPerAxis.level3.get();
			default -> CommonConfig.maxBlocksPerAxis.level0.get();
		};
	}

	public int getMaxMirrorRadius(Player player, boolean nextPowerLevel) {
		if (player.isCreative()) return CommonConfig.maxMirrorRadius.creative.get();
		return switch (getPowerLevel() + (nextPowerLevel ? 1 : 0)) {
			case 1 -> CommonConfig.maxMirrorRadius.level1.get();
			case 2 -> CommonConfig.maxMirrorRadius.level2.get();
			case 3 -> CommonConfig.maxMirrorRadius.level3.get();
			default -> CommonConfig.maxMirrorRadius.level0.get();
		};
	}

	public boolean isDisabled(Player player) {
		return getMaxBlocksPlacedAtOnce(player, false) <= 0 || getMaxBlocksPerAxis(player, false) <= 0;
	}

	public boolean canBreakFar(Player player) {
		return player.getAbilities().instabuild;
	}

	public boolean canReplaceBlocks(Player player) {
		return player.getAbilities().instabuild;
	}

	public CompoundTag serializeNBT(Provider provider) {
		CompoundTag tag = new CompoundTag();
		tag.putInt("powerLevel", getPowerLevel());
		return tag;
	}

	public void deserializeNBT(Provider provider, CompoundTag nbt) {
		setPowerLevel(nbt.getInt("powerLevel"));
	}
}
