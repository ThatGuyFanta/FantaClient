package net.fanta.client.render;

import net.fanta.client.module.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShapes;

/** World ESP rendering for Minecraft 1.21.11's extraction/drawing pipeline. */
public final class EspRenderer {
    private EspRenderer() {}

    public static void render(net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext context) {
        MatrixStack matrices = context.matrices();
        VertexConsumerProvider consumers = context.consumers();
        MinecraftClient client = MinecraftClient.getInstance();
        if (matrices == null || consumers == null || client == null || client.world == null || client.player == null) return;

        try {
            Vec3d camera = client.gameRenderer.getCamera().getPos();
            float tickDelta = client.getRenderTickCounter().getTickDelta(false);
            matrices.push();
            matrices.translate(-camera.x, -camera.y, -camera.z);
            VertexConsumer lines = consumers.getBuffer(RenderLayers.LINES);

            WingsModule.INSTANCE.render(matrices, lines, camera, tickDelta);

            if (BlockEspModule.INSTANCE.isEnabled()) {
                float[] c = colorOrRainbow(0.0f, 0.9f, 1.0f);
                for (BlockPos pos : BlockEspModule.INSTANCE.getMatchingBlocks())
                    outline(matrices, lines, new Box(pos).expand(0.01), c, 0.85f);
            }

            if (StorageEspModule.INSTANCE.isEnabled()) {
                float[] c = colorOrRainbow(1.0f, 0.7f, 0.0f);
                for (BlockPos pos : StorageEspModule.INSTANCE.getStorages())
                    outline(matrices, lines, new Box(pos).expand(0.01), c, 0.85f);
            }

            if (PlayerEspModule.INSTANCE.isEnabled() && PlayerEspModule.INSTANCE.isDrawBox()) {
                float[] c = colorOrRainbow(1.0f, 0.2f, 0.4f);
                for (PlayerEntity player : PlayerEspModule.INSTANCE.getTargets())
                    outline(matrices, lines, PlayerEspModule.getInterpolatedBox(player, tickDelta), c, 0.9f);
            }

            // Tracer marker boxes remain visible without relying on removed legacy line APIs.
            if (PlayerEspModule.INSTANCE.isEnabled() && PlayerEspModule.INSTANCE.isDrawTracers()) {
                float[] c = colorOrRainbow(1.0f, 0.3f, 0.5f);
                for (PlayerEntity player : PlayerEspModule.INSTANCE.getTargets()) {
                    Vec3d end = PlayerEspModule.getInterpolatedBox(player, tickDelta).getCenter();
                    outline(matrices, lines, new Box(end.x - 0.045, end.y - 0.045, end.z - 0.045,
                            end.x + 0.045, end.y + 0.045, end.z + 0.045), c, 0.9f);
                }
            }

            if (ChunkAnalyzerModule.INSTANCE.isEnabled()) {
                for (ChunkPos cp : ChunkAnalyzerModule.INSTANCE.getSuspiciousChunks()) {
                    Box box = new Box(cp.getStartX(), client.world.getBottomY(), cp.getStartZ(),
                            cp.getEndX() + 1, client.world.getBottomY() + 8, cp.getEndZ() + 1);
                    outline(matrices, lines, box, new float[]{1.0f, 0.2f, 0.2f}, 0.65f);
                }
            }

            if (SchematicBuilderModule.INSTANCE.isEnabled()
                    && SchematicBuilderModule.INSTANCE.getOrigin() != null
                    && !SchematicBuilderModule.INSTANCE.getBlocks().isEmpty()) {
                BlockPos origin = SchematicBuilderModule.INSTANCE.getOrigin();
                int drawn = 0;
                for (var sb : SchematicBuilderModule.INSTANCE.getBlocks()) {
                    if (drawn++ >= 400) break;
                    BlockPos pos = origin.add(sb.x, sb.y, sb.z);
                    var state = client.world.getBlockState(pos);
                    if (state.isOf(sb.block)) continue;
                    boolean empty = state.isAir() || state.isReplaceable();
                    outline(matrices, lines, new Box(pos).expand(0.01),
                            empty ? new float[]{0.2f, 0.9f, 0.3f} : new float[]{1.0f, 0.2f, 0.3f}, 0.7f);
                }
            }
            matrices.pop();
        } catch (Throwable ignored) {
            // Keep third-party renderer conflicts from taking down the client.
        }
    }

    public static void outline(MatrixStack matrices, VertexConsumer consumer, Box box, float[] rgb, float alpha) {
        int color = (Math.max(0, Math.min(255, (int)(alpha * 255))) << 24)
                | (Math.max(0, Math.min(255, (int)(rgb[0] * 255))) << 16)
                | (Math.max(0, Math.min(255, (int)(rgb[1] * 255))) << 8)
                | Math.max(0, Math.min(255, (int)(rgb[2] * 255)));
        VertexRendering.drawOutline(matrices, consumer, VoxelShapes.cuboid(box), 0.0, 0.0, 0.0, color, 1.0f);
    }

    private static float[] colorOrRainbow(float r, float g, float b) {
        return RainbowModule.INSTANCE.isEnabled() ? RainbowModule.INSTANCE.getRgb() : new float[]{r, g, b};
    }
}
