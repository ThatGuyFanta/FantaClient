package net.fanta.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fanta.client.gui.FantaClientScreen;
import net.fanta.client.module.*;
import net.fanta.client.render.EspRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FantaClientInitializer implements ClientModInitializer {
    public static final String MOD_ID = "fantaclient";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static KeyBinding openGuiKey;
    /** Debounce for raw GLFW fallback so we don't re-open every tick. */
    private static boolean rightShiftDown = false;

    @Override
    public void onInitializeClient() {
        LOGGER.info("FantaClient initializing for Minecraft 1.21.11");

        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.fantaclient.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.fantaclient"
        ));

        try {
            ConfigManager.load();
        } catch (Throwable t) {
            LOGGER.warn("Config load failed: {}", t.toString());
        }

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            handleGuiKey(client);

            if (client.player == null || client.world == null) {
                return;
            }

            safe(client, FreecamModule::onTick);
            safe(client, FullBrightModule::onTick);
            safe(client, AntiVanishModule::onTick);
            safe(client, StorageEspModule::onTick);
            safe(client, BlockEspModule::onTick);
            safe(client, ChunkAnalyzerModule::onTick);
            safe(client, NameTagsModule::onTick);
            safe(client, PlayerEspModule::onTick);
            safe(client, AutoSprintModule::onTick);
            safe(client, ZoomModule::onTick);
            safe(client, HudModule::onTick);
            safe(client, NoWeatherModule::onTick);
            safe(client, CustomCrosshairModule::onTick);
            safe(client, SchematicBuilderModule::onTick);
            safe(client, TwerkModule::onTick);
            safe(client, DerpModule::onTick);
            safe(client, SpinModule::onTick);
            safe(client, PanicModule::onTick);
            try { RainbowModule.INSTANCE.tickRainbow(); } catch (Throwable ignored) {}
            safe(client, SparkleTrailModule::onTick);
            safe(client, HeartAuraModule::onTick);
            safe(client, FlameHaloModule::onTick);
            safe(client, NoteTrailModule::onTick);
            safe(client, WingsModule::onTick);

            try { ConfigManager.tickAutoSave(); } catch (Throwable ignored) {}
        });

        WorldRenderEvents.AFTER_TRANSLUCENT.register(ctx -> {
            try { EspRenderer.render(ctx); } catch (Throwable ignored) {}
        });

        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            try {
                MinecraftClient mc = MinecraftClient.getInstance();
                if (mc == null || mc.player == null) return;
                HudModule.INSTANCE.render(context, mc);
                CustomCrosshairModule.INSTANCE.render(context, mc);
            } catch (Throwable ignored) {}
        });

        LOGGER.info("FantaClient loaded – {} modules registered", ModuleManager.getModules().size());
    }

    /**
     * Opens/closes the GUI in-game (worlds/servers) and on the title screen.
     * Uses both the registered KeyBinding and a raw GLFW edge-detect fallback,
     * because some servers/mods interfere with KeyBinding.wasPressed().
     */
    private static void handleGuiKey(MinecraftClient client) {
        try {
            boolean pressed = false;

            // Primary: Fabric/Minecraft keybinding
            while (openGuiKey.wasPressed()) {
                pressed = true;
            }

            // Fallback: raw Right Shift edge detect (works in worlds/servers)
            long handle = client.getWindow().getHandle();
            boolean down = GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
            if (down && !rightShiftDown) {
                pressed = true;
            }
            rightShiftDown = down;

            if (!pressed) return;

            // Don't steal Right Shift while typing in chat/sign/anvil/etc.
            if (client.currentScreen != null && !(client.currentScreen instanceof FantaClientScreen)) {
                // Allow opening only when no screen, or toggle when our screen is open
                return;
            }

            if (client.currentScreen instanceof FantaClientScreen) {
                client.currentScreen.close();
            } else {
                client.setScreen(new FantaClientScreen());
            }
        } catch (Throwable t) {
            LOGGER.warn("GUI key error: {}", t.toString());
        }
    }

    private static void safe(MinecraftClient client, ClientTick tick) {
        try {
            tick.run(client);
        } catch (Throwable ignored) {
        }
    }

    @FunctionalInterface
    private interface ClientTick {
        void run(MinecraftClient client);
    }
}
