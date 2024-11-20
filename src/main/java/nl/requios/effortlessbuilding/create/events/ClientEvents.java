package nl.requios.effortlessbuilding.create.events;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.TickEvent.ClientTickEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import nl.requios.effortlessbuilding.create.Create;
import nl.requios.effortlessbuilding.create.CreateClient;
import nl.requios.effortlessbuilding.create.foundation.render.SuperRenderTypeBuffer;
import nl.requios.effortlessbuilding.create.foundation.utility.AnimationTickHolder;
import nl.requios.effortlessbuilding.create.foundation.utility.CameraAngleAnimationService;
import nl.requios.effortlessbuilding.create.foundation.utility.worldWrappers.WrappedClientWorld;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClientEvents {

	private static final String ITEM_PREFIX = "item." + Create.ID;
	private static final String BLOCK_PREFIX = "block." + Create.ID;

	@SubscribeEvent
	public static void onTick(ClientTickEvent event) {
		if (!isGameActive() || event.phase != TickEvent.Phase.END) return;

		AnimationTickHolder.tick();

		CreateClient.GHOST_BLOCKS.tickGhosts();
		CreateClient.OUTLINER.tickOutlines();
		CameraAngleAnimationService.tick();
	}

	@SubscribeEvent
	public static void onLoadWorld(LevelEvent.Load event) {
		LevelAccessor world = event.getLevel();
		if (world.isClientSide() && world instanceof ClientLevel && !(world instanceof WrappedClientWorld)) {
			CreateClient.invalidateRenderers();
			AnimationTickHolder.reset();
		}
	}

	@SubscribeEvent
	public static void onUnloadWorld(LevelEvent.Unload event) {
		if (!event.getLevel()
			.isClientSide())
			return;
		CreateClient.invalidateRenderers();
		AnimationTickHolder.reset();
	}

	@SubscribeEvent
	public static void onRenderWorld(RenderLevelStageEvent event) {
		if(event.getStage() != RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS) return;

		Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera()
			.getPosition();
		float pt = AnimationTickHolder.getPartialTicks();

		PoseStack ms = event.getPoseStack();
		ms.pushPose();
		ms.translate(-cameraPos.x(), -cameraPos.y(), -cameraPos.z());
		SuperRenderTypeBuffer buffer = SuperRenderTypeBuffer.getInstance();

		CreateClient.GHOST_BLOCKS.renderAll(ms, buffer);

		CreateClient.OUTLINER.renderOutlines(ms, buffer, pt);
		buffer.draw();
		RenderSystem.enableCull();

		ms.popPose();
	}

	@SubscribeEvent
	public static void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {
		float partialTicks = AnimationTickHolder.getPartialTicks();

		if (CameraAngleAnimationService.isYawAnimating())
			event.setYaw(CameraAngleAnimationService.getYaw(partialTicks));

		if (CameraAngleAnimationService.isPitchAnimating())
			event.setPitch(CameraAngleAnimationService.getPitch(partialTicks));
	}

	public static boolean isGameActive() {
		return !(Minecraft.getInstance().level == null || Minecraft.getInstance().player == null);
	}

}
