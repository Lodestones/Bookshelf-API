package gg.lode.bookshelfapi.api.manager.impl;

import gg.lode.bookshelfapi.api.manager.IGameManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class APIGameManager implements IGameManager {
    private final Plugin plugin;
    private final Map<UUID, GameState> playerGameStates;

    public APIGameManager(Plugin plugin) {
        this.plugin = plugin;
        this.playerGameStates = new HashMap<>();
    }

    @Override
    public void startGame(Player player, String gameType) {
        GameState state = new GameState(gameType);
        playerGameStates.put(player.getUniqueId(), state);
    }

    @Override
    public void endGame(Player player) {
        playerGameStates.remove(player.getUniqueId());
    }

    @Override
    public boolean isInGame(Player player) {
        return playerGameStates.containsKey(player.getUniqueId());
    }

    @Override
    public String getGameType(Player player) {
        GameState state = playerGameStates.get(player.getUniqueId());
        return state != null ? state.getGameType() : null;
    }

    @Override
    public void setGameData(Player player, String key, Object value) {
        GameState state = playerGameStates.get(player.getUniqueId());
        if (state != null) {
            state.setData(key, value);
        }
    }

    @Override
    public Object getGameData(Player player, String key) {
        GameState state = playerGameStates.get(player.getUniqueId());
        return state != null ? state.getData(key) : null;
    }

    private static class GameState {
        private final String gameType;
        private final Map<String, Object> data;

        public GameState(String gameType) {
            this.gameType = gameType;
            this.data = new HashMap<>();
        }

        public String getGameType() {
            return gameType;
        }

        public void setData(String key, Object value) {
            data.put(key, value);
        }

        public Object getData(String key) {
            return data.get(key);
        }
    }
} 