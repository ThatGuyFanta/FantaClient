package net.fanta.client.module;

import net.minecraft.block.entity.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class StorageEspModule implements FantaModule {
    public static final StorageEspModule INSTANCE = new StorageEspModule();

    private boolean enabled = false;
    private boolean filterChests = true;
    private boolean filterEnderChests = true;
    private boolean filterHoppers = false;
    private boolean filterShulkers = true;
    private boolean filterBarrels = true;

    private final List<BlockPos> storages = new CopyOnWriteArrayList<>();
    private int scanCooldown = 0;

    private StorageEspModule() {}

    @Override
    public String getName() { return "Storage ESP"; }

    @Override
    public String getCategory() { return "Render"; }

    @Override
    public String getDescription() { return "Highlight chests, shulkers, hoppers and other storage."; }

    @Override
    public boolean isEnabled() { return enabled; }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) storages.clear();
    }

    public boolean isFilterChests() { return filterChests; }
    public void setFilterChests(boolean v) { filterChests = v; }

    public boolean isFilterEnderChests() { return filterEnderChests; }
    public void setFilterEnderChests(boolean v) { filterEnderChests = v; }

    public boolean isFilterHoppers() { return filterHoppers; }
    public void setFilterHoppers(boolean v) { filterHoppers = v; }

    public boolean isFilterShulkers() { return filterShulkers; }
    public void setFilterShulkers(boolean v) { filterShulkers = v; }

    public boolean isFilterBarrels() { return filterBarrels; }
    public void setFilterBarrels(boolean v) { filterBarrels = v; }

    public List<BlockPos> getStorages() {
        return storages;
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
        scanCooldown = 20;

        storages.clear();
        BlockPos playerPos = client.player.getBlockPos();
        int pChunkX = playerPos.getX() >> 4;
        int pChunkZ = playerPos.getZ() >> 4;
        int radius = 4;

        for (int cx = -radius; cx <= radius; cx++) {
            for (int cz = -radius; cz <= radius; cz++) {
                WorldChunk chunk = client.world.getChunk(pChunkX + cx, pChunkZ + cz);
                if (chunk == null) continue;

                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    if (shouldHighlight(be)) {
                        if (storages.size() < 256) storages.add(be.getPos().toImmutable());
                    }
                }
            }
        }
    }

    private boolean shouldHighlight(BlockEntity be) {
        if (be instanceof ChestBlockEntity || be instanceof TrappedChestBlockEntity) {
            return filterChests;
        }
        if (be instanceof EnderChestBlockEntity) {
            return filterEnderChests;
        }
        if (be instanceof HopperBlockEntity) {
            return filterHoppers;
        }
        if (be instanceof ShulkerBoxBlockEntity) {
            return filterShulkers;
        }
        if (be instanceof BarrelBlockEntity) {
            return filterBarrels;
        }
        return false;
    }
}
