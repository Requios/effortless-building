package nl.requios.effortlessbuilding;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import nl.requios.effortlessbuilding.attachment.PowerLevel;
import nl.requios.effortlessbuilding.compatibility.CompatHelper;
import nl.requios.effortlessbuilding.gui.DiamondRandomizerBagContainer;
import nl.requios.effortlessbuilding.gui.GoldenRandomizerBagContainer;
import nl.requios.effortlessbuilding.gui.RandomizerBagContainer;
import nl.requios.effortlessbuilding.item.DiamondRandomizerBagItem;
import nl.requios.effortlessbuilding.item.GoldenRandomizerBagItem;
import nl.requios.effortlessbuilding.item.PowerLevelItem;
import nl.requios.effortlessbuilding.item.RandomizerBagItem;
import nl.requios.effortlessbuilding.item.ReachUpgrade1Item;
import nl.requios.effortlessbuilding.item.ReachUpgrade2Item;
import nl.requios.effortlessbuilding.item.ReachUpgrade3Item;
import nl.requios.effortlessbuilding.item.SingleItemLootModifier;
import nl.requios.effortlessbuilding.network.PacketHandler;
import nl.requios.effortlessbuilding.proxy.ClientProxy;
import nl.requios.effortlessbuilding.proxy.ServerProxy;
import nl.requios.effortlessbuilding.systems.ItemUsageTracker;
import nl.requios.effortlessbuilding.systems.ServerBlockPlacer;
import nl.requios.effortlessbuilding.systems.UndoRedo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.function.Supplier;

@Mod(EffortlessBuilding.MODID)
public class EffortlessBuilding {

	public static final String MODID = "effortlessbuilding";
	public static final Logger logger = LogManager.getLogger();

	public static EffortlessBuilding instance;

	public static final ServerBlockPlacer SERVER_BLOCK_PLACER = new ServerBlockPlacer();
	public static final UndoRedo UNDO_REDO = new UndoRedo();
	public static final ItemUsageTracker ITEM_USAGE_TRACKER = new ItemUsageTracker();

	//Registration
	private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
	private static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(BuiltInRegistries.MENU, EffortlessBuilding.MODID);
	private static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIERS = DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, EffortlessBuilding.MODID);
	private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, EffortlessBuilding.MODID);

	public static final DeferredItem<RandomizerBagItem> RANDOMIZER_BAG_ITEM = ITEMS.register("randomizer_bag", RandomizerBagItem::new);
	public static final DeferredItem<GoldenRandomizerBagItem> GOLDEN_RANDOMIZER_BAG_ITEM = ITEMS.register("golden_randomizer_bag", GoldenRandomizerBagItem::new);
	public static final DeferredItem<DiamondRandomizerBagItem> DIAMOND_RANDOMIZER_BAG_ITEM = ITEMS.register("diamond_randomizer_bag", DiamondRandomizerBagItem::new);
	public static final DeferredItem<ReachUpgrade1Item> REACH_UPGRADE_1_ITEM = ITEMS.register("reach_upgrade1", ReachUpgrade1Item::new);
	public static final DeferredItem<ReachUpgrade2Item> REACH_UPGRADE_2_ITEM = ITEMS.register("reach_upgrade2", ReachUpgrade2Item::new);
	public static final DeferredItem<ReachUpgrade3Item> REACH_UPGRADE_3_ITEM = ITEMS.register("reach_upgrade3", ReachUpgrade3Item::new);
	public static final DeferredItem<PowerLevelItem> MUSCLES_ITEM = ITEMS.register("muscles", PowerLevelItem::new);
	public static final DeferredItem<PowerLevelItem> ELASTIC_HAND_ITEM = ITEMS.register("elastic_hand", PowerLevelItem::new);
	public static final DeferredItem<PowerLevelItem> BUILDING_TECHNIQUES_BOOK_ITEM = ITEMS.register("building_techniques_book", PowerLevelItem::new);

	public static final Supplier<MenuType<RandomizerBagContainer>> RANDOMIZER_BAG_CONTAINER = CONTAINERS.register("randomizer_bag", () -> registerContainer(RandomizerBagContainer::new));
	public static final Supplier<MenuType<GoldenRandomizerBagContainer>> GOLDEN_RANDOMIZER_BAG_CONTAINER = CONTAINERS.register("golden_randomizer_bag", () -> registerContainer(GoldenRandomizerBagContainer::new));
	public static final Supplier<MenuType<DiamondRandomizerBagContainer>> DIAMOND_RANDOMIZER_BAG_CONTAINER = CONTAINERS.register("diamond_randomizer_bag", () -> registerContainer(DiamondRandomizerBagContainer::new));

	public static final Supplier<MapCodec<SingleItemLootModifier>> SINGLE_ITEM_LOOT_MODIFIER = EffortlessBuilding.LOOT_MODIFIERS.register("single_item_loot_modifier", SingleItemLootModifier.CODEC);

	public static final Supplier<AttachmentType<PowerLevel>> POWER_LEVEL = ATTACHMENT_TYPES.register("power_level", () -> AttachmentType.serializable(PowerLevel::new).build());

	public EffortlessBuilding(IEventBus modEventBus, ModContainer container, Dist dist) {
		instance = this;

		IEventBus forgeEventBus = NeoForge.EVENT_BUS;

		modEventBus.addListener(EffortlessBuilding::setup);
		modEventBus.addListener(EffortlessBuilding::addTabContents);
		modEventBus.addListener(PacketHandler::setupPackets);

		ITEMS.register(modEventBus);
		CONTAINERS.register(modEventBus);

		LOOT_MODIFIERS.register(modEventBus);
		ATTACHMENT_TYPES.register(modEventBus);

		//Register config
		container.registerConfig(ModConfig.Type.COMMON, CommonConfig.spec);
		container.registerConfig(ModConfig.Type.SERVER, ServerConfig.spec);
		if (dist.isClient()) {
			container.registerConfig(ModConfig.Type.CLIENT, ClientConfig.spec);
			container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
			EffortlessBuildingClient.onConstructorClient(modEventBus, forgeEventBus);
		}
	}

	public static void setup(final FMLCommonSetupEvent event) {
		CompatHelper.setup();
	}

	public static void addTabContents(final BuildCreativeModeTabContentsEvent event) {
		if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
			List<ItemStack> stacks = ITEMS.getEntries().stream().map(reg -> new ItemStack(reg.get())).toList();
			event.acceptAll(stacks);
		}
	}

	public static <T extends AbstractContainerMenu> MenuType<T> registerContainer(IContainerFactory<T> fact) {
		MenuType<T> type = new MenuType<T>(fact, FeatureFlags.REGISTRY.allFlags());
		return type;
	}

	public static void log(String msg) {
		logger.info(msg);
	}

	public static void log(Player player, String msg) {
		log(player, msg, false);
	}

	public static void log(Player player, String msg, boolean actionBar) {
		player.displayClientMessage(Component.literal(msg), actionBar);
	}

	//Log with translation supported, call either on client or server (which then sends a message)
	public static void logTranslate(Player player, String prefix, String translationKey, String suffix, boolean actionBar) {
		if (FMLEnvironment.dist.isClient()) {
			ClientProxy.logTranslate(player, prefix, translationKey, suffix, actionBar);
		} else {
			ServerProxy.logTranslate(player, prefix, translationKey, suffix, actionBar);
		}
	}

	public static void logError(String msg) {
		logger.error(msg);
	}

	public static ResourceLocation asResource(String path) {
		return ResourceLocation.fromNamespaceAndPath(MODID, path);
	}
}
