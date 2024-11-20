package nl.requios.effortlessbuilding;

import net.neoforged.neoforge.common.ModConfigSpec;

import static net.neoforged.neoforge.common.ModConfigSpec.*;

public class ClientConfig {

	private static final Builder builder = new Builder();
	public static final Visuals visuals = new Visuals(builder);
	public static final ModConfigSpec spec = builder.build();

	public static class Visuals {
		public final BooleanValue showBlockPreviews;
		public final BooleanValue onlyShowBlockPreviewsWhenBuilding;
		public final IntValue maxBlockPreviews;
		public final IntValue appearAnimationLength;
		public final IntValue breakAnimationLength;

		public Visuals(Builder builder) {
			builder.push("Visuals");

			showBlockPreviews = builder
					.comment("Show previews of the blocks while placing them")
					.define("showBlockPreviews", true);

			onlyShowBlockPreviewsWhenBuilding = builder
				.comment("Show block previews only when actively using a build mode")
				.define("onlyShowBlockPreviewsWhenBuilding", true);

			maxBlockPreviews = builder
				.comment("Don't show block previews when placing more than this many blocks. " +
						 "The outline will always be rendered.")
				.defineInRange("maxBlockPreviews", 400, 0, 5000);

			appearAnimationLength = builder
					.comment("How long it takes for a block to appear when placed in ticks.",
							"Set to 0 to disable animation.")
					.defineInRange("appearAnimationLength", 5, 0, 100);

			breakAnimationLength = builder
					.comment("How long the break animation is in ticks.",
							"Set to 0 to disable animation.")
					.defineInRange("breakAnimationLength", 10, 0, 100);

			builder.pop();
		}
	}
}
