package net.fanta.client.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;

/**
 * Detached camera. Player body stays put; camera position is driven by mixin when available.
 */
public class FreecamModule implements FantaModule {
    public static final FreecamModule INSTANCE = new FreecamModule();

    private boolean enabled = false;
    private Vec3d freecamPos = Vec3d.ZERO;
    private float freecamYaw;
    private float freecamPitch;

    private boolean storedAllowFlying = false;
    private boolean storedFlying = false;
    private boolean hasStoredAbilities = false;

    private FreecamModule() {}

    @Override
    public String getName() { return "Freecam"; }

    @Override
    public String getCategory() { return "Movement"; }

    @Override
    public String getDescription() { return "Detach camera from player and fly freely (client-side)."; }

    @Override
    public boolean isEnabled() { return enabled; }

    @Override
    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (enabled) {
                if (client != null && client.player != null) onEnable(client);
                this.enabled = true;
            } else {
                if (client != null && client.player != null) onDisable(client);
                this.enabled = false;
            }
        } catch (Throwable t) {
            this.enabled = false;
            try {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client != null && client.player != null) onDisable(client);
            } catch (Throwable ignored) {}
        }
    }

    private void onEnable(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null) {
            this.enabled = false;
            return;
        }
        freecamPos = player.getEntityPos().add(0, player.getEyeHeight(player.getPose()), 0);
        freecamYaw = player.getYaw();
        freecamPitch = player.getPitch();

        storedAllowFlying = player.getAbilities().allowFlying;
        storedFlying = player.getAbilities().flying;
        hasStoredAbilities = true;

        // Local only — helps when moving camera with keys; body still stays
        player.getAbilities().allowFlying = true;
        player.getAbilities().flying = true;
    }

    private void onDisable(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null) return;
        if (hasStoredAbilities) {
            // Restore exact prior state (creative/spectator keep allowFlying)
            if (player.isCreative() || player.isSpectator()) {
                player.getAbilities().allowFlying = true;
                player.getAbilities().flying = storedFlying;
            } else {
                player.getAbilities().allowFlying = storedAllowFlying;
                player.getAbilities().flying = storedFlying && storedAllowFlying;
            }
            player.sendAbilitiesUpdate();
            hasStoredAbilities = false;
        }
    }

    public static void onTick(MinecraftClient client) {
        INSTANCE.tick(client);
    }

    private void tick(MinecraftClient client) {
        if (!enabled || client.player == null) return;
        ClientPlayerEntity player = client.player;

        // Cancel body movement while freecam is on so the player doesn't walk
        player.setVelocity(0, player.getVelocity().y > 0 ? 0 : player.getVelocity().y, 0);
        if (player.isOnGround()) {
            player.setVelocity(Vec3d.ZERO);
        }

        float speed = client.options.sprintKey.isPressed() ? 0.9f : 0.4f;
        // Horizontal look from freecam yaw; vertical from pitch
        float yawRad = (float) Math.toRadians(freecamYaw);
        float pitchRad = (float) Math.toRadians(freecamPitch);
        Vec3d forward = new Vec3d(
                -Math.sin(yawRad) * Math.cos(pitchRad),
                -Math.sin(pitchRad),
                Math.cos(yawRad) * Math.cos(pitchRad)
        ).normalize();
        Vec3d right = new Vec3d(-Math.cos(yawRad), 0, -Math.sin(yawRad)).normalize().multiply(-1);

        Vec3d motion = Vec3d.ZERO;
        if (client.options.forwardKey.isPressed()) motion = motion.add(forward.multiply(speed));
        if (client.options.backKey.isPressed()) motion = motion.subtract(forward.multiply(speed));
        if (client.options.leftKey.isPressed()) motion = motion.subtract(right.multiply(speed));
        if (client.options.rightKey.isPressed()) motion = motion.add(right.multiply(speed));
        if (client.options.jumpKey.isPressed()) motion = motion.add(0, speed, 0);
        if (client.options.sneakKey.isPressed()) motion = motion.add(0, -speed, 0);

        freecamPos = freecamPos.add(motion);

        // Track mouse look from player (vanilla still rotates the player)
        freecamYaw = player.getYaw();
        freecamPitch = player.getPitch();
    }

    public Vec3d getFreecamPos() {
        return freecamPos != null ? freecamPos : Vec3d.ZERO;
    }

    public float getFreecamYaw() { return freecamYaw; }
    public float getFreecamPitch() { return freecamPitch; }
}
