package nl.requios.effortlessbuilding.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import nl.requios.effortlessbuilding.EffortlessBuildingClient;
import nl.requios.effortlessbuilding.systems.BuilderChain;

/***
 * Main render class for Effortless Building
 */
@EventBusSubscriber(Dist.CLIENT)
public class RenderHandler {

	@SubscribeEvent
	public static void onRender(RenderLevelStageEvent event) {
		if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
		Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();

		PoseStack ms = event.getPoseStack();
		BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
		MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(bufferBuilder);

		ms.pushPose();
		ms.translate(-cameraPos.x(), -cameraPos.y(), -cameraPos.z());

		//Mirror and radial mirror lines and areas
		ModifierRenderer.render(ms, buffer);

		ms.popPose();

//		renderSubText(ms);
	}

	@SubscribeEvent
	public static void onRenderGuiEvent(RenderGuiEvent event) {
		renderSubText(event.getPoseStack());

		drawStacks(event.getPoseStack());
	}

	private static final ChatFormatting highlightColor = ChatFormatting.DARK_AQUA;
	private static final ChatFormatting normalColor = ChatFormatting.WHITE;
	private static final Component placingText = Component.literal(
			normalColor + "Left-click to " + highlightColor + "cancel, " +
			normalColor + "Right-click to " + highlightColor + "place");

	private static final Component breakingText = Component.literal(
			normalColor + "Left-click to " + highlightColor + "break, " +
			normalColor + "Right-click to " + highlightColor + "cancel");

	private static void renderSubText(PoseStack ms) {
		var state = EffortlessBuildingClient.BUILDER_CHAIN.getBuildingState();
		if (state == BuilderChain.BuildingState.IDLE) return;

		var text = state == BuilderChain.BuildingState.PLACING ? placingText : breakingText;

		int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
		int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
		var font = Minecraft.getInstance().font;

		ms.pushPose();
		ms.translate(screenWidth / 2.0, screenHeight - 54, 0.0D);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		int l = font.width(text);
		font.drawShadow(ms, text, (int)((float)(-l / 2)), -4, 0xffffffff);
		RenderSystem.disableBlend();
		ms.popPose();
	}

	//Draw item stacks at cursor, showing what will be used and what is missing
	private static void drawStacks(PoseStack ms) {
		var state = EffortlessBuildingClient.BUILDER_CHAIN.getBuildingState();
		if (state != BuilderChain.BuildingState.PLACING) return;

		var stacks = EffortlessBuildingClient.ITEM_USAGE_TRACKER.total;
		//Show if we are in survival or we are using multiple types of items
		if (Minecraft.getInstance().player == null || (Minecraft.getInstance().player.isCreative() && stacks.size() <= 1)) {
			return;
		}

		int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
		int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();

		int x = screenWidth / 2 + 10;
		int y = screenHeight / 2 - 8;

		//Draw item texture with count
		int i = 0;
		for (var stack : stacks.entrySet()) {
			int total = stack.getValue();
			int missing = EffortlessBuildingClient.ITEM_USAGE_TRACKER.getMissingCount(stack.getKey());

			if (total - missing > 0) {
				drawItemStack(ms, new ItemStack(stack.getKey(), total - missing), x + i * 20, y, false);
				i++;
			}

			if (missing > 0) {
				drawItemStack(ms, new ItemStack(stack.getKey(), missing), x + i * 20, y, true);
				i++;
			}
		}
	}

	private static void drawItemStack(PoseStack ms, ItemStack stack, int x, int y, boolean missing) {
		Minecraft.getInstance().getItemRenderer().renderGuiItem(ms, stack, x, y);

		//draw count text, red if missing
		//from ItemRenderer#renderGuiItemDecorations
		ms.pushPose();
		Font font = Minecraft.getInstance().font;
		String text = String.valueOf(stack.getCount());
		ms.translate(0.0D, 0.0D, 200.0F);
		MultiBufferSource.BufferSource multibuffersource$buffersource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
		font.drawInBatch(text, (float)(x + 19 - 2 - font.width(text)), (float)(y + 6 + 3), missing ? ChatFormatting.RED.getColor() : ChatFormatting.WHITE.getColor(), true, ms.last().pose(), multibuffersource$buffersource, Font.DisplayMode.NORMAL, 0, 15728880);
		multibuffersource$buffersource.endBatch();
		ms.popPose();
	}

	protected static VertexConsumer beginLines(MultiBufferSource.BufferSource renderTypeBuffer) {
		return renderTypeBuffer.getBuffer(BuildRenderTypes.LINES);
	}

	protected static void endLines(MultiBufferSource.BufferSource renderTypeBuffer) {
		renderTypeBuffer.endBatch();
	}

	protected static VertexConsumer beginPlanes(MultiBufferSource.BufferSource renderTypeBuffer) {
		return renderTypeBuffer.getBuffer(BuildRenderTypes.PLANES);
	}

	protected static void endPlanes(MultiBufferSource.BufferSource renderTypeBuffer) {
		renderTypeBuffer.endBatch();
	}

}
