package net.fanta.client.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Highlights chunks that show signs of player activity (simplified heuristic).
 * Full reliable "base finder" requires more advanced analysis; this is a lightweight indicator.
 */
public class ChunkAnalyzerModule implements FantaModule {
    public static final ChunkAnalyzerModule INSTANCE = new ChunkAnalyzerModule();

    private boolean enabled = false;
    private final List<ChunkPos> suspiciousChunks = new CopyOnWriteArrayList<>();
    private int scanCooldown = 0;

    private ChunkAnalyzerModule() {}

    @Override
    public String getName() { return "Chunk Analyzer"; }

    @Override
    public String getCategory() { return "Utility"; }

    @Override
    public String getDescription() { return "Marks chunks with signs of player activity (simplified)."; }

    @Override
    public boolean isEnabled() { return enabled; }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) suspiciousChunks.clear();
    }

    public List<ChunkPos> getSuspiciousChunks() {
        return suspiciousChunks;
    }

    public static void onTick(MinecraftClient client) {
        INSTANCE.tick(client);
    }

    private void tick(MinecraftClient client) {
        if (!enabled || client.world == null || client.player == null) return;

        if (scanCooldown > 0) {
            scanCooldown--;
            return;
        }
        scanCooldown = 60;

        suspiciousChunks.clear();
        BlockPos playerPos = client.player.getBlockPos();
        int pChunkX = playerPos.getX() >> 4;
        int pChunkZ = playerPos.getZ() >> 4;
        int radius = 4;

        for (int cx = -radius; cx <= radius; cx++) {
            for (int cz = -radius; cz <= radius; cz++) {
                WorldChunk chunk = client.world.getChunk(pChunkX + cx, pChunkZ + cz);
                if (chunk == null) continue;

                // Very light heuristic: count non-air block entities / presence of storage
                int activityScore = chunk.getBlockEntities().size();
                if (activityScore >= 3) { // arbitrary threshold
                    suspiciousChunks.add(chunk.getPos());
                }
            }
        }
    }
}
