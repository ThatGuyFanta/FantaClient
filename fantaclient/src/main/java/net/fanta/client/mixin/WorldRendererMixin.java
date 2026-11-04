package net.fanta.client.mixin;

import net.fanta.client.module.NoWeatherModule;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    @Inject(method = "renderWeather", at = @At("HEAD"), cancellable = true, require = 0)
    private void fantaclient$noWeather(CallbackInfo ci) {
        try {
            if (NoWeatherModule.INSTANCE.isEnabled()) {
                ci.cancel();
            }
        } catch (Throwable ignored) {
        }
    }
}
