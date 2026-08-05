package gg.lode.bookshelfapi.api.util;

import gg.lode.bookshelfapi.api.mojang.MojangProfile;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public class PlayerLookupHelper {

    private static Function<String, UUID> nameToUUIDFallback;
    private static Function<UUID, String> uuidToNameFallback;

    /**
     * Registers a fallback resolver for name→UUID lookups (e.g. MongoDB username history).
     */
    public static void setNameToUUIDFallback(Function<String, UUID> fallback) {
        nameToUUIDFallback = fallback;
    }

    /**
     * Registers a fallback resolver for UUID→name lookups (e.g. MongoDB username history).
     */
    public static void setUUIDToNameFallback(Function<UUID, String> fallback) {
        uuidToNameFallback = fallback;
    }

    /**
     * Resolves a player name to UUID using Mojang API. Returns null if not found or error.
     */
    public static UUID resolvePlayerUUID(String name) {
        // Try local cache first, but only trust a real, previously-seen profile.
        // For a name that was recently changed away from, Bukkit.getOfflinePlayer(name)
        // fabricates an offline-mode UUID (getName() != null, UUID != zero) that would
        // shadow the DB name-history fallback below. hasPlayedBefore() weeds those out.
        OfflinePlayer offline = Bukkit.getOfflinePlayer(name);
        UUID uuid = offline.getUniqueId();
        if ((offline.isOnline() || offline.hasPlayedBefore())
                && offline.getName() != null && !uuid.equals(new UUID(0, 0))) {
            return uuid;
        }

        // Fallback to Mojang API
        try {
            MojangProfile profile = MojangProfile.getMojangProfile(name);
            if (profile != null) return profile.getUniqueId();
        } catch (IOException e) {
            // Continue to next fallback
        }

        // Fallback to registered resolver (e.g. MongoDB username history)
        if (nameToUUIDFallback != null) {
            return nameToUUIDFallback.apply(name);
        }

        return null;
    }

    /**
     * Resolves a UUID to a player name. Returns null if not found or on error.
     *
     * @param uniqueId the player's UUID. A null is treated as "unknown" and
     *                 answered with null, rather than passed through to
     *                 {@link Bukkit#getOfflinePlayer(UUID)}, which throws on
     *                 null. Callers reading UUIDs out of storage cannot always
     *                 guarantee one is present, and a missing name should not
     *                 take down the command asking for it.
     */
    public static String resolvePlayerName(UUID uniqueId) {
        if (uniqueId == null) return null;

        // Try local cache first
        OfflinePlayer offline = Bukkit.getOfflinePlayer(uniqueId);
        UUID uuid = offline.getUniqueId();
        if (offline.getName() != null && !uuid.equals(new UUID(0, 0))) {
            return offline.getName();
        }

        // Fallback to Mojang API
        try {
            MojangProfile profile = MojangProfile.getMojangProfileFromUUID(uniqueId.toString());
            if (profile != null) return profile.getName();
        } catch (IOException e) {
            // Continue to next fallback
        }

        // Fallback to registered resolver (e.g. MongoDB username history)
        if (uuidToNameFallback != null) {
            return uuidToNameFallback.apply(uniqueId);
        }

        return null;
    }

    /**
     * Returns a list of online player names on this server for autocompletion.
     */
    public static List<String> getOnlinePlayerNames() {
        List<String> names = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            names.add(p.getName());
        }
        return names;
    }
}
