package net.fanta.client.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

/**
 * Fun module: rapidly toggles sneaking to create a "twerk" animation.
 */
public class TwerkModule implements FantaModule {
    public static final TwerkModule INSTANCE = new TwerkModule();

    private boolean enabled = false;
    private int tickCounter = 0;
    private boolean sneakState = false;

    private TwerkModule() {}

    @Override
    public String getName() { return "Twerk"; }

    @Override
    public String getCategory() { return "Fun"; }

    @Override
    public String getDescription() { return "Rapid crouch spam for the classic twerk dance."; }

    @Override
    public boolean isEnabled() { return enabled; }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            // Release sneak when disabled
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                client.options.sneakKey.setPressed(false);
            }
            sneakState = false;
            tickCounter = 0;
        }
    }

    public static void onTick(MinecraftClient client) {
        INSTANCE.tick(client);
    }

    private void tick(MinecraftClient client) {
        if (!enabled || client.player == null) return;

        tickCounter++;
        // Toggle sneak every 2-3 ticks for a snappy twerk
        if (tickCounter >= 2) {
            tickCounter = 0;
            sneakState = !sneakState;
            client.options.sneakKey.setPressed(sneakState);
        }
    }
}
