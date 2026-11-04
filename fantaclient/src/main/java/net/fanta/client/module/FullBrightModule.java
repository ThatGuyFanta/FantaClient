package net.fanta.client.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.SimpleOption;

/**
 * FullBright via gamma option (reliable) + soft lightmap mixin as backup.
 */
public class FullBrightModule implements FantaModule {
    public static final FullBrightModule INSTANCE = new FullBrightModule();

    private boolean enabled = false;
    private double previousGamma = 1.0;
    private boolean stored = false;

    private FullBrightModule() {}

    @Override
    public String getName() { return "FullBright"; }

    @Override
    public String getCategory() { return "Render"; }

    @Override
    public String getDescription() { return "Brightens the world (gamma). Works without fragile lightmap injects."; }

    @Override
    public boolean isEnabled() { return enabled; }

    @Override
    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;
        this.enabled = enabled;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.options == null) return;
        try {
            SimpleOption<Double> gamma = client.options.getGamma();
            if (enabled) {
                if (!stored) {
                    previousGamma = gamma.getValue();
                    stored = true;
                }
                gamma.setValue(16.0); // max brightness
            } else if (stored) {
                gamma.setValue(previousGamma);
                stored = false;
            }
        } catch (Throwable ignored) {
        }
    }

    public static void onTick(MinecraftClient client) {
        if (!INSTANCE.enabled || client == null || client.options == null) return;
        try {
            // Keep gamma forced while enabled (other mods may overwrite)
            if (client.options.getGamma().getValue() < 15.0) {
                client.options.getGamma().setValue(16.0);
            }
        } catch (Throwable ignored) {
        }
    }
}
