package net.fanta.client.mixin;

import net.fanta.client.module.FreecamModule;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Redirect(
            method = "renderWorld",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/Camera;update(Lnet/minecraft/world/BlockView;Lnet/minecraft/entity/Entity;ZZF)V"
            ),
            require = 0
    )
    private void fantaclient$freecamCamera(Camera camera, BlockView area, Entity focusedEntity,
                                           boolean thirdPerson, boolean inverseView, float tickDelta) {
        try {
            camera.update(area, focusedEntity, thirdPerson, inverseView, tickDelta);
            if (FreecamModule.INSTANCE.isEnabled()) {
                Vec3d pos = FreecamModule.INSTANCE.getFreecamPos();
                if (pos != null) {
                    camera.setPos(pos.x, pos.y, pos.z);
                }
            }
        } catch (Throwable ignored) {
        }
    }
}
