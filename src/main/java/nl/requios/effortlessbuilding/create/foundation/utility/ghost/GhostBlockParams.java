package nl.requios.effortlessbuilding.create.foundation.utility.ghost;

import net.createmod.catnip.theme.Color;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

public class GhostBlockParams {

	protected final BlockState state;
	protected BlockPos pos;
	protected Supplier<Float> alphaSupplier;
	protected Supplier<Float> scaleSupplier;
	public Supplier<Color> rgbSupplier;

	private GhostBlockParams(BlockState state) {
		this.state = state;
		this.pos = BlockPos.ZERO;
		this.alphaSupplier = () -> 1f;
		this.scaleSupplier = () -> 0.85f;
		this.rgbSupplier = () -> Color.WHITE;
	}

	public static GhostBlockParams of(BlockState state) {
		return new GhostBlockParams(state);
	}

	public static GhostBlockParams of(Block block) {
		return of(block.defaultBlockState());
	}

	public GhostBlockParams at(BlockPos pos) {
		this.pos = pos;
		return this;
	}

	public GhostBlockParams at(int x, int y, int z) {
		return this.at(new BlockPos(x, y, z));
	}

	public GhostBlockParams alpha(Supplier<Float> alphaSupplier) {
		this.alphaSupplier = alphaSupplier;
		return this;
	}

	public GhostBlockParams alpha(float alpha) {
		return this.alpha(() -> alpha);
	}

	public GhostBlockParams breathingAlpha() {
		return this.alpha(() -> (float) GhostBlocks.getBreathingAlpha());
	}

	public GhostBlockParams scale(Supplier<Float> scaleSupplier) {
		this.scaleSupplier = scaleSupplier;
		return this;
	}

	public GhostBlockParams scale(float scale) {
		return this.scale(() -> scale);
	}

	public GhostBlockParams colored(Supplier<Color> colorSupplier) {
		this.rgbSupplier = colorSupplier;
		return this;
	}

	public GhostBlockParams colored(Color color) {
		return this.colored(() -> color);
	}

	public GhostBlockParams colored(int color) {
		var color2 = new Color(color, false);
		return this.colored(() -> color2);
	}

	public GhostBlockParams breathingCyan() {
		return this.colored(() -> new Color((float) GhostBlocks.getBreathingColor(), 1f, 1f, 1f));
	}
}
