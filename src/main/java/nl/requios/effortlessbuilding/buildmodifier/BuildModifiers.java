package nl.requios.effortlessbuilding.buildmodifier;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import nl.requios.effortlessbuilding.create.foundation.utility.NBTHelper;
import nl.requios.effortlessbuilding.network.message.ModifierSettingsPacket;
import nl.requios.effortlessbuilding.utilities.BlockSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class BuildModifiers {
	private List<BaseModifier> modifierSettingsList = new ArrayList<>();

	public List<BaseModifier> getModifierSettingsList() {
		return Collections.unmodifiableList(modifierSettingsList);
	}

	public void addModifierSettings(BaseModifier modifierSettings) {
		modifierSettingsList.add(modifierSettings);
	}

	public void removeModifierSettings(BaseModifier modifierSettings) {
		modifierSettingsList.remove(modifierSettings);
	}

	public void removeModifierSettings(int index) {
		modifierSettingsList.remove(index);
	}

	public void moveUp(BaseModifier modifierSettings) {
		int index = modifierSettingsList.indexOf(modifierSettings);
		if (index == 0) return;

		Collections.swap(modifierSettingsList, index, index - 1);
	}

	public void moveDown(BaseModifier modifierSettings) {
		int index = modifierSettingsList.indexOf(modifierSettings);
		if (index == modifierSettingsList.size() - 1) return;

		Collections.swap(modifierSettingsList, index, index + 1);
	}

	public void setFirst(BaseModifier modifierSettings) {
		int index = modifierSettingsList.indexOf(modifierSettings);
		if (index == 0) return;

		modifierSettingsList.remove(index);
		modifierSettingsList.add(0, modifierSettings);
	}

	public void setLast(BaseModifier modifierSettings) {
		int index = modifierSettingsList.indexOf(modifierSettings);
		if (index == modifierSettingsList.size() - 1) return;

		modifierSettingsList.remove(index);
		modifierSettingsList.add(modifierSettings);
	}

	public void clearAllModifierSettings() {
		modifierSettingsList.clear();
	}

	public void findCoordinates(BlockSet blocks, Player player) {
		for (BaseModifier modifierSettings : modifierSettingsList) {
			modifierSettings.findCoordinates(blocks, player);
		}
	}

	public void onPowerLevelChanged(int powerLevel) {
		for (BaseModifier modifierSettings : modifierSettingsList) {
			modifierSettings.onPowerLevelChanged(powerLevel);
		}
	}

	public CompoundTag serializeNBT() {
		var compoundTag = new CompoundTag();
		compoundTag.put("modifierSettingsList", NBTHelper.writeCompoundList(modifierSettingsList, BaseModifier::serializeNBT));
		return compoundTag;
	}

	public void deserializeNBT(CompoundTag compoundTag) {
		var listTag = compoundTag.getList("modifierSettingsList", Tag.TAG_COMPOUND);
		modifierSettingsList = NBTHelper.readCompoundList(listTag, tag -> {
			var modifier = createModifier(tag.getString("type"));
			modifier.deserializeNBT(tag);
			return modifier;
		});
	}

	public void save() {
		PacketDistributor.SERVER.noArg().send(new ModifierSettingsPacket(serializeNBT()));

		//Save locally as well?
//		var listTag = NBTHelper.writeCompoundList(modifierSettingsList, BaseModifier::serializeNBT);
//		player.getPersistentData().put(DATA_KEY, listTag);
	}

	private BaseModifier createModifier(String type) {
		switch (type) {
			case "Mirror": return new Mirror();
			case "Array": return new Array();
			case "RadialMirror": return new RadialMirror();
			default: throw new IllegalArgumentException("Unknown modifier type: " + type);
		}
	}

//	public static String sanitize(ModifierSettingsManager.ModifierSettings modifierSettings, Player player) {
//		int maxReach = ReachHelper.getMaxReach(player);
//		String error = "";
//
//		//Mirror settings
//		Mirror.MirrorSettings m = modifierSettings.getMirrorSettings();
//		if (m.radius < 1) {
//			m.radius = 1;
//			error += "Mirror size has to be at least 1. This has been corrected. ";
//		}
//		if (m.getReach() > maxReach) {
//			m.radius = maxReach / 2;
//			error += "Mirror exceeds your maximum reach of " + (maxReach / 2) + ". Radius has been set to " + (maxReach / 2) + ". ";
//		}
//
//		//Array settings
//		Array.ArraySettings a = modifierSettings.getArraySettings();
//		if (a.count < 0) {
//			a.count = 0;
//			error += "Array count may not be negative. It has been reset to 0.";
//		}
//
//		if (a.getReach() > maxReach) {
//			a.count = 0;
//			error += "Array exceeds your maximum reach of " + maxReach + ". Array count has been reset to 0. ";
//		}
//
//		//Radial mirror settings
//		RadialMirror.RadialMirrorSettings r = modifierSettings.getRadialMirrorSettings();
//		if (r.slices < 2) {
//			r.slices = 2;
//			error += "Radial mirror needs to have at least 2 slices. Slices has been set to 2.";
//		}
//
//		if (r.radius < 1) {
//			r.radius = 1;
//			error += "Radial mirror radius has to be at least 1. This has been corrected. ";
//		}
//		if (r.getReach() > maxReach) {
//			r.radius = maxReach / 2;
//			error += "Radial mirror exceeds your maximum reach of " + (maxReach / 2) + ". Radius has been set to " + (maxReach / 2) + ". ";
//		}
//
//		return error;
//	}
}
