package net.fanta.client.render;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fanta.client.module.*;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;

/**
 * ESP / overlay rendering for 1.21.11.
 * Uses WorldRenderer.drawBox + VertexConsumer from the world render context
 * (BufferRenderer.drawWithGlobalProgram was removed in recent versions).
 */
public final class EspRenderer {
    private EspRenderer() {}

    public static void render(WorldRenderContext context) {
        try {
        MatrixStack matrices = context.matrixStack();
        VertexConsumerProvider consumers = context.consumers();
        if (matrices == null || consumers == null) return;

        Vec3d cam = context.camera().getPos();
        float tickDelta = context.tickCounter().getTickDelta(false);

        // Cosmetic wings (own transform)
        WingsModule.INSTANCE.render(matrices, consumers, cam, tickDelta);

        matrices.push();
        matrices.translate(-cam.x, -cam.y, -cam.z);

        VertexConsumer lines = consumers.getBuffer(RenderLayer.getLines());

        // Block ESP
        if (BlockEspModule.INSTANCE.isEnabled()) {
            float[] c = colorOrRainbow(0.0f, 0.9f, 1.0f);
            for (BlockPos pos : BlockEspModule.INSTANCE.getMatchingBlocks()) {
                WorldRenderer.drawBox(matrices, lines, new Box(pos).expand(0.01), c[0], c[1], c[2], 0.85f);
            }
        }

        // Storage ESP
        if (StorageEspModule.INSTANCE.isEnabled()) {
            float[] c = colorOrRainbow(1.0f, 0.7f, 0.0f);
            for (BlockPos pos : StorageEspModule.INSTANCE.getStorages()) {
                WorldRenderer.drawBox(matrices, lines, new Box(pos).expand(0.01), c[0], c[1], c[2], 0.85f);
            }
        }

        // Player ESP
        if (PlayerEspModule.INSTANCE.isEnabled() && PlayerEspModule.INSTANCE.isDrawBox()) {
            float[] c = colorOrRainbow(1.0f, 0.2f, 0.4f);
            for (PlayerEntity p : PlayerEspModule.INSTANCE.getTargets()) {
                Box box = PlayerEspModule.getInterpolatedBox(p, tickDelta);
                WorldRenderer.drawBox(matrices, lines, box, c[0], c[1], c[2], 0.9f);
            }
        }

        // Player tracers
        if (PlayerEspModule.INSTANCE.isEnabled() && PlayerEspModule.INSTANCE.isDrawTracers()) {
            float[] c = colorOrRainbow(1.0f, 0.3f, 0.5f);
            for (PlayerEntity p : PlayerEspModule.INSTANCE.getTargets()) {
                Box box = PlayerEspModule.getInterpolatedBox(p, tickDelta);
                Vec3d end = box.getCenter();
                // simple vertical marker at target (line from feet to head already in box)
                WorldRenderer.drawBox(matrices, lines,
                        end.x - 0.05, end.y - 0.05, end.z - 0.05,
                        end.x + 0.05, end.y + 0.05, end.z + 0.05,
                        c[0], c[1], c[2], 0.9f);
            }
        }

        // Chunk analyzer
        if (ChunkAnalyzerModule.INSTANCE.isEnabled()) {
            for (ChunkPos cp : ChunkAnalyzerModule.INSTANCE.getSuspiciousChunks()) {
                Box box = new Box(
                        cp.getStartX(), context.world().getBottomY(), cp.getStartZ(),
                        cp.getEndX() + 1, context.world().getBottomY() + 8, cp.getEndZ() + 1
                );
                WorldRenderer.drawBox(matrices, lines, box, 1.0f, 0.2f, 0.2f, 0.65f);
            }
        }

        // Schematic ghosts
        if (SchematicBuilderModule.INSTANCE.isEnabled()
                && SchematicBuilderModule.INSTANCE.getOrigin() != null
                && !SchematicBuilderModule.INSTANCE.getBlocks().isEmpty()) {
            BlockPos origin = SchematicBuilderModule.INSTANCE.getOrigin();
            int drawn = 0;
            for (var sb : SchematicBuilderModule.INSTANCE.getBlocks()) {
                if (drawn++ > 400) break;
                BlockPos pos = origin.add(sb.x, sb.y, sb.z);
                var state = context.world().getBlockState(pos);
                if (state.isOf(sb.block)) continue;
                boolean empty = state.isAir() || state.isReplaceable();
                float r = empty ? 0.2f : 1.0f;
                float g = empty ? 0.9f : 0.2f;
                WorldRenderer.drawBox(matrices, lines, new Box(pos).expand(0.01), r, g, 0.3f, 0.7f);
            }
        }

        matrices.pop();
        } catch (Throwable t) {
            // Never let ESP/rendering crash the client (Sodium/Iris/API drift)
        }
    }

    private static float[] colorOrRainbow(float r, float g, float b) {
        if (RainbowModule.INSTANCE.isEnabled()) {
            return RainbowModule.INSTANCE.getRgb();
        }
        return new float[]{r, g, b};
    }
}
