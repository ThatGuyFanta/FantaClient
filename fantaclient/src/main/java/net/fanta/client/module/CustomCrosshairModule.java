package net.fanta.client.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/**
 * Replaces the default crosshair with a simple Fanta-bottle silhouette.
 */
public class CustomCrosshairModule implements FantaModule {
    public static final CustomCrosshairModule INSTANCE = new CustomCrosshairModule();

    private boolean enabled = false;
    private int color = 0xFFFF6A00; // Fanta orange
    private int outline = 0xFF222222;

    private CustomCrosshairModule() {}

    @Override
    public String getName() { return "Fanta Crosshair"; }

    @Override
    public String getCategory() { return "Render"; }

    @Override
    public String getDescription() { return "Custom crosshair shaped like a Fanta bottle."; }

    @Override
    public boolean isEnabled() { return enabled; }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public static void onTick(MinecraftClient client) {
        // nothing needed per tick
    }

    /**
     * Draw the bottle crosshair centered on screen.
     * Called from HUD render when the module is enabled.
     */
    public void render(DrawContext context, MinecraftClient client) {
        if (!enabled || client.options.hudHidden) return;
        // Don't draw if in a screen / debug crosshair etc.
        if (client.currentScreen != null) return;

        int cx = client.getWindow().getScaledWidth() / 2;
        int cy = client.getWindow().getScaledHeight() / 2;

        // Use rainbow color if Rainbow module is on
        int fill = color;
        if (RainbowModule.INSTANCE.isEnabled()) {
            fill = RainbowModule.INSTANCE.getColor(1f);
        }

        drawFantaBottle(context, cx, cy, fill, outline);
    }

    /**
     * Simple pixel-style Fanta bottle centered at (cx, cy).
     * Scale is small so it works as a crosshair.
     */
    private void drawFantaBottle(DrawContext ctx, int cx, int cy, int fill, int outline) {
        // Geometry (relative to center) – small bottle ~11px tall
        // Cap / top
        fillRect(ctx, cx - 1, cy - 8, 3, 2, outline);          // cap outline-ish
        fillRect(ctx, cx - 1, cy - 7, 3, 1, fill);

        // Neck
        fillRect(ctx, cx - 1, cy - 6, 3, 3, fill);
        // neck outline sides
        fillRect(ctx, cx - 2, cy - 6, 1, 3, outline);
        fillRect(ctx, cx + 2, cy - 6, 1, 3, outline);

        // Shoulder / upper body
        fillRect(ctx, cx - 3, cy - 3, 7, 2, fill);
        fillRect(ctx, cx - 4, cy - 3, 1, 2, outline);
        fillRect(ctx, cx + 3, cy - 3, 1, 2, outline);

        // Main body
        fillRect(ctx, cx - 4, cy - 1, 9, 7, fill);
        // body side outlines
        fillRect(ctx, cx - 5, cy - 1, 1, 7, outline);
        fillRect(ctx, cx + 4, cy - 1, 1, 7, outline);

        // Bottom
        fillRect(ctx, cx - 4, cy + 6, 9, 1, outline);
        fillRect(ctx, cx - 3, cy + 6, 7, 1, fill);

        // Little label stripe (white-ish)
        fillRect(ctx, cx - 3, cy + 1, 7, 2, 0xCCFFFFFF);

        // Tiny orange dot in center of label (aim point)
        fillRect(ctx, cx, cy + 1, 1, 1, fill);
    }

    private void fillRect(DrawContext ctx, int x, int y, int w, int h, int argb) {
        ctx.fill(x, y, x + w, y + h, argb);
    }
}
