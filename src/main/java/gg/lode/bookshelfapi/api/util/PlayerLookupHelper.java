package gg.lode.bookshelfapi.api.util;

import gg.lode.bookshelfapi.api.mojang.MojangProfile;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerLookupHelper {
    /**
     * Resolves a player name to UUID using Mojang API. Returns null if not found or error.
     */
    public static UUID resolvePlayerUUID(String name) {
        // Try local cache first
        OfflinePlayer offline = Bukkit.getOfflinePlayer(name);
        UUID uuid = offline.getUniqueId();
        if (offline.getName() != null && !uuid.equals(new UUID(0, 0))) {
            return uuid;
        }

        // Fallback to Mojang API
        try {
            MojangProfile profile = MojangProfile.getMojangProfile(name);
            return profile != null ? profile.getUniqueId() : null;
        } catch (IOException | org.json.simple.parser.ParseException e) {
            return null;
        }
    }

    /**
     * Resolves a player name to UUID using Mojang API. Returns null if not found or error.
     */
    public static String resolvePlayerName(UUID uniqueId) {
        // Try local cache first
        OfflinePlayer offline = Bukkit.getOfflinePlayer(uniqueId);
        UUID uuid = offline.getUniqueId();
        if (offline.getName() != null && !uuid.equals(new UUID(0, 0))) {
            return offline.getName();
        }

        // Fallback to Mojang API
        try {
            MojangProfile profile = MojangProfile.getMojangProfileFromUUID(uniqueId.toString());
            return profile != null ? profile.getName() : null;
        } catch (IOException | org.json.simple.parser.ParseException e) {
            return null;
        }
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