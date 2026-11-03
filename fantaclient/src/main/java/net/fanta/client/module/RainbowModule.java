package net.fanta.client.module;

/**
 * Fun module: makes ESP / HUD colors cycle through rainbow.
 * Other renderers can query getColor() when this is enabled.
 */
public class RainbowModule implements FantaModule {
    public static final RainbowModule INSTANCE = new RainbowModule();

    private boolean enabled = false;
    private float hue = 0f;

    private RainbowModule() {}

    @Override
    public String getName() { return "Rainbow"; }

    @Override
    public String getCategory() { return "Fun"; }

    @Override
    public String getDescription() { return "Rainbow colors on ESP boxes and HUD accent."; }

    @Override
    public boolean isEnabled() { return enabled; }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /** Call once per frame/tick to advance the rainbow. */
    public void tickRainbow() {
        if (!enabled) return;
        hue += 0.012f;
        if (hue > 1f) hue -= 1f;
    }

    /**
     * @return ARGB color int cycling through HSV rainbow
     */
    public int getColor(float alpha) {
        if (!enabled) return 0xFF00E5C0; // default accent
        int rgb = java.awt.Color.HSBtoRGB(hue, 0.9f, 1.0f);
        int a = Math.min(255, Math.max(0, (int) (alpha * 255)));
        return (a << 24) | (rgb & 0x00FFFFFF);
    }

    public float[] getRgb() {
        if (!enabled) return new float[]{0f, 0.9f, 0.75f};
        int rgb = java.awt.Color.HSBtoRGB(hue, 0.9f, 1.0f);
        float r = ((rgb >> 16) & 0xFF) / 255f;
        float g = ((rgb >> 8) & 0xFF) / 255f;
        float b = (rgb & 0xFF) / 255f;
        return new float[]{r, g, b};
    }
}
