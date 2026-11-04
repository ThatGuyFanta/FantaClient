package net.fanta.client.module;

import net.minecraft.client.MinecraftClient;

/**
 * Client-side weather suppression (visual only).
 * Actual rain particles / sky are controlled via mixin.
 */
public class NoWeatherModule implements FantaModule {
    public static final NoWeatherModule INSTANCE = new NoWeatherModule();

    private boolean enabled = false;

    private NoWeatherModule() {}

    @Override
    public String getName() { return "NoWeather"; }

    @Override
    public String getCategory() { return "Render"; }

    @Override
    public String getDescription() { return "Removes rain and thunder visuals client-side."; }

    @Override
    public boolean isEnabled() { return enabled; }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public static void onTick(MinecraftClient client) {
        // Handled primarily by mixin
    }
}
