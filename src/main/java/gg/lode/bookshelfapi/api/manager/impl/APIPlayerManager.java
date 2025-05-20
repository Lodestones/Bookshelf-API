package gg.lode.bookshelfapi.api.manager.impl;

import gg.lode.bookshelfapi.api.manager.IPlayerManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class APIPlayerManager implements IPlayerManager {
    private final Plugin plugin;
    private final Map<UUID, PlayerData> playerDataMap;

    public APIPlayerManager(Plugin plugin) {
        this.plugin = plugin;
        this.playerDataMap = new HashMap<>();
    }

    @Override
    public void registerPlayer(Player player) {
        playerDataMap.put(player.getUniqueId(), new PlayerData());
    }

    @Override
    public void unregisterPlayer(Player player) {
        playerDataMap.remove(player.getUniqueId());
    }

    @Override
    public boolean isRegistered(Player player) {
        return playerDataMap.containsKey(player.getUniqueId());
    }

    @Override
    public void setPlayerData(Player player, String key, Object value) {
        PlayerData data = playerDataMap.computeIfAbsent(player.getUniqueId(), k -> new PlayerData());
        data.setData(key, value);
    }

    @Override
    public Object getPlayerData(Player player, String key) {
        PlayerData data = playerDataMap.get(player.getUniqueId());
        return data != null ? data.getData(key) : null;
    }

    private static class PlayerData {
        private final Map<String, Object> data;

        public PlayerData() {
            this.data = new HashMap<>();
        }

        public void setData(String key, Object value) {
            data.put(key, value);
        }

        public Object getData(String key) {
            return data.get(key);
        }
    }
} 