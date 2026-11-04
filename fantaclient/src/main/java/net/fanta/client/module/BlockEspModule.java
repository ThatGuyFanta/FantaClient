package net.fanta.client.module;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class BlockEspModule implements FantaModule {
    public static final BlockEspModule INSTANCE = new BlockEspModule();

    private boolean enabled = false;
    private Block selectedBlock = Blocks.DIAMOND_ORE;
    private final List<BlockPos> matchingBlocks = new CopyOnWriteArrayList<>();
    private int scanCooldown = 0;
    private static final int SCAN_RADIUS_CHUNKS = 3;

    private BlockEspModule() {}

    @Override
    public String getName() { return "Block ESP"; }

    @Override
    public String getCategory() { return "Render"; }

    @Override
    public String getDescription() { return "Highlight selected blocks through walls."; }

    @Override
    public boolean isEnabled() { return enabled; }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            matchingBlocks.clear();
        }
    }

    public Block getSelectedBlock() {
        return selectedBlock;
    }

    public void setSelectedBlock(Block block) {
        this.selectedBlock = block;
        matchingBlocks.clear();
        scanCooldown = 0;
    }

    public List<BlockPos> getMatchingBlocks() {
        return matchingBlocks;
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
        scanCooldown = 40; // rescan every 2 seconds

        matchingBlocks.clear();
        BlockPos playerPos = client.player.getBlockPos();
        int pChunkX = playerPos.getX() >> 4;
        int pChunkZ = playerPos.getZ() >> 4;

        for (int cx = -SCAN_RADIUS_CHUNKS; cx <= SCAN_RADIUS_CHUNKS; cx++) {
            for (int cz = -SCAN_RADIUS_CHUNKS; cz <= SCAN_RADIUS_CHUNKS; cz++) {
                WorldChunk chunk = client.world.getChunk(pChunkX + cx, pChunkZ + cz);
                if (chunk == null) continue;

                ChunkPos cp = chunk.getPos();
                int startX = cp.getStartX();
                int startZ = cp.getStartZ();
                int minY = client.world.getBottomY();
                int maxY = client.world.getTopYInclusive();

                // Limit Y scan around player for performance
                int playerY = playerPos.getY();
                int yMin = Math.max(minY, playerY - 64);
                int yMax = Math.min(maxY, playerY + 64);

                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        for (int y = yMin; y <= yMax; y++) {
                            BlockPos pos = new BlockPos(startX + x, y, startZ + z);
                            if (chunk.getBlockState(pos).isOf(selectedBlock)) {
                                if (matchingBlocks.size() < 512) matchingBlocks.add(pos.toImmutable());
                            }
                        }
                    }
                }
            }
        }
    }
}
