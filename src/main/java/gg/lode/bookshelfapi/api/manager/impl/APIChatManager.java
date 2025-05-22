package gg.lode.bookshelfapi.api.manager.impl;

import gg.lode.bookshelfapi.api.Task;
import gg.lode.bookshelfapi.api.event.PlayerChatEvent;
import gg.lode.bookshelfapi.api.manager.IChatManager;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class APIChatManager implements Listener, IChatManager {
    private final JavaPlugin plugin;

    public APIChatManager(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void on(AsyncChatEvent event) {
        if (event.isCancelled())
            return;

        Player player = event.getPlayer();
        PlayerChatEvent playerChatEvent = new PlayerChatEvent(player, event.message());
        Task.later(plugin, playerChatEvent::callEvent, 1L);
        event.setCancelled(true);
    }

    @Override
    public boolean isChatMuted() {
        throw new UnsupportedOperationException("This method is exclusive when Bookshelf is installed in your server. Not as a shaded dependency.");
    }

    @Override
    public void setChatMuted(boolean value) {
        throw new UnsupportedOperationException("This method is exclusive when Bookshelf is installed in your server. Not as a shaded dependency.");
    }

    @Override
    public boolean canPlayerSpeak(UUID uniqueId) {
        throw new UnsupportedOperationException("This method is exclusive when Bookshelf is installed in your server. Not as a shaded dependency.");
    }

}