package nl.requios.effortlessbuilding;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import nl.requios.effortlessbuilding.buildmode.BuildModes;
import nl.requios.effortlessbuilding.buildmodifier.BuildModifiers;
import nl.requios.effortlessbuilding.gui.DiamondRandomizerBagScreen;
import nl.requios.effortlessbuilding.gui.GoldenRandomizerBagScreen;
import nl.requios.effortlessbuilding.gui.RandomizerBagScreen;
import nl.requios.effortlessbuilding.render.BlockPreviews;
import nl.requios.effortlessbuilding.systems.*;

public class EffortlessBuildingClient {

    public static final BuilderChain BUILDER_CHAIN = new BuilderChain();
    public static final BuildModes BUILD_MODES = new BuildModes();
    public static final BuildModifiers BUILD_MODIFIERS = new BuildModifiers();
    public static final BuildSettings BUILD_SETTINGS = new BuildSettings();
    public static final BlockPreviews BLOCK_PREVIEWS = new BlockPreviews();
    public static final BuilderFilter BUILDER_FILTER = new BuilderFilter();
    public static final ItemUsageTracker ITEM_USAGE_TRACKER = new ItemUsageTracker();

    public static void onConstructorClient(IEventBus modEventBus, IEventBus forgeEventBus) {
        modEventBus.addListener(EffortlessBuildingClient::clientSetup);
    }

    public static void clientSetup(final FMLClientSetupEvent event) {
        MenuScreens.register(EffortlessBuilding.RANDOMIZER_BAG_CONTAINER.get(), RandomizerBagScreen::new);
        MenuScreens.register(EffortlessBuilding.GOLDEN_RANDOMIZER_BAG_CONTAINER.get(), GoldenRandomizerBagScreen::new);
        MenuScreens.register(EffortlessBuilding.DIAMOND_RANDOMIZER_BAG_CONTAINER.get(), DiamondRandomizerBagScreen::new);
    }
}
