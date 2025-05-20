package gg.lode.bookshelfapi.api.manager.impl;

import gg.lode.bookshelfapi.api.manager.ICooldownManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class APICooldownManager implements ICooldownManager {
    private final Plugin plugin;
    private final Map<String, Map<UUID, Long>> cooldowns;

    public APICooldownManager(Plugin plugin) {
        this.plugin = plugin;
        this.cooldowns = new ConcurrentHashMap<>();
    }

    @Override
    public void setCooldown(Player player, String cooldownId, long durationMillis) {
        Map<UUID, Long> cooldownMap = cooldowns.computeIfAbsent(cooldownId, k -> new HashMap<>());
        cooldownMap.put(player.getUniqueId(), System.currentTimeMillis() + durationMillis);
    }

    @Override
    public boolean isOnCooldown(Player player, String cooldownId) {
        Map<UUID, Long> cooldownMap = cooldowns.get(cooldownId);
        if (cooldownMap == null) {
            return false;
        }

        Long endTime = cooldownMap.get(player.getUniqueId());
        if (endTime == null) {
            return false;
        }

        if (System.currentTimeMillis() >= endTime) {
            cooldownMap.remove(player.getUniqueId());
            return false;
        }

        return true;
    }

    @Override
    public long getRemainingCooldown(Player player, String cooldownId) {
        Map<UUID, Long> cooldownMap = cooldowns.get(cooldownId);
        if (cooldownMap == null) {
            return 0;
        }

        Long endTime = cooldownMap.get(player.getUniqueId());
        if (endTime == null) {
            return 0;
        }

        long remaining = endTime - System.currentTimeMillis();
        if (remaining <= 0) {
            cooldownMap.remove(player.getUniqueId());
            return 0;
        }

        return remaining;
    }

    @Override
    public void removeCooldown(Player player, String cooldownId) {
        Map<UUID, Long> cooldownMap = cooldowns.get(cooldownId);
        if (cooldownMap != null) {
            cooldownMap.remove(player.getUniqueId());
        }
    }

    @Override
    public void clearAllCooldowns(Player player) {
        for (Map<UUID, Long> cooldownMap : cooldowns.values()) {
            cooldownMap.remove(player.getUniqueId());
        }
    }
} 