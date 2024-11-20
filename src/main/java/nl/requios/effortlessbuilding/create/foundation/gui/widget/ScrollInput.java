package nl.requios.effortlessbuilding.create.foundation.gui.widget;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import nl.requios.effortlessbuilding.create.AllKeys;
import nl.requios.effortlessbuilding.create.foundation.utility.Components;
import nl.requios.effortlessbuilding.create.foundation.utility.Lang;

import java.util.function.Consumer;
import java.util.function.Function;

public class ScrollInput extends AbstractSimiWidget {

	protected Consumer<Integer> onScroll;
	protected int state;
	protected Component title = Lang.translateDirect("gui.scrollInput.defaultTitle");
	protected final Component scrollToModify = Lang.translateDirect("gui.scrollInput.scrollToModify");
	protected final Component shiftScrollsFaster = Lang.translateDirect("gui.scrollInput.shiftScrollsFaster");
	protected Component hint = null;
	protected Label displayLabel;
	protected boolean inverted;
	protected boolean soundPlayed;
	protected Function<Integer, Component> formatter;

	protected int min, max;
	protected int shiftStep;
	Function<StepContext, Integer> step;

	public ScrollInput(int xIn, int yIn, int widthIn, int heightIn) {
		super(xIn, yIn, widthIn, heightIn);
		state = 0;
		min = 0;
		max = 1;
		shiftStep = 5;
		step = standardStep();
		formatter = i -> Components.literal(String.valueOf(i));
		soundPlayed = false;
	}

	public Function<StepContext, Integer> standardStep() {
		return c -> c.shift ? shiftStep : 1;
	}

	public ScrollInput inverted() {
		inverted = true;
		return this;
	}

	public ScrollInput withRange(int min, int max) {
		this.min = min;
		this.max = max;
		return this;
	}

	public ScrollInput calling(Consumer<Integer> onScroll) {
		this.onScroll = onScroll;
		return this;
	}

	public ScrollInput format(Function<Integer, Component> formatter) {
		this.formatter = formatter;
		return this;
	}

	public ScrollInput removeCallback() {
		this.onScroll = null;
		return this;
	}

	public ScrollInput titled(MutableComponent title) {
		this.title = title;
		updateTooltip();
		return this;
	}

	public ScrollInput addHint(MutableComponent hint) {
		this.hint = hint;
		updateTooltip();
		return this;
	}

	public ScrollInput withStepFunction(Function<StepContext, Integer> step) {
		this.step = step;
		return this;
	}

	public ScrollInput writingTo(Label label) {
		this.displayLabel = label;
		if (label != null)
			writeToLabel();
		return this;
	}
	
	@Override
	public void tick() {
		super.tick();
		soundPlayed = false;
	}

	public int getState() {
		return state;
	}

	public ScrollInput setState(int state) {
		this.state = state;
		clampState();
		updateTooltip();
		if (displayLabel != null)
			writeToLabel();
		return this;
	}

	public ScrollInput withShiftStep(int step) {
		shiftStep = step;
		return this;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (!this.visible || !this.isHovered) return false; //Added

		if (inverted)
			scrollX *= -1;

		StepContext context = new StepContext();
		context.control = AllKeys.ctrlDown();
		context.shift = AllKeys.shiftDown();
		context.currentValue = state;
		context.forward = scrollX > 0;

		int priorState = state;
		boolean shifted = AllKeys.shiftDown();
		int step = (int) Math.signum(scrollX) * this.step.apply(context);

		state += step;
		if (shifted)
			state -= state % shiftStep;

		clampState();

		if (priorState != state) {
//			if (!soundPlayed)
//				Minecraft.getInstance()
//					.getSoundManager()
//					.play(SimpleSoundInstance.forUI(AllSoundEvents.SCROLL_VALUE.getMainEvent(),
//						1.5f + 0.1f * (state - min) / (max - min)));
//			soundPlayed = true;
			onChanged();
		}

//		return priorState != state;
		return true; //Changed
	}

	protected void clampState() {
		if (state >= max)
			state = max - 1;
		if (state < min)
			state = min;
	}

	public void onChanged() {
		if (displayLabel != null)
			writeToLabel();
		if (onScroll != null)
			onScroll.accept(state);
		updateTooltip();
	}

	protected void writeToLabel() {
		displayLabel.text = formatter.apply(state);
	}

	protected void updateTooltip() {
		toolTip.clear();
		if (title == null)
			return;
		toolTip.add(title.plainCopy()
			.withStyle(s -> s.withColor(HEADER_RGB)));
		if (hint != null)
			toolTip.add(hint.plainCopy()
				.withStyle(s -> s.withColor(HINT_RGB)));
		toolTip.add(scrollToModify.plainCopy()
			.withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_GRAY));
		toolTip.add(shiftScrollsFaster.plainCopy()
			.withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_GRAY));
	}

	public static class StepContext {
		public int currentValue;
		public boolean forward;
		public boolean shift;
		public boolean control;
	}
}
