package net.fanta.client.mixin;

import net.fanta.client.module.FullBrightModule;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Soft fullbright. require=0 avoids crash if lightmap method signature changes.
 */
@Mixin(LightmapTextureManager.class)
public class LightmapTextureManagerMixin {

    @ModifyVariable(
            method = "update",
            at = @At("STORE"),
            ordinal = 0,
            require = 0
    )
    private float fantaclient$fullbright(float original) {
        try {
            if (FullBrightModule.INSTANCE.isEnabled()) {
                return 1.0f;
            }
        } catch (Throwable ignored) {
        }
        return original;
    }
}
