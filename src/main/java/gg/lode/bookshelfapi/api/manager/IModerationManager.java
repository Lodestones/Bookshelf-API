package gg.lode.bookshelfapi.api.manager;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IModerationManager {

    // --- Mute ---

    CompletableFuture<Boolean> isMuted(UUID player);

    CompletableFuture<Void> mute(UUID player, String reason, @Nullable CommandSender issuer, long duration);

    CompletableFuture<Void> unmute(UUID player, String reason, @Nullable CommandSender issuer);

    // --- Ban ---

    CompletableFuture<Boolean> isBanned(UUID player);

    CompletableFuture<Void> ban(UUID player, String reason, @Nullable CommandSender issuer, long duration);

    CompletableFuture<Void> unban(UUID player, String reason, @Nullable CommandSender issuer);

    // --- IP Ban ---

    CompletableFuture<Boolean> isIPBanned(String ip);

    CompletableFuture<Void> banIP(String ip, String reason, @Nullable CommandSender issuer, long duration);

    CompletableFuture<Void> unbanIP(String ip, String reason, @Nullable CommandSender issuer);

    // --- Kick ---

    CompletableFuture<Void> kick(UUID player, String reason, @Nullable CommandSender issuer);

    // --- Warn ---

    CompletableFuture<Void> warn(UUID player, String reason, @Nullable CommandSender issuer);

    CompletableFuture<Boolean> hasWarnings(UUID player);

    // --- Query ---

    CompletableFuture<Boolean> hasInfractions(UUID player);

    CompletableFuture<Boolean> hasRecentInfractions(UUID player, long duration);

    CompletableFuture<List<Infraction>> getInfractions(UUID player);

    CompletableFuture<List<Infraction>> getActiveInfractions(UUID player);

    // --- Alts ---

    CompletableFuture<List<UUID>> getAlts(UUID player);

    CompletableFuture<String> getLastKnownIP(UUID player);

    record Infraction(UUID player, String type, String reason, String issuer,
                      long issuedAt, long expiresAt, boolean active) {
    }
}
