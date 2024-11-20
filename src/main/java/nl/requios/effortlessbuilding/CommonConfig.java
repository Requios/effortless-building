package nl.requios.effortlessbuilding;

import net.neoforged.neoforge.common.ModConfigSpec;

import static net.neoforged.neoforge.common.ModConfigSpec.*;

public class CommonConfig {

	private static final Builder builder = new Builder();
	public static final Reach reach = new Reach(builder);
	public static final MaxBlocksPlacedAtOnce maxBlocksPlacedAtOnce = new MaxBlocksPlacedAtOnce(builder);
	public static final MaxBlocksPerAxis maxBlocksPerAxis = new MaxBlocksPerAxis(builder);
	public static final MaxMirrorRadius maxMirrorRadius = new MaxMirrorRadius(builder);
	public static final ModConfigSpec spec = builder.build();

	public static class Reach {
		public final IntValue creative;
		public final IntValue level0;
		public final IntValue level1;
		public final IntValue level2;
		public final IntValue level3;

		public Reach(Builder builder) {
			builder.push("Reach");

			creative = builder
				.comment("How far away the player can place and break blocks.")
				.defineInRange("reachCreative", 200, 0, 1000);

			level0 = builder
				.comment("Maximum reach in survival without upgrades",
					"Consume Power Level upgrades to permanently increase this.")
				.defineInRange("reachLevel0", 0, 0, 1000);

			level1 = builder
				.defineInRange("reachLevel1", 8, 0, 1000);

			level2 = builder
				.defineInRange("reachLevel2", 16, 0, 1000);

			level3 = builder
				.defineInRange("reachLevel3", 32, 0, 1000);

			builder.pop();
		}
	}

	public static class MaxBlocksPlacedAtOnce {
		public final IntValue creative;
		public final IntValue level0;
		public final IntValue level1;
		public final IntValue level2;
		public final IntValue level3;

		public MaxBlocksPlacedAtOnce(Builder builder) {
			builder.push("MaxBlocksPlacedAtOnce");

			creative = builder
				.comment("How many blocks can be placed in one click.")
				.defineInRange("maxBlocksPlacedAtOnceCreative", 10000, 0, 100000);

			level0 = builder
				.comment("In survival without upgrades",
						"Consume Power Level upgrades to permanently increase this.",
						"Set to 0 to disable Effortless Building until the player has increased their Building Power Level.")
				.defineInRange("maxBlocksPlacedAtOnceLevel0", 128, 0, 100000);

			level1 = builder
				.defineInRange("maxBlocksPlacedAtOnceLevel1", 192, 0, 100000);

			level2 = builder
				.defineInRange("maxBlocksPlacedAtOnceLevel2", 320, 0, 100000);

			level3 = builder
				.defineInRange("maxBlocksPlacedAtOnceLevel3", 640, 0, 100000);

			builder.pop();
		}
	}

	public static class MaxBlocksPerAxis {
		public final IntValue creative;
		public final IntValue level0;
		public final IntValue level1;
		public final IntValue level2;
		public final IntValue level3;

		public MaxBlocksPerAxis(Builder builder) {
			builder.push("MaxBlocksPerAxis");

			creative = builder
				.comment("How many blocks can be placed at once per axis when using build modes (e.g. walls).",
						"Also affects the array modifier.")
				.defineInRange("maxBlocksPerAxisCreative", 1000, 0, 1000);

			level0 = builder
				.comment("In survival without upgrades",
						"Consume Power Level upgrades to permanently increase this.")
				.defineInRange("maxBlocksPerAxisLevel0", 8, 0, 1000);

			level1 = builder
				.defineInRange("maxBlocksPerAxisLevel1", 12, 0, 1000);

			level2 = builder
				.defineInRange("maxBlocksPerAxisLevel2", 16, 0, 1000);

			level3 = builder
				.defineInRange("maxBlocksPerAxisLevel3", 20, 0, 1000);

			builder.pop();
		}
	}

	public static class MaxMirrorRadius {
		public final IntValue creative;
		public final IntValue level0;
		public final IntValue level1;
		public final IntValue level2;
		public final IntValue level3;

		public MaxMirrorRadius(Builder builder) {
			builder.push("MaxMirrorRadius");

			creative = builder
				.comment("The maximum (radial) mirror radius.")
				.defineInRange("maxMirrorRadiusCreative", 200, 0, 1000);

			level0 = builder
				.comment("Maximum reach in survival without upgrades",
						"Consume Power Level upgrades upgrades to permanently increase this.")
				.defineInRange("maxMirrorRadiusLevel0", 16, 0, 1000);

			level1 = builder
				.defineInRange("maxMirrorRadiusLevel1", 32, 0, 1000);

			level2 = builder
				.defineInRange("maxMirrorRadiusLevel2", 48, 0, 1000);

			level3 = builder
				.defineInRange("maxMirrorRadiusLevel3", 64, 0, 1000);

			builder.pop();
		}
	}
}
