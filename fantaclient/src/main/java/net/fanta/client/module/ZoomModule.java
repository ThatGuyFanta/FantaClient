package net.fanta.client.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.SimpleOption;

public class ZoomModule implements FantaModule {
    public static final ZoomModule INSTANCE = new ZoomModule();

    private boolean enabled = false;
    private boolean active = false; // currently zooming
    private double previousFov = 70.0;
    private double zoomFov = 20.0;

    private ZoomModule() {}

    @Override
    public String getName() { return "Zoom"; }

    @Override
    public String getCategory() { return "Render"; }

    @Override
    public String getDescription() { return "Hold the zoom key (C by default via mixin/opt) or toggle for closer FOV."; }

    @Override
    public boolean isEnabled() { return enabled; }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            restoreFov();
            active = false;
        }
    }

    public boolean isActive() {
        return enabled && active;
    }

    public void setActive(boolean active) {
        if (!enabled) return;
        if (this.active == active) return;
        this.active = active;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options == null) return;
        SimpleOption<Double> fov = client.options.getFov();
        if (active) {
            previousFov = fov.getValue();
            fov.setValue(zoomFov);
        } else {
            fov.setValue(previousFov);
        }
    }

    private void restoreFov() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options != null) {
            client.options.getFov().setValue(previousFov > 0 ? previousFov : 70.0);
        }
    }

    public static void onTick(MinecraftClient client) {
        // Toggle-style: when module is on, keep zoomed. Can be improved with a key later.
        if (INSTANCE.enabled && !INSTANCE.active) {
            INSTANCE.setActive(true);
        } else if (!INSTANCE.enabled && INSTANCE.active) {
            INSTANCE.setActive(false);
        }
    }
}
