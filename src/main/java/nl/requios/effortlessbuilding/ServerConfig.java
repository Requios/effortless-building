package nl.requios.effortlessbuilding;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.Arrays;
import java.util.List;

import static net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import static net.neoforged.neoforge.common.ModConfigSpec.Builder;
import static net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;
import static net.neoforged.neoforge.common.ModConfigSpec.IntValue;

public class ServerConfig {
    private static final Builder builder = new Builder();
    public static final ServerConfig.Validation validation = new ServerConfig.Validation(builder);
    public static final ServerConfig.Memory memory = new ServerConfig.Memory(builder);
    public static final ModConfigSpec spec = builder.build();

    public static class Validation {
        public final BooleanValue allowInSurvival;
        public final BooleanValue useWhitelist;
        public final ConfigValue<List<? extends String>> whitelist;
        public final IntValue maxBlocksPlacedAtOnce;

        public Validation(Builder builder) {
            builder.push("Validation");

            allowInSurvival = builder
                    .comment("Allow use of the mod for players that are in survival mode. Otherwise, only creative mode players can use the mod.")
                    .define("allowInSurvival", true);

            useWhitelist = builder
                    .comment("Use a whitelist to determine which players can use the mod. If false, all players can use the mod.")
                    .define("useWhitelist", false);

            whitelist = builder
                    .comment("List of player names that can use the mod.")
                    .defineList("whitelist", Arrays.asList("Player1", "Player2"), o -> true);

            maxBlocksPlacedAtOnce = builder
                    .comment("Maximum number of blocks that can be placed at once. This is a last check. If you want the player to receive visual feedback instead of an error message, change values in the common config.")
                    .defineInRange("maxBlocksPlacedAtOnce", 10000, 1, 100000);

            builder.pop();
        }
    }

    public static class Memory {
        public final IntValue undoStackSize;

        public Memory(Builder builder) {
            builder.push("Memory");

            undoStackSize = builder
                    .comment("How many sets of blocks are remembered for the undo functionality, per player.")
                    .worldRestart()
                    .defineInRange("undoStackSize", 50, 10, 200);

            builder.pop();
        }
    }
}
