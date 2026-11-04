package net.fanta.client.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

import java.util.Random;

/**
 * Fun module: makes the player head look around randomly / derp.
 */
public class DerpModule implements FantaModule {
    public static final DerpModule INSTANCE = new DerpModule();

    private boolean enabled = false;
    private final Random random = new Random();
    private float targetYaw;
    private float targetPitch;
    private int changeTicks = 0;

    private DerpModule() {}

    @Override
    public String getName() { return "Derp"; }

    @Override
    public String getCategory() { return "Fun"; }

    @Override
    public String getDescription() { return "Random head twitching and looking around."; }

    @Override
    public boolean isEnabled() { return enabled; }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled) {
            changeTicks = 0;
            pickNewTarget();
        }
    }

    public static void onTick(MinecraftClient client) {
        INSTANCE.tick(client);
    }

    private void tick(MinecraftClient client) {
        if (!enabled || client.player == null) return;
        ClientPlayerEntity player = client.player;

        changeTicks--;
        if (changeTicks <= 0) {
            pickNewTarget();
        }

        // Smooth-ish lerp toward random look direction
        float yaw = player.getYaw();
        float pitch = player.getPitch();

        float newYaw = yaw + (targetYaw - yaw) * 0.35f;
        float newPitch = pitch + (targetPitch - pitch) * 0.35f;

        // Clamp pitch
        newPitch = Math.max(-90f, Math.min(90f, newPitch));

        player.setYaw(newYaw);
        player.setPitch(newPitch);
    }

    private void pickNewTarget() {
        targetYaw = random.nextFloat() * 360f - 180f;
        targetPitch = random.nextFloat() * 160f - 80f;
        changeTicks = 4 + random.nextInt(10);
    }
}
