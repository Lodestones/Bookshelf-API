package gg.lode.bookshelfapi.api;

import gg.lode.bookshelfapi.api.compat.FoliaCompat;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.TimeUnit;

public class Task {

    private static final long MS_PER_TICK = 50L;

    public static TaskHandle run(JavaPlugin plugin, Runnable runnable) {
        if (FoliaCompat.isFolia()) {
            return TaskHandle.of(
                    Bukkit.getServer().getGlobalRegionScheduler().run(plugin, task -> runnable.run())
            );
        }
        return TaskHandle.of(Bukkit.getScheduler().runTask(plugin, runnable));
    }

    public static TaskHandle runAsync(JavaPlugin plugin, Runnable runnable) {
        if (FoliaCompat.isFolia()) {
            return TaskHandle.of(
                    Bukkit.getServer().getAsyncScheduler().runNow(plugin, task -> runnable.run())
            );
        }
        return TaskHandle.of(Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable));
    }

    public static TaskHandle timer(JavaPlugin plugin, Runnable runnable, long delay, long period) {
        if (FoliaCompat.isFolia()) {
            return TaskHandle.of(
                    Bukkit.getServer().getGlobalRegionScheduler().runAtFixedRate(
                            plugin, task -> runnable.run(), Math.max(delay, 1L), period)
            );
        }
        return TaskHandle.of(Bukkit.getScheduler().runTaskTimer(plugin, runnable, delay, period));
    }

    public static TaskHandle timerAsync(JavaPlugin plugin, Runnable runnable, long delay, long period) {
        if (FoliaCompat.isFolia()) {
            return TaskHandle.of(
                    Bukkit.getServer().getAsyncScheduler().runAtFixedRate(
                            plugin, task -> runnable.run(),
                            Math.max(delay, 1L) * MS_PER_TICK, period * MS_PER_TICK, TimeUnit.MILLISECONDS)
            );
        }
        return TaskHandle.of(Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, delay, period));
    }

    public static TaskHandle later(JavaPlugin plugin, Runnable runnable, long delay) {
        if (FoliaCompat.isFolia()) {
            if (delay <= 0) {
                return run(plugin, runnable);
            }
            return TaskHandle.of(
                    Bukkit.getServer().getGlobalRegionScheduler().runDelayed(plugin, task -> runnable.run(), delay)
            );
        }
        return TaskHandle.of(Bukkit.getScheduler().runTaskLater(plugin, runnable, delay));
    }

    public static TaskHandle laterAsync(JavaPlugin plugin, Runnable runnable, long delay) {
        if (FoliaCompat.isFolia()) {
            return TaskHandle.of(
                    Bukkit.getServer().getAsyncScheduler().runDelayed(
                            plugin, task -> runnable.run(),
                            Math.max(delay, 1L) * MS_PER_TICK, TimeUnit.MILLISECONDS)
            );
        }
        return TaskHandle.of(Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runnable, delay));
    }

    public static void cancelAllPluginTasks(JavaPlugin plugin) {
        if (FoliaCompat.isFolia()) {
            Bukkit.getServer().getGlobalRegionScheduler().cancelTasks(plugin);
            Bukkit.getServer().getAsyncScheduler().cancelTasks(plugin);
            return;
        }
        Bukkit.getScheduler().cancelTasks(plugin);
    }
}
