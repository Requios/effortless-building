package nl.requios.effortlessbuilding;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import nl.requios.effortlessbuilding.buildmode.BuildModes;
import nl.requios.effortlessbuilding.buildmodifier.BuildModifiers;
import nl.requios.effortlessbuilding.gui.DiamondRandomizerBagScreen;
import nl.requios.effortlessbuilding.gui.GoldenRandomizerBagScreen;
import nl.requios.effortlessbuilding.gui.RandomizerBagScreen;
import nl.requios.effortlessbuilding.render.BlockPreviews;
import nl.requios.effortlessbuilding.systems.BuildSettings;
import nl.requios.effortlessbuilding.systems.BuilderChain;
import nl.requios.effortlessbuilding.systems.BuilderFilter;
import nl.requios.effortlessbuilding.systems.ItemUsageTracker;

public class EffortlessBuildingClient {

    public static final BuilderChain BUILDER_CHAIN = new BuilderChain();
    public static final BuildModes BUILD_MODES = new BuildModes();
    public static final BuildModifiers BUILD_MODIFIERS = new BuildModifiers();
    public static final BuildSettings BUILD_SETTINGS = new BuildSettings();
    public static final BlockPreviews BLOCK_PREVIEWS = new BlockPreviews();
    public static final BuilderFilter BUILDER_FILTER = new BuilderFilter();
    public static final ItemUsageTracker ITEM_USAGE_TRACKER = new ItemUsageTracker();

    public static void onConstructorClient(IEventBus modEventBus, IEventBus forgeEventBus) {
        modEventBus.addListener(EffortlessBuildingClient::registerMenuScreens);
    }

    public static void registerMenuScreens(final RegisterMenuScreensEvent event) {
        event.register(EffortlessBuilding.RANDOMIZER_BAG_CONTAINER.get(), RandomizerBagScreen::new);
        event.register(EffortlessBuilding.GOLDEN_RANDOMIZER_BAG_CONTAINER.get(), GoldenRandomizerBagScreen::new);
        event.register(EffortlessBuilding.DIAMOND_RANDOMIZER_BAG_CONTAINER.get(), DiamondRandomizerBagScreen::new);
    }
}
