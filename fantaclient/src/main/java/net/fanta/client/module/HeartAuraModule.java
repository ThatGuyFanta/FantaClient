package net.fanta.client.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;

/** Cosmetic: hearts around the player. */
public class HeartAuraModule implements FantaModule {
    public static final HeartAuraModule INSTANCE = new HeartAuraModule();
    private boolean enabled = false;
    private int tick;

    private HeartAuraModule() {}

    @Override public String getName() { return "Heart Aura"; }
    @Override public String getCategory() { return "Accessories"; }
    @Override public String getDescription() { return "Spawns heart particles around you."; }
    @Override public boolean isEnabled() { return enabled; }
    @Override public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public static void onTick(MinecraftClient client) {
        if (!INSTANCE.enabled || client == null || client.player == null || client.world == null) return;
        INSTANCE.tick++;
        if (INSTANCE.tick % 8 != 0) return;
        Vec3d pos = client.player.getEntityPos().add(0, 1.0, 0);
        double a = INSTANCE.tick * 0.3;
        double r = 0.8;
        client.world.addParticleClient(ParticleTypes.HEART,
                pos.x + Math.cos(a) * r,
                pos.y + 0.2,
                pos.z + Math.sin(a) * r,
                0, 0.05, 0);
    }
}
