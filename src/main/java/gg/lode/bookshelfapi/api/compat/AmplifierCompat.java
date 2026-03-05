package gg.lode.bookshelfapi.api.compat;

import gg.lode.bookshelfapi.api.Task;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class AmplifierCompat {

    private static final Map<UUID, BukkitTask> scheduledUnmutes = new ConcurrentHashMap<>();

    public static void muteVoice(Player player) {
        setWhoCanHear(player, new HashSet<>());
    }

    public static void unmuteVoice(Player player) {
        setWhoCanHear(player, null);
    }

    /**
     * Cancels any scheduled voice unmute task for this player.
     * Call this on player quit.
     */
    public static void cancelScheduledUnmute(UUID uuid) {
        BukkitTask task = scheduledUnmutes.remove(uuid);
        if (task != null) task.cancel();
    }

    /**
     * Schedules a delayed task to unmute the player's voice when their mute expires.
     * The provided check runs when the timer fires to verify the mute wasn't re-applied.
     * Any previously scheduled unmute for this player is cancelled first.
     *
     * @param plugin the plugin instance for scheduling
     * @param player the muted player
     * @param expiresAt the mute expiration timestamp in milliseconds, or -1 for permanent
     * @param stillMutedCheck called when the timer fires; should call {@link #unmuteVoice(Player)} if no longer muted
     */
    public static void scheduleVoiceUnmute(JavaPlugin plugin, Player player, long expiresAt, Runnable stillMutedCheck) {
        if (Bukkit.getPluginManager().getPlugin("Amplifier") == null) return;
        if (expiresAt == -1) return;

        long remainingMs = expiresAt - System.currentTimeMillis();
        if (remainingMs <= 0) return;

        cancelScheduledUnmute(player.getUniqueId());

        long delayTicks = Math.max(1, remainingMs / 50);
        BukkitTask task = Task.later(plugin, () -> {
            scheduledUnmutes.remove(player.getUniqueId());
            if (!player.isOnline()) return;
            stillMutedCheck.run();
        }, delayTicks);
        scheduledUnmutes.put(player.getUniqueId(), task);
    }

    @SuppressWarnings("unchecked")
    private static void setWhoCanHear(Player player, Set<UUID> whoCanHear) {
        try {
            if (Bukkit.getPluginManager().getPlugin("Amplifier") == null) return;

            Class<?> apiClass = Class.forName("gg.lode.amplifierapi.AmplifierAPI");
            Object api = apiClass.getMethod("getApi").invoke(null);
            if (api == null) return;

            Object voiceManager = api.getClass().getMethod("getVoiceManager").invoke(api);
            if (voiceManager == null) return;

            CompletableFuture<?> future = (CompletableFuture<?>) voiceManager.getClass()
                    .getMethod("fetchOrCreateVoicePlayer", Player.class)
                    .invoke(voiceManager, player);

            future.thenAccept(voicePlayer -> {
                try {
                    voicePlayer.getClass().getMethod("setWhoCanHear", Set.class)
                            .invoke(voicePlayer, whoCanHear);
                } catch (Exception ignored) {
                }
            });
        } catch (Exception ignored) {
        }
    }
}
