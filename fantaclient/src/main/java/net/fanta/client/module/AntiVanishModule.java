package net.fanta.client.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AntiVanishModule implements FantaModule {
    public static final AntiVanishModule INSTANCE = new AntiVanishModule();

    private boolean enabled = false;
    private final Set<UUID> warned = new HashSet<>();
    private int cooldownTicks = 0;

    private AntiVanishModule() {}

    @Override
    public String getName() { return "Anti-Vanish"; }

    @Override
    public String getCategory() { return "Utility"; }

    @Override
    public String getDescription() { return "Detect players who are vanished (in world but not on tab list)."; }

    @Override
    public boolean isEnabled() { return enabled; }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            warned.clear();
        }
    }

    public static void onTick(MinecraftClient client) {
        INSTANCE.tick(client);
    }

    private void tick(MinecraftClient client) {
        if (!enabled || client.world == null || client.player == null || client.getNetworkHandler() == null) {
            return;
        }

        if (cooldownTicks > 0) {
            cooldownTicks--;
            return;
        }

        for (PlayerEntity player : client.world.getPlayers()) {
            if (player == client.player) continue;
            UUID uuid = player.getUuid();
            PlayerListEntry entry = client.getNetworkHandler().getPlayerListEntry(uuid);
            if (entry == null && !warned.contains(uuid)) {
                client.player.sendMessage(
                        Text.literal("[FantaClient] ").formatted(Formatting.GOLD)
                                .append(Text.literal("Possible vanished player nearby: " + player.getName().getString())
                                        .formatted(Formatting.RED)),
                        false);
                warned.add(uuid);
                cooldownTicks = 40; // avoid spam
            }
        }

        // Clean up warned set for players that left
        warned.removeIf(uuid -> client.world.getPlayerByUuid(uuid) == null);
    }
}
