package nl.requios.effortlessbuilding.buildmode;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import nl.requios.effortlessbuilding.EffortlessBuilding;
import nl.requios.effortlessbuilding.network.IsUsingBuildModePacket;
import nl.requios.effortlessbuilding.network.PacketHandler;
import nl.requios.effortlessbuilding.utilities.BlockSet;

@OnlyIn(Dist.CLIENT)
public class BuildModes {
	private BuildModeEnum buildMode = BuildModeEnum.DISABLED;
	private BuildModeEnum previousBuildMode = BuildModeEnum.DISABLED;
	private BuildModeEnum beforeDisabledBuildMode = BuildModeEnum.SINGLE;

	public void findCoordinates(BlockSet blocks, Player player) {
		buildMode.instance.findCoordinates(blocks);
	}

	public BuildModeEnum getBuildMode() {
		return buildMode;
	}

	public void setBuildMode(BuildModeEnum buildMode) {
		this.buildMode = buildMode;

		PacketHandler.INSTANCE.sendToServer(new IsUsingBuildModePacket(this.buildMode != BuildModeEnum.DISABLED));

		EffortlessBuilding.log(Minecraft.getInstance().player, I18n.get(buildMode.getNameKey()), true);
	}

	public void activatePreviousBuildMode() {
		var temp = buildMode;
		setBuildMode(previousBuildMode);
		previousBuildMode = temp;
	}

	public void activateDisableBuildModeToggle(){
		if (buildMode == BuildModeEnum.DISABLED) {
			setBuildMode(beforeDisabledBuildMode);
		} else {
			beforeDisabledBuildMode = buildMode;
			setBuildMode(BuildModeEnum.DISABLED);
		}
	}

	public void onCancel() {
		getBuildMode().instance.initialize();
	}

	//Find coordinates on a line bound by a plane
	public static Vec3 findXBound(double x, Vec3 start, Vec3 look) {
		//then y and z are
		double y = (x - start.x) / look.x * look.y + start.y;
		double z = (x - start.x) / look.x * look.z + start.z;

		return new Vec3(x, y, z);
	}

	public static Vec3 findYBound(double y, Vec3 start, Vec3 look) {
		//then x and z are
		double x = (y - start.y) / look.y * look.x + start.x;
		double z = (y - start.y) / look.y * look.z + start.z;

		return new Vec3(x, y, z);
	}

	public static Vec3 findZBound(double z, Vec3 start, Vec3 look) {
		//then x and y are
		double x = (z - start.z) / look.z * look.x + start.x;
		double y = (z - start.z) / look.z * look.y + start.y;

		return new Vec3(x, y, z);
	}

	//Use this instead of player.getLookVec() in any buildmodes code
	public static Vec3 getPlayerLookVec(Player player) {
		Vec3 lookVec = player.getLookAngle();
		double x = lookVec.x;
		double y = lookVec.y;
		double z = lookVec.z;

		//Further calculations (findXBound etc) don't like any component being 0 or 1 (e.g. dividing by 0)
		//isCriteriaValid below will take up to 2 minutes to raytrace blocks towards infinity if that is the case
		//So make sure they are close to but never exactly 0 or 1
		if (Math.abs(x) < 0.0001) x = 0.0001;
		if (Math.abs(x - 1.0) < 0.0001) x = 0.9999;
		if (Math.abs(x + 1.0) < 0.0001) x = -0.9999;

		if (Math.abs(y) < 0.0001) y = 0.0001;
		if (Math.abs(y - 1.0) < 0.0001) y = 0.9999;
		if (Math.abs(y + 1.0) < 0.0001) y = -0.9999;

		if (Math.abs(z) < 0.0001) z = 0.0001;
		if (Math.abs(z - 1.0) < 0.0001) z = 0.9999;
		if (Math.abs(z + 1.0) < 0.0001) z = -0.9999;

		return new Vec3(x, y, z);
	}

	public static boolean isCriteriaValid(Vec3 start, Vec3 look, int reach, Player player, boolean skipRaytrace, Vec3 lineBound, Vec3 planeBound, double distToPlayerSq) {
		boolean intersects = false;
		if (!skipRaytrace) {
			//collision within a 1 block radius to selected is fine
			ClipContext rayTraceContext = new ClipContext(start, lineBound, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player);
			HitResult rayTraceResult = player.level.clip(rayTraceContext);
			intersects = rayTraceResult != null && rayTraceResult.getType() == HitResult.Type.BLOCK &&
				planeBound.subtract(rayTraceResult.getLocation()).lengthSqr() > 4;
		}

		return planeBound.subtract(start).dot(look) > 0 &&
			distToPlayerSq > 2 && distToPlayerSq < reach * reach &&
			!intersects;
	}

}
