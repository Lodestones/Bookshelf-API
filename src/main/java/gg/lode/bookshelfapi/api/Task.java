package gg.lode.bookshelfapi.api;

import gg.lode.bookshelfapi.api.compat.FoliaCompat;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.TimeUnit;

public class Task {

    private static final long MS_PER_TICK = 50L;

    public static BukkitTask run(JavaPlugin plugin, Runnable runnable) {
        if (FoliaCompat.isFolia()) {
            return wrap(plugin,
                    Bukkit.getServer().getGlobalRegionScheduler().run(plugin, task -> runnable.run())
            );
        }
        return Bukkit.getScheduler().runTask(plugin, runnable);
    }

    public static void runEntity(JavaPlugin plugin, Entity entity, Runnable runnable) {
        if (FoliaCompat.isFolia()) {
            entity.getScheduler().run(plugin, task -> runnable.run(), null);
            return;
        }
        Bukkit.getScheduler().runTask(plugin, runnable);
    }

    public static void laterEntity(JavaPlugin plugin, Entity entity, Runnable runnable, long delay) {
        if (FoliaCompat.isFolia()) {
            entity.getScheduler().execute(plugin, runnable, null, Math.max(delay, 1L));
            return;
        }
        Bukkit.getScheduler().runTaskLater(plugin, runnable, delay);
    }

    public static BukkitTask runAsync(JavaPlugin plugin, Runnable runnable) {
        if (FoliaCompat.isFolia()) {
            return wrap(plugin,
                    Bukkit.getServer().getAsyncScheduler().runNow(plugin, task -> runnable.run())
            );
        }
        return Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
    }

    public static BukkitTask timer(JavaPlugin plugin, Runnable runnable, long delay, long period) {
        if (FoliaCompat.isFolia()) {
            return wrap(plugin,
                    Bukkit.getServer().getGlobalRegionScheduler().runAtFixedRate(
                            plugin, task -> runnable.run(), Math.max(delay, 1L), period)
            );
        }
        return Bukkit.getScheduler().runTaskTimer(plugin, runnable, delay, period);
    }

    public static BukkitTask timerAsync(JavaPlugin plugin, Runnable runnable, long delay, long period) {
        if (FoliaCompat.isFolia()) {
            return wrap(plugin,
                    Bukkit.getServer().getAsyncScheduler().runAtFixedRate(
                            plugin, task -> runnable.run(),
                            Math.max(delay, 1L) * MS_PER_TICK, period * MS_PER_TICK, TimeUnit.MILLISECONDS)
            );
        }
        return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, delay, period);
    }

    public static BukkitTask later(JavaPlugin plugin, Runnable runnable, long delay) {
        if (FoliaCompat.isFolia()) {
            if (delay <= 0) {
                return run(plugin, runnable);
            }
            return wrap(plugin,
                    Bukkit.getServer().getGlobalRegionScheduler().runDelayed(plugin, task -> runnable.run(), delay)
            );
        }
        return Bukkit.getScheduler().runTaskLater(plugin, runnable, delay);
    }

    public static BukkitTask laterAsync(JavaPlugin plugin, Runnable runnable, long delay) {
        if (FoliaCompat.isFolia()) {
            return wrap(plugin,
                    Bukkit.getServer().getAsyncScheduler().runDelayed(
                            plugin, task -> runnable.run(),
                            Math.max(delay, 1L) * MS_PER_TICK, TimeUnit.MILLISECONDS)
            );
        }
        return Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runnable, delay);
    }

    public static void cancelAllPluginTasks(JavaPlugin plugin) {
        if (FoliaCompat.isFolia()) {
            Bukkit.getServer().getGlobalRegionScheduler().cancelTasks(plugin);
            Bukkit.getServer().getAsyncScheduler().cancelTasks(plugin);
            return;
        }
        Bukkit.getScheduler().cancelTasks(plugin);
    }

    private static BukkitTask wrap(Plugin owner, ScheduledTask task) {
        return new BukkitTask() {
            @Override
            public int getTaskId() {
                return -1;
            }

            @Override
            public Plugin getOwner() {
                return owner;
            }

            @Override
            public boolean isSync() {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return task.isCancelled();
            }

            @Override
            public void cancel() {
                task.cancel();
            }
        };
    }
}
