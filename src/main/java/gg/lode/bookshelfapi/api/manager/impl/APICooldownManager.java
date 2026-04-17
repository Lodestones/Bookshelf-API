package gg.lode.bookshelfapi.api.manager.impl;

import gg.lode.bookshelfapi.api.Task;
import gg.lode.bookshelfapi.api.TaskHandle;
import gg.lode.bookshelfapi.api.manager.ICooldownManager;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class APICooldownManager implements ICooldownManager {
    private final JavaPlugin plugin;
    private final Map<String, Long> cooldowns = new HashMap<>();
    private final Map<String, TaskHandle> callbacks = new HashMap<>();
    private final TaskHandle timerTask;

    public APICooldownManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.timerTask = Task.timer(plugin, this::run, 1, 1);
    }

    @Override
    public void setCooldown(Player player, String id, long milliseconds) {
        cooldowns.put(player.getUniqueId() + "-" + id, System.currentTimeMillis() + milliseconds);
    }

    @Override
    public void setCooldown(String id, long milliseconds) {
        cooldowns.put(id, System.currentTimeMillis() + milliseconds);
    }

    @Override
    public void setCooldown(Player player, String id, long milliseconds, Consumer<Player> callback) {
        cooldowns.put(player.getUniqueId() + "-" + id, System.currentTimeMillis() + milliseconds);
        if (callbacks.containsKey(player.getUniqueId() + "-" + id)) {
            callbacks.get(player.getUniqueId() + "-" + id).cancel();
        }

        callbacks.put(player.getUniqueId() + "-" + id, Task.later(plugin, () -> {
            if (callback != null) {
                callback.accept(player);
            }
        }, (milliseconds / 1000) * 20));
    }

    @Override
    public void setCooldown(String id, long milliseconds, Consumer<Player> callback) {
        cooldowns.put(id, System.currentTimeMillis() + milliseconds);

        if (callbacks.containsKey(id)) {
            callbacks.get(id).cancel();
        }

        callbacks.put(id, Task.later(plugin, () -> {
            if (callback != null) {
                callback.accept(null);
            }
        }, (milliseconds / 1000) * 20));
    }

    @Override
    public boolean hasCooldown(Player player, String id) {
        String key = player.getUniqueId() + "-" + id;
        return cooldowns.containsKey(key) && cooldowns.get(key) > System.currentTimeMillis();
    }

    @Override
    public boolean hasCooldown(String id) {
        return cooldowns.containsKey(id) && cooldowns.get(id) > System.currentTimeMillis();
    }

    @Override
    public boolean notifyPlayerWithCooldown(Player player, String id, Component component) {
        return notifyPlayerWithCooldown(player, id, component, 1000L);
    }

    @Override
    public boolean notifyPlayerWithCooldown(Player player, String id, String message) {
        return notifyPlayerWithCooldown(player, id, Component.text(message), 1000L);
    }

    @Override
    public boolean notifyPlayerWithCooldown(Player player, String id, Component component, long milliseconds) {
        if (hasCooldown(player, id)) {
            player.sendMessage(component);
            return true;
        }
        setCooldown(player, id, milliseconds);
        return false;
    }

    @Override
    public boolean notifyPlayerWithCooldown(Player player, String id, String message, long milliseconds) {
        return notifyPlayerWithCooldown(player, id, Component.text(message), milliseconds);
    }

    @Override
    public long getCooldown(Player player, String id) {
        String key = player.getUniqueId() + "-" + id;
        Long end = cooldowns.get(key);
        if (end == null) return 0;
        long remaining = end - System.currentTimeMillis();
        return Math.max(remaining, 0);
    }

    public void run() {
        List<String> keysToRemove = new ArrayList<>();
        cooldowns.forEach((key, cooldown) -> {
            if (System.currentTimeMillis() > cooldown)
                keysToRemove.add(key);
        });
        keysToRemove.forEach(cooldowns::remove);
    }
}