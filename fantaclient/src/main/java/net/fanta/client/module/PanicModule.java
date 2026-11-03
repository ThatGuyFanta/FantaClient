package net.fanta.client.module;

import net.minecraft.client.MinecraftClient;

/**
 * Fun / utility: instantly disables every module (panic button).
 */
public class PanicModule implements FantaModule {
    public static final PanicModule INSTANCE = new PanicModule();

    private boolean enabled = false;

    private PanicModule() {}

    @Override
    public String getName() { return "Panic"; }

    @Override
    public String getCategory() { return "Fun"; }

    @Override
    public String getDescription() { return "Instantly turns off ALL modules when enabled."; }

    @Override
    public boolean isEnabled() { return enabled; }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled) {
            // Turn everything else off
            for (FantaModule m : ModuleManager.getModules()) {
                if (m != this && m.isEnabled()) {
                    m.setEnabled(false);
                }
            }
            // Also turn ourselves back off so it acts as a one-shot button
            this.enabled = false;
            net.fanta.client.ConfigManager.save();
        }
    }

    public static void onTick(MinecraftClient client) {
        // one-shot behaviour handled in setEnabled
    }
}
