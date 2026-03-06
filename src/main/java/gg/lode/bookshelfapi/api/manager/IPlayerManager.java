package gg.lode.bookshelfapi.api.manager;

import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IPlayerManager {

    boolean hasGodMode(Player player);
    void setGodMode(Player player, boolean value);
    String getCurrentChatChannel(Player player);
    void setChatChannel(Player player, String channel);

    // --- Privacy ---

    CompletableFuture<Boolean> canReceiveMessages(UUID player);
    CompletableFuture<Void> setCanReceiveMessages(UUID player, boolean canReceive);

    CompletableFuture<List<UUID>> getBlockedPlayers(UUID player);
    CompletableFuture<Void> addBlockedPlayer(UUID player, UUID blocked);
    CompletableFuture<Void> removeBlockedPlayer(UUID player, UUID blocked);

    // --- Social Spy ---

    CompletableFuture<Boolean> isSocialSpyEnabled(UUID player);
    CompletableFuture<Void> setSocialSpyEnabled(UUID player, boolean enabled);

    // --- Whisper ---

    CompletableFuture<UUID> getLatestWhisper(UUID player);
    CompletableFuture<Void> setLatestWhisper(UUID player, UUID target);

}
