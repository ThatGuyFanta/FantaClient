package net.fanta.client.module;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Auto-builds a loaded schematic by placing missing blocks when the player
 * has the required items and is in range.
 *
 * Schematic format (simple JSON-lines / text):
 *   Each line: x,y,z,block_id
 *   Example: 0,0,0,minecraft:stone
 * Files go in: config/fantaclient/schematics/*.schem.txt
 */
public class SchematicBuilderModule implements FantaModule {
    public static final SchematicBuilderModule INSTANCE = new SchematicBuilderModule();

    private boolean enabled = false;
    private BlockPos origin = null;
    private final List<SchematicBlock> blocks = new ArrayList<>();
    private String loadedName = "none";
    private int placeCooldown = 0;
    private int placeDelayTicks = 2; // delay between placements
    private double reach = 4.5;
    private int index = 0;

    private SchematicBuilderModule() {}

    @Override
    public String getName() { return "Schematic Builder"; }

    @Override
    public String getCategory() { return "Utility"; }

    @Override
    public String getDescription() {
        return "Auto-places blocks from a loaded schematic when you have the items.";
    }

    @Override
    public boolean isEnabled() { return enabled; }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled) {
            if (blocks.isEmpty()) {
                loadFirstAvailableOrDemo();
            }
            if (origin == null) {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client.player != null) {
                    origin = client.player.getBlockPos();
                }
            }
            index = 0;
        }
    }

    public String getLoadedName() { return loadedName; }
    public int getBlockCount() { return blocks.size(); }
    public BlockPos getOrigin() { return origin; }

    public void setOriginToPlayer() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            origin = client.player.getBlockPos();
            index = 0;
        }
    }

    public static void onTick(MinecraftClient client) {
        try {
            INSTANCE.tick(client);
        } catch (Throwable ignored) {}
    }

    private void tick(MinecraftClient client) {
        if (!enabled || client == null || client.player == null || client.world == null || client.interactionManager == null) {
            return;
        }
        if (origin == null || blocks.isEmpty()) {
            return;
        }

        if (placeCooldown > 0) {
            placeCooldown--;
            return;
        }

        ClientPlayerEntity player = client.player;
        World world = client.world;

        // Scan a window of schematic blocks for something placeable
        int checked = 0;
        int maxCheck = Math.min(64, blocks.size());

        while (checked < maxCheck) {
            if (index >= blocks.size()) {
                index = 0;
            }
            SchematicBlock sb = blocks.get(index);
            index++;
            checked++;

            BlockPos pos = origin.add(sb.x, sb.y, sb.z);
            BlockState current = world.getBlockState(pos);

            // Already correct
            if (current.isOf(sb.block)) {
                continue;
            }
            // Only place into replaceable / air-like
            if (!current.isReplaceable() && !current.isAir()) {
                continue;
            }

            double dist = player.getEyePos().distanceTo(Vec3d.ofCenter(pos));
            if (dist > reach) {
                continue;
            }

            int slot = findHotbarSlot(player, sb.block);
            if (slot == -1) {
                continue;
            }

            // Switch hotbar slot
            try {
                player.getInventory().selectedSlot = slot;
            } catch (Throwable t) {
                return;
            }

            // Place against a neighbor face if possible
            Direction face = findPlaceSide(world, pos);
            if (face == null) {
                // Try placing as if clicking the block below upward
                face = Direction.UP;
                pos = pos.down();
                if (!world.getBlockState(pos).isSolidBlock(world, pos)) {
                    continue;
                }
            }

            BlockPos clickPos = pos;
            if (face != null && !world.getBlockState(pos).isAir()) {
                // clickPos is the solid neighbor
            } else {
                // adjust: we need a solid block to click
                BlockPos neighbor = origin.add(sb.x, sb.y, sb.z).offset(face.getOpposite());
                if (world.getBlockState(neighbor).isAir()) {
                    continue;
                }
                clickPos = neighbor;
                face = face;
            }

            BlockPos target = origin.add(sb.x, sb.y, sb.z);
            // Recompute neighbor to click
            Direction placeFace = null;
            BlockPos against = null;
            for (Direction d : Direction.values()) {
                BlockPos n = target.offset(d);
                BlockState ns = world.getBlockState(n);
                if (!ns.isAir() && !ns.isReplaceable()) {
                    against = n;
                    placeFace = d.getOpposite();
                    break;
                }
            }
            if (against == null || placeFace == null) {
                // Allow placing on ground under target
                against = target.down();
                if (world.getBlockState(against).isAir()) {
                    continue;
                }
                placeFace = Direction.UP;
            }

            Vec3d hit = Vec3d.ofCenter(against).add(
                    placeFace.getOffsetX() * 0.5,
                    placeFace.getOffsetY() * 0.5,
                    placeFace.getOffsetZ() * 0.5
            );
            BlockHitResult bhr = new BlockHitResult(hit, placeFace, against, false);

            client.interactionManager.interactBlock(player, Hand.MAIN_HAND, bhr);
            player.swingHand(Hand.MAIN_HAND);

            placeCooldown = placeDelayTicks;
            return; // one block per delay window
        }
    }

    private int findHotbarSlot(ClientPlayerEntity player, Block block) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            if (stack.getItem() instanceof BlockItem bi && bi.getBlock() == block) {
                return i;
            }
        }
        return -1;
    }

    private Direction findPlaceSide(World world, BlockPos target) {
        for (Direction d : Direction.values()) {
            BlockPos n = target.offset(d);
            if (!world.getBlockState(n).isAir() && !world.getBlockState(n).isReplaceable()) {
                return d.getOpposite();
            }
        }
        return null;
    }

    public void loadFirstAvailableOrDemo() {
        Path dir = Path.of("config/fantaclient/schematics");
        try {
            Files.createDirectories(dir);
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.schem.txt")) {
                for (Path p : stream) {
                    if (loadFromFile(p)) {
                        return;
                    }
                }
            }
        } catch (Exception ignored) {}

        // Demo: 3x3 stone platform
        blocks.clear();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                blocks.add(new SchematicBlock(x, 0, z, Blocks.STONE));
            }
        }
        loadedName = "demo_3x3_stone";
    }

    public boolean loadFromFile(Path path) {
        List<SchematicBlock> loaded = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split(",");
                if (parts.length < 4) continue;
                int x = Integer.parseInt(parts[0].trim());
                int y = Integer.parseInt(parts[1].trim());
                int z = Integer.parseInt(parts[2].trim());
                String id = parts[3].trim();
                Block block = Registries.BLOCK.get(Identifier.of(id));
                if (block == null || block == Blocks.AIR) continue;
                loaded.add(new SchematicBlock(x, y, z, block));
            }
        } catch (Exception e) {
            return false;
        }
        if (loaded.isEmpty()) return false;
        blocks.clear();
        blocks.addAll(loaded);
        loadedName = path.getFileName().toString();
        index = 0;
        return true;
    }

    public List<SchematicBlock> getBlocks() {
        return blocks;
    }

    public static final class SchematicBlock {
        public final int x, y, z;
        public final Block block;

        public SchematicBlock(int x, int y, int z, Block block) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.block = block;
        }
    }
}
