package to.lodestone.bookshelfapi.api;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class Task {

    /**
     * Schedule a runnable to be executed synchronously on the next tick.
     *
     * @param plugin the Plugin owning and executing the task
     * @param runnable a Runnable containing code to be executed in the task
     * @return a BukkitTask containing the scheduled task
     */
    public static BukkitTask run(JavaPlugin plugin, Runnable runnable) {
        return Bukkit.getScheduler().runTask(plugin, runnable);
    }

    /**
     * Schedule a runnable to be executed asynchronously on the next tick.
     *
     * @param plugin the Plugin owning and executing the task
     * @param runnable a Runnable containing code to be executed in the task
     * @return a BukkitTask containing the scheduled task
     */
    public static BukkitTask runAsync(JavaPlugin plugin, Runnable runnable) {
        return Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
    }

    /**
     * Schedule a runnable to be repeatedly executed synchronously on a timer.
     *
     * @param plugin the Plugin owning and executing the task
     * @param runnable a Runnable containing code to be executed in the task
     * @param delay the delay, in ticks, before the first execution of the task
     * @param period the delay, in ticks, between each sequential execution of the task
     * @return a BukkitTask containing the scheduled task
     */
    public static BukkitTask timer(JavaPlugin plugin, Runnable runnable, long delay, long period) {
        return Bukkit.getScheduler().runTaskTimer(plugin, runnable, delay, period);
    }

    /**
     * Schedule a runnable to be repeatedly executed asynchronously on a timer.
     *
     * @param plugin the Plugin owning and executing the task
     * @param runnable a Runnable containing code to be executed in the task
     * @param delay the delay, in ticks, before the first execution of the task
     * @param period the delay, in ticks, between each sequential execution of the task
     * @return a BukkitTask containing the scheduled task
     */
    public static BukkitTask timerAsync(JavaPlugin plugin, Runnable runnable, long delay, long period) {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, delay, period);
    }

    /**
     * Schedule a runnable to be executed synchronously after a specified delay.
     *
     * @param plugin the Plugin owning and executing the task
     * @param runnable a Runnable containing code to be executed in the task
     * @param delay the delay, in ticks, before running the task
     * @return a BukkitTask containing the scheduled task
     */
    public static BukkitTask later(JavaPlugin plugin, Runnable runnable, long delay) {
        return Bukkit.getScheduler().runTaskLater(plugin, runnable, delay);
    }

    /**
     * Schedule a runnable to be executed asynchronously after a specified delay.
     *
     * @param plugin the Plugin owning and executing the task
     * @param runnable a Runnable containing code to be executed in the task
     * @param delay the delay, in ticks, before running the task
     * @return a BukkitTask containing the scheduled task
     */
    public static BukkitTask laterAsync(JavaPlugin plugin, Runnable runnable, long delay) {
        return Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runnable, delay);
    }

    /**
     * Cancel and remove all tasks scheduled by a Plugin.
     *
     * @param plugin the Plugin owning the tasks to be removed
     */
    public static void cancelAllPluginTasks(JavaPlugin plugin) {
        Bukkit.getScheduler().cancelTasks(plugin);
    }
}