package net.fanta.client.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class HudModule implements FantaModule {
    public static final HudModule INSTANCE = new HudModule();

    private boolean enabled = true; // on by default
    private boolean showCoords = true;
    private boolean showFps = true;
    private boolean showModules = true;

    private HudModule() {}

    @Override
    public String getName() { return "HUD"; }

    @Override
    public String getCategory() { return "Render"; }

    @Override
    public String getDescription() { return "On-screen info: coordinates, FPS and enabled modules."; }

    @Override
    public boolean isEnabled() { return enabled; }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isShowCoords() { return showCoords; }
    public void setShowCoords(boolean v) { showCoords = v; }

    public boolean isShowFps() { return showFps; }
    public void setShowFps(boolean v) { showFps = v; }

    public boolean isShowModules() { return showModules; }
    public void setShowModules(boolean v) { showModules = v; }

    public static void onTick(MinecraftClient client) {
        // nothing per-tick needed
    }

    /**
     * Called from a HUD render mixin / event.
     */
    public void render(DrawContext context, MinecraftClient client) {
        if (!enabled || client.player == null) return;

        int x = 4;
        int y = 4;
        int color = 0xFF00E5C0;
        int secondary = 0xFFAAAAAA;

        if (showFps) {
            int fps = client.getCurrentFps();
            context.drawText(client.textRenderer, "FPS: " + fps, x, y, color, true);
            y += 12;
        }

        if (showCoords) {
            BlockPos pos = client.player.getBlockPos();
            String coords = String.format("XYZ: %d / %d / %d", pos.getX(), pos.getY(), pos.getZ());
            context.drawText(client.textRenderer, coords, x, y, 0xFFFFFFFF, true);
            y += 12;

            String nether = String.format("Nether: %d / %d / %d",
                    pos.getX() / 8, pos.getY(), pos.getZ() / 8);
            context.drawText(client.textRenderer, nether, x, y, secondary, true);
            y += 14;
        }

        if (showModules) {
            context.drawText(client.textRenderer, "FantaClient", x, y, color, true);
            y += 12;
            for (FantaModule m : ModuleManager.getModules()) {
                if (m.isEnabled() && m != this) {
                    context.drawText(client.textRenderer, "> " + m.getName(), x, y, 0xFFCCFFEE, true);
                    y += 11;
                }
            }
        }
    }
}
