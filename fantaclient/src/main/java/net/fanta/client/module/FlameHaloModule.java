package net.fanta.client.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;

/** Cosmetic: flame ring above the player's head. */
public class FlameHaloModule implements FantaModule {
    public static final FlameHaloModule INSTANCE = new FlameHaloModule();
    private boolean enabled = false;
    private int tick;

    private FlameHaloModule() {}

    @Override public String getName() { return "Flame Halo"; }
    @Override public String getCategory() { return "Accessories"; }
    @Override public String getDescription() { return "Flame particle halo above your head."; }
    @Override public boolean isEnabled() { return enabled; }
    @Override public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public static void onTick(MinecraftClient client) {
        if (!INSTANCE.enabled || client == null || client.player == null || client.world == null) return;
        INSTANCE.tick++;
        Vec3d pos = client.player.getEntityPos().add(0, 2.1, 0);
        double a = INSTANCE.tick * 0.25;
        double r = 0.45;
        client.world.addParticle(ParticleTypes.FLAME,
                pos.x + Math.cos(a) * r,
                pos.y,
                pos.z + Math.sin(a) * r,
                0, 0.01, 0);
        client.world.addParticle(ParticleTypes.FLAME,
                pos.x + Math.cos(a + Math.PI) * r,
                pos.y,
                pos.z + Math.sin(a + Math.PI) * r,
                0, 0.01, 0);
    }
}
