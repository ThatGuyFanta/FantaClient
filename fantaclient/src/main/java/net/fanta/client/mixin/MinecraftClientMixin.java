package net.fanta.client.mixin;

import net.fanta.client.module.FreecamModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Inject(method = "getCameraEntity", at = @At("RETURN"), cancellable = true, require = 0)
    private void fantaclient$getCameraEntity(CallbackInfoReturnable<Entity> cir) {
        try {
            // Keep vanilla camera entity; freecam only moves Camera pos.
            // This hook reserved for future use / compatibility.
        } catch (Throwable ignored) {
        }
    }
}
