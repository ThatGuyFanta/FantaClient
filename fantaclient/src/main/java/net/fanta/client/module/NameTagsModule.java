package net.fanta.client.module;

import net.minecraft.client.MinecraftClient;

public class NameTagsModule implements FantaModule {
    public static final NameTagsModule INSTANCE = new NameTagsModule();

    private boolean enabled = false;
    private boolean showArmor = true;
    private boolean showHandItems = true;
    private boolean scaleWithDistance = true;

    private NameTagsModule() {}

    @Override
    public String getName() { return "Name Tags"; }

    @Override
    public String getCategory() { return "Render"; }

    @Override
    public String getDescription() { return "Enhanced player name tags with optional armor/item info."; }

    @Override
    public boolean isEnabled() { return enabled; }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isShowArmor() { return showArmor; }
    public void setShowArmor(boolean v) { showArmor = v; }

    public boolean isShowHandItems() { return showHandItems; }
    public void setShowHandItems(boolean v) { showHandItems = v; }

    public boolean isScaleWithDistance() { return scaleWithDistance; }
    public void setScaleWithDistance(boolean v) { scaleWithDistance = v; }

    public static void onTick(MinecraftClient client) {
        // Rendering handled via mixin / WorldRenderEvents if needed.
        // Name tags enhancement is primarily visual and applied in render path.
    }
}
