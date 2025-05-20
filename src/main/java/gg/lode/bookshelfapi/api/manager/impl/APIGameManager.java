package gg.lode.bookshelfapi.api.manager.impl;

import gg.lode.bookshelfapi.api.Configuration;
import gg.lode.bookshelfapi.api.manager.IGameManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class APIGameManager implements IGameManager, Listener {
    private final JavaPlugin plugin;
    private final Configuration configFile;

    public APIGameManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.configFile = new Configuration(plugin, "config.yml");
        if (!this.configFile.initialize()) {
            plugin.getLogger().severe("Failed to initialize config.yml file!");
        }
        plugin.getServer().getPluginManager().registerEvents(this, (Plugin) plugin);
    }

    @EventHandler
    public void on(EntityDamageByEntityEvent event) {
        Projectile projectile;
        Entity entity;
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player && !this.isPVPEnabled()
                || (entity = event.getDamager()) instanceof Projectile
                && (projectile = (Projectile) entity).getShooter() instanceof Player
                && event.getEntity() instanceof Player && !this.isPVPEnabled()) {
            event.setCancelled(true);
        }
    }

    @Override
    public boolean isPVPEnabled() {
        return this.configFile.getBoolean("config.is_pvp_enabled");
    }

    @Override
    public void setPVPEnabled(boolean value) {
        this.configFile.set("config.is_pvp_enabled", value);
        this.configFile.save();
    }

}