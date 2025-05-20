package gg.lode.bookshelfapi.api.manager.impl;

import gg.lode.bookshelfapi.api.manager.IVanishManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class APIVanishManager implements IVanishManager {
    private final Plugin plugin;
    private final Set<UUID> vanishedPlayers;

    public APIVanishManager(Plugin plugin) {
        this.plugin = plugin;
        this.vanishedPlayers = new HashSet<>();
    }

    @Override
    public void vanish(Player player) {
        vanishedPlayers.add(player.getUniqueId());
        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            if (!onlinePlayer.hasPermission("bookshelf.vanish.see")) {
                onlinePlayer.hidePlayer(plugin, player);
            }
        }
    }

    @Override
    public void unvanish(Player player) {
        vanishedPlayers.remove(player.getUniqueId());
        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            onlinePlayer.showPlayer(plugin, player);
        }
    }

    @Override
    public boolean isVanished(Player player) {
        return vanishedPlayers.contains(player.getUniqueId());
    }

    @Override
    public Set<UUID> getVanishedPlayers() {
        return new HashSet<>(vanishedPlayers);
    }
} 