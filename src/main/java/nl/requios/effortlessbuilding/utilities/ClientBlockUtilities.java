package nl.requios.effortlessbuilding.utilities;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import nl.requios.effortlessbuilding.EffortlessBuildingClient;

@OnlyIn(Dist.CLIENT)
public class ClientBlockUtilities {

    public static boolean determineIfLookingAtInteractiveObject(Minecraft mc, Level level) {
        //Check if we are looking at an interactive object
        var result = false;
        if (mc.hitResult != null) {
            if (mc.hitResult.getType() == HitResult.Type.BLOCK) {
                var blockHitResult = (BlockHitResult) mc.hitResult;
                var blockState = level.getBlockState(blockHitResult.getBlockPos());
                if (blockState.hasBlockEntity()) {
                    result = true;
                }
            }
            if (mc.hitResult.getType() == HitResult.Type.ENTITY) {
                result = true;
            }
        }
        return result;
    }

    public static void playSoundIfFurtherThanNormal(Player player, BlockEntry blockEntry, boolean breaking) {

        if (Minecraft.getInstance().hitResult != null && Minecraft.getInstance().hitResult.getType() == HitResult.Type.BLOCK)
            return;

        if (blockEntry == null || blockEntry.newBlockState == null)
            return;

        SoundType soundType = blockEntry.newBlockState.getBlock().getSoundType(blockEntry.newBlockState, player.level, blockEntry.blockPos, player);
        SoundEvent soundEvent = breaking ? soundType.getBreakSound() : soundType.getPlaceSound();
        player.level.playSound(player, player.blockPosition(), soundEvent, SoundSource.BLOCKS, 0.6f, soundType.getPitch());
    }

    public static BlockHitResult getLookingAtFar(Player player) {
        Level world = player.level;

        //base distance off of player ability (config)
        float raytraceRange = EffortlessBuildingClient.POWER_LEVEL.getPlacementReach(player);

        Vec3 look = player.getLookAngle();
        Vec3 start = new Vec3(player.getX(), player.getY() + player.getEyeHeight(), player.getZ());
        Vec3 end = new Vec3(player.getX() + look.x * raytraceRange, player.getY() + player.getEyeHeight() + look.y * raytraceRange, player.getZ() + look.z * raytraceRange);

        return world.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
    }
}
