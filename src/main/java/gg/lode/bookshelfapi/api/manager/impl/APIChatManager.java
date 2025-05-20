package gg.lode.bookshelfapi.api.manager.impl;

import gg.lode.bookshelfapi.api.manager.IChatManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class APIChatManager implements IChatManager {
    private final Plugin plugin;
    private final Map<UUID, ChatState> playerChatStates;
    private final Map<String, Consumer<Player>> chatHandlers;

    public APIChatManager(Plugin plugin) {
        this.plugin = plugin;
        this.playerChatStates = new HashMap<>();
        this.chatHandlers = new HashMap<>();
    }

    @Override
    public void startChatSession(Player player, String sessionId) {
        ChatState state = new ChatState(sessionId);
        playerChatStates.put(player.getUniqueId(), state);
    }

    @Override
    public void endChatSession(Player player) {
        playerChatStates.remove(player.getUniqueId());
    }

    @Override
    public boolean isInChatSession(Player player) {
        return playerChatStates.containsKey(player.getUniqueId());
    }

    @Override
    public String getCurrentSessionId(Player player) {
        ChatState state = playerChatStates.get(player.getUniqueId());
        return state != null ? state.getSessionId() : null;
    }

    @Override
    public void registerChatHandler(String sessionId, Consumer<Player> handler) {
        chatHandlers.put(sessionId, handler);
    }

    @Override
    public void unregisterChatHandler(String sessionId) {
        chatHandlers.remove(sessionId);
    }

    @Override
    public void handleChatMessage(Player player, String message) {
        ChatState state = playerChatStates.get(player.getUniqueId());
        if (state != null) {
            Consumer<Player> handler = chatHandlers.get(state.getSessionId());
            if (handler != null) {
                handler.accept(player);
            }
        }
    }

    private static class ChatState {
        private final String sessionId;

        public ChatState(String sessionId) {
            this.sessionId = sessionId;
        }

        public String getSessionId() {
            return sessionId;
        }
    }
} 