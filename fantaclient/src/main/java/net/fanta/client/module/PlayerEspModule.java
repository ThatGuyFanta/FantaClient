package net.fanta.client.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PlayerEspModule implements FantaModule {
    public static final PlayerEspModule INSTANCE = new PlayerEspModule();

    private boolean enabled = false;
    private boolean drawBox = true;
    private boolean drawTracers = false;
    private final List<PlayerEntity> targets = new CopyOnWriteArrayList<>();

    private PlayerEspModule() {}

    @Override
    public String getName() { return "Player ESP"; }

    @Override
    public String getCategory() { return "Render"; }

    @Override
    public String getDescription() { return "Highlight other players with boxes and optional tracers."; }

    @Override
    public boolean isEnabled() { return enabled; }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) targets.clear();
    }

    public boolean isDrawBox() { return drawBox; }
    public void setDrawBox(boolean v) { drawBox = v; }
    public void toggleBox() { drawBox = !drawBox; }

    public boolean isDrawTracers() { return drawTracers; }
    public void setDrawTracers(boolean v) { drawTracers = v; }
    public void toggleTracers() { drawTracers = !drawTracers; }

    public List<PlayerEntity> getTargets() {
        return targets;
    }

    public static void onTick(MinecraftClient client) {
        INSTANCE.tick(client);
    }

    private void tick(MinecraftClient client) {
        if (!enabled || client.world == null || client.player == null) {
            targets.clear();
            return;
        }
        targets.clear();
        for (PlayerEntity player : client.world.getPlayers()) {
            if (player == client.player || !player.isAlive()) continue;
            targets.add(player);
        }
    }

    public static Box getInterpolatedBox(PlayerEntity player, float tickDelta) {
        double x = player.lastX + (player.getX() - player.lastX) * tickDelta;
        double y = player.lastY + (player.getY() - player.lastY) * tickDelta;
        double z = player.lastZ + (player.getZ() - player.lastZ) * tickDelta;
        float w = player.getWidth() / 2f;
        float h = player.getHeight();
        return new Box(x - w, y, z - w, x + w, y + h, z + w);
    }
}
