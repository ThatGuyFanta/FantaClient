package net.fanta.client.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

/**
 * Cosmetic wings behind the local player (third person).
 * Uses WorldRenderer.drawBox segments instead of removed BufferRenderer API.
 */
public class WingsModule implements FantaModule {
    public static final WingsModule INSTANCE = new WingsModule();

    private boolean enabled = false;
    private float flap = 0f;

    private WingsModule() {}

    @Override
    public String getName() { return "Wings"; }

    @Override
    public String getCategory() { return "Accessories"; }

    @Override
    public String getDescription() { return "Cosmetic animated wings behind you."; }

    @Override
    public boolean isEnabled() { return enabled; }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public static void onTick(MinecraftClient client) {
        if (!INSTANCE.enabled || client.player == null) return;
        float speed = client.player.isOnGround() ? 0.25f : 0.55f;
        if (client.player.getVelocity().horizontalLengthSquared() > 0.01) {
            speed += 0.2f;
        }
        INSTANCE.flap += speed;
        // Light particle outline so wings still show if line ESP path fails (Sodium/Iris)
        try {
            if (client.world != null && client.options.getPerspective().isFirstPerson() == false) {
                var pos = client.player.getEntityPos().add(0, 1.2, 0);
                double a = INSTANCE.flap * 0.4;
                client.world.addParticle(net.minecraft.particle.ParticleTypes.END_ROD,
                        pos.x + Math.cos(a) * 0.6, pos.y, pos.z + Math.sin(a) * 0.6, 0, 0, 0);
            }
        } catch (Throwable ignored) {}
    }

    public void render(MatrixStack matrices, VertexConsumerProvider consumers, Vec3d cameraPos, float tickDelta) {
        if (!enabled || consumers == null) return;
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null) return;
        if (client.options.getPerspective().isFirstPerson()) return;

        double x = MathHelper.lerp(tickDelta, player.lastX, player.getX());
        double y = MathHelper.lerp(tickDelta, player.lastY, player.getY());
        double z = MathHelper.lerp(tickDelta, player.lastZ, player.getZ());
        float yaw = MathHelper.lerp(tickDelta, player.lastYaw, player.getYaw());

        float flapAngle = MathHelper.sin(flap) * 35f;
        float baseSpread = 25f;

        float[] rgb = RainbowModule.INSTANCE.isEnabled()
                ? RainbowModule.INSTANCE.getRgb()
                : new float[]{1.0f, 0.55f, 0.15f};

        VertexConsumer lines = consumers.getBuffer(RenderLayer.getLines());

        matrices.push();
        matrices.translate(x - cameraPos.x, y - cameraPos.y + 1.1, z - cameraPos.z);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-yaw));
        matrices.translate(0, 0, 0.15);

        // Left wing panel (box approximation)
        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90 + baseSpread + flapAngle));
        drawWingBoxes(matrices, lines, rgb[0], rgb[1], rgb[2], 0.9f);
        matrices.pop();

        // Right wing
        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90 - baseSpread - flapAngle));
        drawWingBoxes(matrices, lines, rgb[0], rgb[1], rgb[2], 0.9f);
        matrices.pop();

        matrices.pop();
    }

    private void drawWingBoxes(MatrixStack matrices, VertexConsumer lines, float r, float g, float b, float a) {
        // Several thin boxes along the wing length
        for (int i = 0; i < 5; i++) {
            double x0 = i * 0.22;
            double x1 = x0 + 0.2;
            double y0 = -0.35 + i * 0.05;
            double y1 = 0.45 - i * 0.08;
            WorldRenderer.drawBox(matrices, lines, x0, y0, -0.02, x1, y1, 0.02, r, g, b, a);
        }
    }
}
