package nl.requios.effortlessbuilding;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import nl.requios.effortlessbuilding.buildmode.BuildModeEnum;
import nl.requios.effortlessbuilding.buildmode.ModeOptions;
import nl.requios.effortlessbuilding.capability.CapabilityHandler;
import nl.requios.effortlessbuilding.gui.buildmode.PlayerSettingsGui;
import nl.requios.effortlessbuilding.gui.buildmode.RadialMenu;
import nl.requios.effortlessbuilding.gui.buildmodifier.ModifiersScreen;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(Dist.CLIENT)
public class ClientEvents {

    public static KeyMapping[] keyBindings;
    public static int ticksInGame = 0;
    private static int placeCooldown = 0;
    private static int breakCooldown = 0;

    //Mod Bus Events
    @EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static class ModBusEvents {

        @SubscribeEvent
        public static void registerKeyMappings(RegisterKeyMappingsEvent event) {

            keyBindings = new KeyMapping[6];

            keyBindings[0] = new KeyMapping("key.effortlessbuilding.mode.desc", KeyConflictContext.IN_GAME, InputConstants.getKey(GLFW.GLFW_KEY_LEFT_ALT, 0), "key.effortlessbuilding.category");
            keyBindings[1] = new KeyMapping("key.effortlessbuilding.hud.desc", KeyConflictContext.IN_GAME, InputConstants.getKey(GLFW.GLFW_KEY_KP_ADD, 0), "key.effortlessbuilding.category");
            keyBindings[2] = new KeyMapping("key.effortlessbuilding.undo.desc", KeyConflictContext.IN_GAME, KeyModifier.CONTROL, InputConstants.getKey(GLFW.GLFW_KEY_Z, 0), "key.effortlessbuilding.category");
            keyBindings[3] = new KeyMapping("key.effortlessbuilding.redo.desc", KeyConflictContext.IN_GAME, KeyModifier.CONTROL, InputConstants.getKey(GLFW.GLFW_KEY_Y, 0), "key.effortlessbuilding.category");
            keyBindings[4] = new KeyMapping("key.effortlessbuilding.previous_build_mode.desc", KeyConflictContext.IN_GAME, InputConstants.UNKNOWN, "key.effortlessbuilding.category");
            keyBindings[5] = new KeyMapping("key.effortlessbuilding.disable_build_mode_toggle.desc", KeyConflictContext.IN_GAME, InputConstants.UNKNOWN, "key.effortlessbuilding.category");

            for (KeyMapping keyBinding : keyBindings) {
                event.register(keyBinding);
            }
        }

//        @SubscribeEvent
//        public static void registerShaders(RegisterShadersEvent event) throws IOException {
//            event.registerShader(new ShaderInstance(event.getResourceManager(),
//                            new ResourceLocation(EffortlessBuilding.MODID, "dissolve"),
//                            DefaultVertexFormat.BLOCK),
//                    shaderInstance -> BuildRenderTypes.dissolveShaderInstance = shaderInstance);
//        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (!isGameActive()) return;

        if (event.phase == TickEvent.Phase.START) {

            EffortlessBuildingClient.BUILDER_CHAIN.onTick();

            onMouseInput();

            EffortlessBuildingClient.BLOCK_PREVIEWS.onTick();

        } else if (event.phase == TickEvent.Phase.END) {
            Screen gui = Minecraft.getInstance().screen;
            if (gui == null || !gui.isPauseScreen()) {
                ticksInGame++;
            }
        }

    }

    private static void onMouseInput() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;
        BuildModeEnum buildMode = EffortlessBuildingClient.BUILD_MODES.getBuildMode();

        if (mc.screen != null ||
            RadialMenu.instance.isVisible()) {
            return;
        }

        if (mc.options.keyUse.isDown()) {

            if (placeCooldown <= 0) {
                placeCooldown = 4;

                EffortlessBuildingClient.BUILDER_CHAIN.onRightClick();

            } else if (buildMode == BuildModeEnum.SINGLE) {
                placeCooldown--;
                if (ModeOptions.getBuildSpeed() == ModeOptions.ActionEnum.FAST_SPEED) placeCooldown = 0;
            }
        } else {
            placeCooldown = 0;
        }

        if (mc.options.keyAttack.isDown()) {

            //Break block in distance in creative (or survival if enabled in config)
            if (breakCooldown <= 0) {
                breakCooldown = 4;

                EffortlessBuildingClient.BUILDER_CHAIN.onLeftClick();

            } else if (buildMode == BuildModeEnum.SINGLE) {
                breakCooldown--;
                if (ModeOptions.getBuildSpeed() == ModeOptions.ActionEnum.FAST_SPEED) breakCooldown = 0;
            }

        } else {
            breakCooldown = 0;
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    public static void onKeyPress(InputEvent.Key event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null)
            return;

        //Radial menu
        if (keyBindings[0].isDown()) {
            if (!CapabilityHandler.isDisabled(player)) {
                if (!RadialMenu.instance.isVisible()) {
                    Minecraft.getInstance().setScreen(RadialMenu.instance);
                }
            } else {
                EffortlessBuilding.log(player, "Build modes are disabled until your reach has increased. Increase your reach with craftable reach upgrades.");
            }
        }

        //Show Modifier Settings GUI
        if (keyBindings[1].consumeClick()) {
            openModifierSettings();
        }

        //Undo (Ctrl+Z)
        if (keyBindings[2].consumeClick()) {
            ModeOptions.performAction(player, ModeOptions.ActionEnum.UNDO);
        }

        //Redo (Ctrl+Y)
        if (keyBindings[3].consumeClick()) {
            ModeOptions.performAction(player, ModeOptions.ActionEnum.REDO);
        }

        //Previous build mode
        if (keyBindings[4].consumeClick()) {
            ModeOptions.performAction(player, ModeOptions.ActionEnum.PREVIOUS_BUILD_MODE);
        }

        //Disable build mode toggle
        if (keyBindings[5].consumeClick()) {
            ModeOptions.performAction(player, ModeOptions.ActionEnum.DISABLE_BUILD_MODE_TOGGLE);
        }
    }

    public static void openModifierSettings() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        //Disabled if max reach is 0, might be set in the config that way.
        if (CapabilityHandler.isDisabled(player)) {
            EffortlessBuilding.log(player, "Build modifiers are disabled until your power level has increased. Increase your power level by consuming certain items.");
        } else {
            mc.setScreen(new ModifiersScreen());
        }
    }

    public static void openPlayerSettings() {
        Minecraft mc = Minecraft.getInstance();
        mc.setScreen(new PlayerSettingsGui());
    }

    @SubscribeEvent
    public static void onGuiOpen(ScreenEvent event) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            EffortlessBuildingClient.BUILDER_CHAIN.cancel();
        }
    }

    public static boolean isKeybindDown(int keybindIndex) {
        return InputConstants.isKeyDown(
                Minecraft.getInstance().getWindow().getWindow(),
                keyBindings[keybindIndex].getKey().getValue());
    }

    public static boolean isGameActive() {
        return !(Minecraft.getInstance().level == null || Minecraft.getInstance().player == null);
    }

}
