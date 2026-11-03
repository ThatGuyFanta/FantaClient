package net.fanta.client.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

/**
 * Fun module: continuously spins the player yaw.
 */
public class SpinModule implements FantaModule {
    public static final SpinModule INSTANCE = new SpinModule();

    private boolean enabled = false;
    private float speed = 25f; // degrees per tick

    private SpinModule() {}

    @Override
    public String getName() { return "Spin"; }

    @Override
    public String getCategory() { return "Fun"; }

    @Override
    public String getDescription() { return "Spin your character around really fast."; }

    @Override
    public boolean isEnabled() { return enabled; }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public float getSpeed() { return speed; }
    public void setSpeed(float speed) { this.speed = speed; }

    public static void onTick(MinecraftClient client) {
        INSTANCE.tick(client);
    }

    private void tick(MinecraftClient client) {
        if (!enabled || client.player == null) return;
        ClientPlayerEntity player = client.player;
        player.setYaw(player.getYaw() + speed);
    }
}
