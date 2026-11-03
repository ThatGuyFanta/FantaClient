package net.fanta.client;

import net.fanta.client.module.*;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

/**
 * Config manager with:
 * - default auto-save file
 * - named profiles (save/load)
 * - export share packs to config/fantaclient/share/
 * - import from share or profiles folder
 * - reset to defaults
 */
public final class ConfigManager {
    private static final Path CONFIG_DIR = Path.of("config", "fantaclient");
    private static final Path DEFAULT_FILE = CONFIG_DIR.resolve("default.properties");
    private static final Path PROFILES_DIR = CONFIG_DIR.resolve("profiles");
    private static final Path SHARE_DIR = CONFIG_DIR.resolve("share");
    private static final Path LEGACY_FILE = Path.of("config", "fantaclient.properties");

    private static String activeProfile = "default";
    private static int autoSaveCounter = 0;
    private static final int AUTO_SAVE_INTERVAL = 600;
    private static String lastMessage = "";

    private ConfigManager() {}

    public static String getActiveProfile() {
        return activeProfile;
    }

    public static String getLastMessage() {
        return lastMessage;
    }

    public static Path getShareDir() {
        return SHARE_DIR;
    }

    public static Path getProfilesDir() {
        return PROFILES_DIR;
    }

    private static void ensureDirs() throws Exception {
        Files.createDirectories(CONFIG_DIR);
        Files.createDirectories(PROFILES_DIR);
        Files.createDirectories(SHARE_DIR);
    }

    public static void load() {
        try {
            ensureDirs();
            // Migrate legacy single file once
            if (Files.exists(LEGACY_FILE) && !Files.exists(DEFAULT_FILE)) {
                Files.copy(LEGACY_FILE, DEFAULT_FILE, StandardCopyOption.REPLACE_EXISTING);
            }
            Path file = DEFAULT_FILE;
            if (!Files.exists(file)) {
                lastMessage = "No config – using defaults";
                FantaClientInitializer.LOGGER.info(lastMessage);
                return;
            }
            loadFromPath(file);
            activeProfile = "default";
            lastMessage = "Loaded default config";
        } catch (Throwable t) {
            lastMessage = "Load failed: " + t.getMessage();
            FantaClientInitializer.LOGGER.warn(lastMessage);
        }
    }

    public static void save() {
        try {
            ensureDirs();
            saveToPath(DEFAULT_FILE);
            if (!"default".equals(activeProfile)) {
                saveToPath(PROFILES_DIR.resolve(sanitize(activeProfile) + ".properties"));
            }
            lastMessage = "Saved (" + activeProfile + ")";
        } catch (Throwable t) {
            lastMessage = "Save failed: " + t.getMessage();
            FantaClientInitializer.LOGGER.warn(lastMessage, t);
        }
    }

    public static void tickAutoSave() {
        try {
            autoSaveCounter++;
            if (autoSaveCounter >= AUTO_SAVE_INTERVAL) {
                autoSaveCounter = 0;
                save();
            }
        } catch (Throwable t) {
            autoSaveCounter = 0;
            FantaClientInitializer.LOGGER.warn("Auto-save failed: {}", t.toString());
        }
    }

    /** Save current toggles under a named profile. */
    public static boolean saveProfile(String name) {
        try {
            ensureDirs();
            String n = sanitize(name);
            if (n.isEmpty()) {
                lastMessage = "Invalid profile name";
                return false;
            }
            saveToPath(PROFILES_DIR.resolve(n + ".properties"));
            saveToPath(DEFAULT_FILE);
            activeProfile = n;
            lastMessage = "Profile saved: " + n;
            chat(lastMessage);
            return true;
        } catch (Throwable t) {
            lastMessage = "Profile save failed";
            return false;
        }
    }

    /** Load a named profile. */
    public static boolean loadProfile(String name) {
        try {
            ensureDirs();
            String n = sanitize(name);
            Path path = PROFILES_DIR.resolve(n + ".properties");
            if (!Files.exists(path)) {
                lastMessage = "Profile not found: " + n;
                chat(lastMessage);
                return false;
            }
            loadFromPath(path);
            saveToPath(DEFAULT_FILE);
            activeProfile = n;
            lastMessage = "Profile loaded: " + n;
            chat(lastMessage);
            return true;
        } catch (Throwable t) {
            lastMessage = "Profile load failed";
            return false;
        }
    }

    /** List profile names (without extension). */
    public static List<String> listProfiles() {
        List<String> out = new ArrayList<>();
        try {
            ensureDirs();
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(PROFILES_DIR, "*.properties")) {
                for (Path p : stream) {
                    String fn = p.getFileName().toString();
                    out.add(fn.substring(0, fn.length() - ".properties".length()));
                }
            }
        } catch (Throwable ignored) {}
        out.sort(String::compareToIgnoreCase);
        return out;
    }

    /**
     * Export current config to share folder with timestamp.
     * Share the generated .properties file with friends.
     */
    public static Path exportShare() {
        try {
            ensureDirs();
            String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            String base = sanitize(activeProfile);
            Path out = SHARE_DIR.resolve(base + "-" + stamp + ".properties");
            saveToPath(out);
            lastMessage = "Exported: " + out.getFileName();
            chat("Config exported to config/fantaclient/share/" + out.getFileName());
            return out;
        } catch (Throwable t) {
            lastMessage = "Export failed";
            chat(lastMessage);
            return null;
        }
    }

    /** Import a share/profile file by filename (with or without .properties). */
    public static boolean importShare(String fileName) {
        try {
            ensureDirs();
            String fn = fileName.endsWith(".properties") ? fileName : fileName + ".properties";
            Path path = SHARE_DIR.resolve(fn);
            if (!Files.exists(path)) {
                path = PROFILES_DIR.resolve(fn);
            }
            if (!Files.exists(path)) {
                path = CONFIG_DIR.resolve(fn);
            }
            if (!Files.exists(path)) {
                lastMessage = "File not found: " + fn;
                chat(lastMessage);
                return false;
            }
            loadFromPath(path);
            // Copy into profiles as imported name
            String profileName = sanitize(fn.replace(".properties", ""));
            Files.copy(path, PROFILES_DIR.resolve(profileName + ".properties"), StandardCopyOption.REPLACE_EXISTING);
            saveToPath(DEFAULT_FILE);
            activeProfile = profileName;
            lastMessage = "Imported: " + profileName;
            chat(lastMessage);
            return true;
        } catch (Throwable t) {
            lastMessage = "Import failed";
            chat(lastMessage);
            return false;
        }
    }

    /** List shareable files in share folder. */
    public static List<String> listShareFiles() {
        List<String> out = new ArrayList<>();
        try {
            ensureDirs();
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(SHARE_DIR, "*.properties")) {
                for (Path p : stream) {
                    out.add(p.getFileName().toString());
                }
            }
        } catch (Throwable ignored) {}
        out.sort(String::compareToIgnoreCase);
        return out;
    }

    /** Disable all modules (safe defaults). */
    public static void resetDefaults() {
        try {
            for (FantaModule m : ModuleManager.getModules()) {
                try {
                    m.setEnabled(false);
                } catch (Throwable ignored) {}
            }
            activeProfile = "default";
            saveToPath(DEFAULT_FILE);
            lastMessage = "Reset to defaults";
            chat(lastMessage);
        } catch (Throwable t) {
            lastMessage = "Reset failed";
        }
    }

    /** Cycle to next profile in list (for GUI quick switch). */
    public static void cycleProfile(int delta) {
        List<String> profiles = listProfiles();
        if (profiles.isEmpty()) {
            saveProfile("main");
            profiles = listProfiles();
        }
        if (profiles.isEmpty()) return;
        int idx = profiles.indexOf(activeProfile);
        if (idx < 0) idx = 0;
        idx = (idx + delta + profiles.size()) % profiles.size();
        loadProfile(profiles.get(idx));
    }

    // ---- internal ----

    private static void loadFromPath(Path path) throws Exception {
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            Properties props = new Properties();
            props.load(reader);

            for (FantaModule m : ModuleManager.getModules()) {
                String key = keyFor(m);
                setBool(props, key, m);
            }

            PlayerEspModule.INSTANCE.setDrawBox(Boolean.parseBoolean(props.getProperty("playeresp.box", "true")));
            PlayerEspModule.INSTANCE.setDrawTracers(Boolean.parseBoolean(props.getProperty("playeresp.tracers", "false")));
            StorageEspModule.INSTANCE.setFilterChests(Boolean.parseBoolean(props.getProperty("storage.filter.chests", "true")));
            StorageEspModule.INSTANCE.setFilterEnderChests(Boolean.parseBoolean(props.getProperty("storage.filter.ender", "true")));
            StorageEspModule.INSTANCE.setFilterHoppers(Boolean.parseBoolean(props.getProperty("storage.filter.hoppers", "true")));
            StorageEspModule.INSTANCE.setFilterShulkers(Boolean.parseBoolean(props.getProperty("storage.filter.shulkers", "true")));

            String blockId = props.getProperty("blockesp.block", "minecraft:diamond_ore");
            try {
                Block block = Registries.BLOCK.get(Identifier.of(blockId));
                if (block != null && block != Blocks.AIR) {
                    BlockEspModule.INSTANCE.setSelectedBlock(block);
                }
            } catch (Throwable ignored) {}

            activeProfile = props.getProperty("profile.name", activeProfile);
        }
    }

    private static void saveToPath(Path path) throws Exception {
        Properties props = new Properties();
        props.setProperty("profile.name", activeProfile);
        for (FantaModule m : ModuleManager.getModules()) {
            props.setProperty(keyFor(m), String.valueOf(m.isEnabled()));
        }
        props.setProperty("playeresp.box", String.valueOf(PlayerEspModule.INSTANCE.isDrawBox()));
        props.setProperty("playeresp.tracers", String.valueOf(PlayerEspModule.INSTANCE.isDrawTracers()));
        props.setProperty("storage.filter.chests", String.valueOf(StorageEspModule.INSTANCE.isFilterChests()));
        props.setProperty("storage.filter.ender", String.valueOf(StorageEspModule.INSTANCE.isFilterEnderChests()));
        props.setProperty("storage.filter.hoppers", String.valueOf(StorageEspModule.INSTANCE.isFilterHoppers()));
        props.setProperty("storage.filter.shulkers", String.valueOf(StorageEspModule.INSTANCE.isFilterShulkers()));
        try {
            Identifier id = Registries.BLOCK.getId(BlockEspModule.INSTANCE.getSelectedBlock());
            props.setProperty("blockesp.block", id != null ? id.toString() : "minecraft:diamond_ore");
        } catch (Throwable ignored) {
            props.setProperty("blockesp.block", "minecraft:diamond_ore");
        }

        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            props.store(writer, "FantaClient config - share files from config/fantaclient/share/");
        }
    }

    private static String keyFor(FantaModule m) {
        return m.getName().toLowerCase(Locale.ROOT).replace(' ', '_');
    }

    private static void setBool(Properties props, String key, FantaModule module) {
        try {
            if (props.containsKey(key) && module != null) {
                module.setEnabled(Boolean.parseBoolean(props.getProperty(key, "false")));
            }
        } catch (Throwable t) {
            FantaClientInitializer.LOGGER.warn("Config key '{}' ignored: {}", key, t.toString());
        }
    }

    private static String sanitize(String name) {
        if (name == null) return "";
        return name.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9._-]", "_");
    }

    private static void chat(String msg) {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null && client.player != null) {
                client.player.sendMessage(
                        Text.literal("[FantaClient] ").formatted(Formatting.GOLD)
                                .append(Text.literal(msg).formatted(Formatting.GRAY)),
                        false
                );
            }
        } catch (Throwable ignored) {}
    }
}
