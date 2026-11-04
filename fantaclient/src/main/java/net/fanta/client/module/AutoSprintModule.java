package net.fanta.client.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

public class AutoSprintModule implements FantaModule {
    public static final AutoSprintModule INSTANCE = new AutoSprintModule();

    private boolean enabled = false;

    private AutoSprintModule() {}

    @Override
    public String getName() { return "AutoSprint"; }

    @Override
    public String getCategory() { return "Movement"; }

    @Override
    public String getDescription() { return "Automatically sprint when moving forward."; }

    @Override
    public boolean isEnabled() { return enabled; }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public static void onTick(MinecraftClient client) {
        INSTANCE.tick(client);
    }

    private void tick(MinecraftClient client) {
        if (!enabled || client.player == null) return;
        ClientPlayerEntity player = client.player;

        if (player.input.hasForwardMovement()
                && !player.isSneaking()
                && !player.isUsingItem()
                && !player.horizontalCollision
                && player.getHungerManager().getFoodLevel() > 6) {
            player.setSprinting(true);
        }
    }
}
