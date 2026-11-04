package net.fanta.client.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;

/** Cosmetic: note particles while moving (music vibe). */
public class NoteTrailModule implements FantaModule {
    public static final NoteTrailModule INSTANCE = new NoteTrailModule();
    private boolean enabled = false;

    private NoteTrailModule() {}

    @Override public String getName() { return "Note Trail"; }
    @Override public String getCategory() { return "Accessories"; }
    @Override public String getDescription() { return "Music-note particles while walking."; }
    @Override public boolean isEnabled() { return enabled; }
    @Override public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public static void onTick(MinecraftClient client) {
        if (!INSTANCE.enabled || client == null || client.player == null || client.world == null) return;
        if (client.player.getVelocity().horizontalLengthSquared() < 0.001) return;
        if (client.world.getTime() % 3 != 0) return;
        Vec3d pos = client.player.getEntityPos().add(0, 0.8, 0);
        client.world.addParticleClient(ParticleTypes.NOTE,
                pos.x, pos.y, pos.z,
                client.world.random.nextDouble(), 0, 0);
    }
}
