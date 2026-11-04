package net.fanta.client.module;

import net.fanta.client.render.EspRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Box;

/** Animated cosmetic wings rendered through the 1.21.11 world render context. */
public class WingsModule implements FantaModule {
    public static final WingsModule INSTANCE = new WingsModule();
    private boolean enabled = false;
    private float flap = 0f;

    private WingsModule() {}
    @Override public String getName() { return "Wings"; }
    @Override public String getCategory() { return "Accessories"; }
    @Override public String getDescription() { return "Cosmetic animated wings behind you."; }
    @Override public boolean isEnabled() { return enabled; }
    @Override public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public static void onTick(MinecraftClient client) {
        if (!INSTANCE.enabled || client.player == null) return;
        float speed = client.player.isOnGround() ? 0.25f : 0.55f;
        if (client.player.getVelocity().horizontalLengthSquared() > 0.01) speed += 0.2f;
        INSTANCE.flap += speed;
    }

    public void render(MatrixStack matrices, VertexConsumer lines, Vec3d cameraPos, float tickDelta) {
        if (!enabled || lines == null) return;
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null || client.options.getPerspective().isFirstPerson()) return;

        double x = MathHelper.lerp(tickDelta, player.lastX, player.getX());
        double y = MathHelper.lerp(tickDelta, player.lastY, player.getY());
        double z = MathHelper.lerp(tickDelta, player.lastZ, player.getZ());
        float yaw = MathHelper.lerp(tickDelta, player.lastYaw, player.getYaw());
        float flapAngle = MathHelper.sin(flap) * 35f;
        float[] rgb = RainbowModule.INSTANCE.isEnabled() ? RainbowModule.INSTANCE.getRgb() : new float[]{1.0f, 0.55f, 0.15f};

        matrices.push();
        matrices.translate(x - cameraPos.x, y - cameraPos.y + 1.1, z - cameraPos.z);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-yaw));
        matrices.translate(0, 0, 0.15);
        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(115 + flapAngle));
        drawWing(matrices, lines, rgb);
        matrices.pop();
        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-115 - flapAngle));
        drawWing(matrices, lines, rgb);
        matrices.pop();
        matrices.pop();
    }

    private void drawWing(MatrixStack matrices, VertexConsumer lines, float[] rgb) {
        for (int i = 0; i < 5; i++) {
            double x0 = i * 0.22;
            double x1 = x0 + 0.2;
            double y0 = -0.35 + i * 0.05;
            double y1 = 0.45 - i * 0.08;
            EspRenderer.outline(matrices, lines, new Box(x0, y0, -0.02, x1, y1, 0.02), rgb, 0.9f);
        }
    }
}
