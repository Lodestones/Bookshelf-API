package gg.lode.bookshelfapi.api.manager.impl;

import gg.lode.bookshelfapi.api.manager.IPlayerManager;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class APIPlayerManager implements IPlayerManager {
    private final JavaPlugin plugin;

    public APIPlayerManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean hasGodMode(Player player) {
        return player.getPersistentDataContainer().has(new NamespacedKey("bookshelf", "godmode"), PersistentDataType.BOOLEAN);
    }
}