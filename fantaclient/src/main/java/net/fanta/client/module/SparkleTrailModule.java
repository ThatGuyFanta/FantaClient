package net.fanta.client.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;

/** Cosmetic: sparkle particle trail while moving. */
public class SparkleTrailModule implements FantaModule {
    public static final SparkleTrailModule INSTANCE = new SparkleTrailModule();
    private boolean enabled = false;

    private SparkleTrailModule() {}

    @Override public String getName() { return "Sparkle Trail"; }
    @Override public String getCategory() { return "Accessories"; }
    @Override public String getDescription() { return "Leaves a sparkle particle trail while you move."; }
    @Override public boolean isEnabled() { return enabled; }
    @Override public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public static void onTick(MinecraftClient client) {
        if (!INSTANCE.enabled || client == null || client.player == null || client.world == null) return;
        if (client.player.getVelocity().horizontalLengthSquared() < 0.001) return;
        if (client.world.getTime() % 2 != 0) return;
        Vec3d pos = client.player.getEntityPos().add(0, 0.1, 0);
        for (int i = 0; i < 1; i++) {
            double ox = (client.world.random.nextDouble() - 0.5) * 0.4;
            double oz = (client.world.random.nextDouble() - 0.5) * 0.4;
            client.world.addParticleClient(ParticleTypes.END_ROD, pos.x + ox, pos.y, pos.z + oz, 0, 0.02, 0);
        }
    }
}
