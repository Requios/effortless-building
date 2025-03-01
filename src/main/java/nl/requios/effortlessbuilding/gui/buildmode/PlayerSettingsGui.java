package nl.requios.effortlessbuilding.gui.buildmode;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import net.neoforged.neoforge.client.gui.widget.ExtendedSlider;
import nl.requios.effortlessbuilding.EffortlessBuilding;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PlayerSettingsGui extends Screen {

	protected int left, right, top, bottom;
	protected boolean showShaderList = false;
	private Button shaderTypeButton;
	private ShaderTypeList shaderTypeList;
	private Button closeButton;

	public PlayerSettingsGui() {
		super(Component.translatable("effortlessbuilding.screen.player_settings"));
	}

	@Override
	protected void init() {
		left = this.width / 2 - 140;
		right = this.width / 2 + 140;
		top = this.height / 2 - 100;
		bottom = this.height / 2 + 100;

		int yy = top;
		shaderTypeList = new ShaderTypeList(this.minecraft);
		addWidget(shaderTypeList);
		//TODO set selected name
		Component currentShaderName = ShaderType.DISSOLVE_BLUE.name;
		shaderTypeButton = new ExtendedButton(right - 180, yy, 180, 20, currentShaderName, (button) -> {
			showShaderList = !showShaderList;
		});
		addRenderableOnly(shaderTypeButton);

		yy += 50;
		ExtendedSlider slider = new ExtendedSlider(right - 200, yy, 200, 20, Component.empty(), Component.empty(), 0.5, 2.0, 1.0, true);
		addRenderableOnly(slider);

		closeButton = new ExtendedButton(left + 50, bottom - 20, 180, 20, Component.literal("Done"), (button) -> this.minecraft.player.closeContainer());
		addRenderableOnly(closeButton);
	}

	@Override
	public void tick() {
		super.tick();
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		super.render(guiGraphics, mouseX, mouseY, partialTicks);

		int yy = top;
		guiGraphics.drawString(font, "Shader type", left, yy + 5, 0xFFFFFF, false);

		yy += 50;
		guiGraphics.drawString(font, "Shader speed", left, yy + 5, 0xFFFFFF, false);

		if (showShaderList)
			this.shaderTypeList.render(guiGraphics, mouseX, mouseY, partialTicks);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		if (showShaderList) {
			if (!shaderTypeList.isMouseOver(mouseX, mouseY) && !shaderTypeButton.isMouseOver(mouseX, mouseY))
				showShaderList = false;
		}
		return true;
	}

	@Override
	public void removed() {
		ShaderTypeList.ShaderTypeEntry selectedShader = shaderTypeList.getSelected();
		//TODO save and remove
	}

	public enum ShaderType {
		DISSOLVE_BLUE("Dissolve Blue"),
		DISSOLVE_ORANGE("Dissolve Orange");

		public Component name;

		ShaderType(Component name) {
			this.name = name;
		}

		ShaderType(String name) {
			this.name = Component.literal(name);
		}
	}

	//Inspired by LanguageScreen
	@OnlyIn(Dist.CLIENT)
	class ShaderTypeList extends ObjectSelectionList<PlayerSettingsGui.ShaderTypeList.ShaderTypeEntry> {

		public ShaderTypeList(Minecraft mcIn) {
			super(mcIn, 180, 140, top + 20, /*top + 100,*/ 18);
			this.setX(right - width);

			for (int i = 0; i < 40; i++) {

				for (ShaderType shaderType : ShaderType.values()) {
					ShaderTypeEntry shaderTypeEntry = new ShaderTypeEntry(shaderType);
					addEntry(shaderTypeEntry);
					//TODO setSelected to this if appropriate
				}

			}

			if (this.getSelected() != null) {
				this.centerScrollOn(this.getSelected());
			}
		}

		@Override
		public int getRowWidth() {
			return width;
		}

		@Override
		public void setSelected(PlayerSettingsGui.ShaderTypeList.ShaderTypeEntry selected) {
			super.setSelected(selected);
			Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			EffortlessBuilding.log("Selected shader " + selected.shaderType.name);
			shaderTypeButton.setMessage(selected.shaderType.name);
//            showShaderList = false;
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int pButton) {
			if (!showShaderList) return false;
			return super.mouseClicked(mouseX, mouseY, pButton);
		}

		@Override
		public boolean mouseReleased(double mouseX, double mouseY, int button) {
			if (!showShaderList) return false;
			return super.mouseReleased(mouseX, mouseY, button);
		}

		@Override
		public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
			if (!showShaderList) return false;
			return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
		}

		@Override
		public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
			if (!showShaderList) return false;
			return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
		}

		@Override
		public boolean isMouseOver(double mouseX, double mouseY) {
			if (!showShaderList) return false;
			return super.isMouseOver(mouseX, mouseY);
		}

		public boolean isFocused() {
			return PlayerSettingsGui.this.getFocused() == this;
		}

		@Override
		protected int getScrollbarPosition() {
			return right - 6;
		}

		//From AbstractSelectionList, disabled parts
//		@Override //TODO: Check if this is still needed!
//		public void render(GuiGraphics guiGraphics, int p_render_1_, int p_render_2_, float p_render_3_) {
//			this.renderBackground(guiGraphics);
//			int i = this.getScrollbarPosition();
//			int j = i + 6;
//			Tesselator tessellator = Tesselator.getInstance();
//			BufferBuilder bufferbuilder = tessellator.getBuilder();
////            this.minecraft.getTextureManager().bindTexture(AbstractGui.BACKGROUND_LOCATION);
//			RenderSystem.enableBlend();
//			RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
//			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
//			float f = 32.0F;
//			bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
//			bufferbuilder.addVertex(this.x0, this.y1, 0.0D).setColor(20, 20, 20, 180);
//			bufferbuilder.addVertex(this.x1, this.y1, 0.0D).setColor(20, 20, 20, 180);
//			bufferbuilder.addVertex(this.x1, this.y0, 0.0D).setColor(20, 20, 20, 180);
//			bufferbuilder.addVertex(this.x0, this.y0, 0.0D).setColor(20, 20, 20, 180);
//			tessellator.end();
//			int k = this.getRowLeft();
//			int l = this.y0 + 4 - (int) this.getScrollAmount();
//			if (this.renderHeader) {
//				this.renderHeader(guiGraphics, k, l);
//			}
//
//			this.renderList(guiGraphics, p_render_1_, p_render_2_, p_render_3_);
//			RenderSystem.disableDepthTest();
////            this.renderHoleBackground(0, this.y0, 255, 255);
////            this.renderHoleBackground(this.y1, this.height, 255, 255);
//			RenderSystem.enableBlend();
//			RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
////			RenderSystem.disableTexture();
//			RenderSystem.setShader(GameRenderer::getPositionColorShader);
////            int i1 = 4;
////            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
////            bufferbuilder.pos((double)this.x0, (double)(this.y0 + 4), 0.0D).tex(0.0F, 1.0F).setColor(0, 0, 0, 0);
////            bufferbuilder.pos((double)this.x1, (double)(this.y0 + 4), 0.0D).tex(1.0F, 1.0F).setColor(0, 0, 0, 0);
////            bufferbuilder.pos((double)this.x1, (double)this.y0, 0.0D).tex(1.0F, 0.0F).setColor(0, 0, 0, 255);
////            bufferbuilder.pos((double)this.x0, (double)this.y0, 0.0D).tex(0.0F, 0.0F).setColor(0, 0, 0, 255);
////            tessellator.draw();
////            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
////            bufferbuilder.pos((double)this.x0, (double)this.y1, 0.0D).tex(0.0F, 1.0F).setColor(0, 0, 0, 255);
////            bufferbuilder.pos((double)this.x1, (double)this.y1, 0.0D).tex(1.0F, 1.0F).setColor(0, 0, 0, 255);
////            bufferbuilder.pos((double)this.x1, (double)(this.y1 - 4), 0.0D).tex(1.0F, 0.0F).setColor(0, 0, 0, 0);
////            bufferbuilder.pos((double)this.x0, (double)(this.y1 - 4), 0.0D).tex(0.0F, 0.0F).setColor(0, 0, 0, 0);
////            tessellator.draw();
//
//			//SCROLLBAR
//			int j1 = this.getMaxScroll();
//			if (j1 > 0) {
//				int k1 = (int) ((float) ((this.y1 - this.y0) * (this.y1 - this.y0)) / (float) this.getMaxPosition());
//				k1 = Mth.clamp(k1, 32, this.y1 - this.y0 - 8);
//				int l1 = (int) this.getScrollAmount() * (this.y1 - this.y0 - k1) / j1 + this.y0;
//				if (l1 < this.y0) {
//					l1 = this.y0;
//				}
//
//				bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
//				bufferbuilder.addVertex(i, this.y1, 0.0D).uv(0.0F, 1.0F).setColor(0, 0, 0, 255);
//				bufferbuilder.addVertex(j, this.y1, 0.0D).uv(1.0F, 1.0F).setColor(0, 0, 0, 255);
//				bufferbuilder.addVertex(j, this.y0, 0.0D).uv(1.0F, 0.0F).setColor(0, 0, 0, 255);
//				bufferbuilder.addVertex(i, this.y0, 0.0D).uv(0.0F, 0.0F).setColor(0, 0, 0, 255);
//				tessellator.end();
//				bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
//				bufferbuilder.addVertex(i, l1 + k1, 0.0D).uv(0.0F, 1.0F).setColor(128, 128, 128, 255);
//				bufferbuilder.addVertex(j, l1 + k1, 0.0D).uv(1.0F, 1.0F).setColor(128, 128, 128, 255);
//				bufferbuilder.addVertex(j, l1, 0.0D).uv(1.0F, 0.0F).setColor(128, 128, 128, 255);
//				bufferbuilder.addVertex(i, l1, 0.0D).uv(0.0F, 0.0F).setColor(128, 128, 128, 255);
//				tessellator.end();
//				bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
//				bufferbuilder.addVertex(i, l1 + k1 - 1, 0.0D).uv(0.0F, 1.0F).setColor(192, 192, 192, 255);
//				bufferbuilder.addVertex(j - 1, l1 + k1 - 1, 0.0D).uv(1.0F, 1.0F).setColor(192, 192, 192, 255);
//				bufferbuilder.addVertex(j - 1, l1, 0.0D).uv(1.0F, 0.0F).setColor(192, 192, 192, 255);
//				bufferbuilder.addVertex(i, l1, 0.0D).uv(0.0F, 0.0F).setColor(192, 192, 192, 255);
//				tessellator.end();
//			}
//
////            this.renderDecorations(p_render_1_, p_render_2_);
////			RenderSystem.enableTexture();
//			RenderSystem.disableBlend();
//		}

//		public int getMaxScroll() {
//			return Math.max(0, this.getMaxPosition() - (this.y1 - this.y0 - 4));
//		}

		@OnlyIn(Dist.CLIENT)
		public class ShaderTypeEntry extends ObjectSelectionList.Entry<ShaderTypeEntry> {
			private final ShaderType shaderType;

			public ShaderTypeEntry(ShaderType shaderType) {
				this.shaderType = shaderType;
			}

			@Override
			public void render(GuiGraphics guiGraphics, int itemIndex, int rowTop, int rowLeft, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean hovered, float partialTicks) {
				if (rowTop + 10 > ShaderTypeList.this.getY() && rowTop + rowHeight - 5 < (ShaderTypeList.this.getY() + ShaderTypeList.this.getHeight()))
					guiGraphics.drawString(font, shaderType.name, ShaderTypeList.this.getX() + 8, rowTop + 4, 0xFFFFFF, false);
			}

			@Override
			public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
				if (p_mouseClicked_5_ == 0) {
					setSelected(this);
					return true;
				} else {
					return false;
				}
			}

			@Override
			public Component getNarration() {
				return null;
			}
		}
	}
}
