package gg.lode.bookshelfapi.api.manager.impl;

import gg.lode.bookshelfapi.api.Task;
import gg.lode.bookshelfapi.api.manager.IMenuManager;
import gg.lode.bookshelfapi.api.menu.Menu;
import gg.lode.bookshelfapi.api.menu.build.MenuBuilder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

public class APIMenuManager implements IMenuManager, Listener {

    private final JavaPlugin plugin;
    private static final HashMap<UUID, Menu> activeMenus = new HashMap<>();

    public APIMenuManager(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void register(Player player, Menu menu) {
        register(player.getUniqueId(), menu);
    }

    @Override
    public void register(UUID uniqueId, Menu menu) {
        if (activeMenus.containsKey(uniqueId)) {
            activeMenus.remove(uniqueId).close();
        }

        activeMenus.put(uniqueId, menu);
    }

    @Override
    public void registerAndOpen(Player player, Menu menu) {
        registerAndOpen(player.getUniqueId(), menu);
    }

    @Override
    public void registerAndOpen(UUID uniqueId, Menu menu) {
        register(uniqueId, menu);
        menu.open();
    }

    @EventHandler
    public void on(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null)
            return;

        InventoryView view = event.getView();

        if (activeMenus.containsKey(player.getUniqueId())) {
            final int slot = event.getSlot();
            Menu menu = activeMenus.get(player.getUniqueId());

            if (view.getTopInventory().equals(clickedInventory)) {
                menu.getTopMenuBuilder().getClickActions().forEach(c -> c.accept(event));
                menu.getTopMenuBuilder().process(slot, event);
            } else if (view.getBottomInventory().equals(clickedInventory)) {
                MenuBuilder bottomMenuBuilder = menu.getBottomMenuBuilder();
                if (bottomMenuBuilder == null) return;

                bottomMenuBuilder.process(slot, event);
            }
        }
    }

    @EventHandler
    public void on(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        Menu menu = activeMenus.get(player.getUniqueId());
        if (menu == null)
            return;

        menu.getTopMenuBuilder().getCloseActions().forEach(closeEvent -> closeEvent.accept(event));
        if (!event.getReason().equals(InventoryCloseEvent.Reason.OPEN_NEW)) Task.later(plugin, () -> activeMenus.remove(player.getUniqueId()), 1L);
    }

    @EventHandler
    public void on(PlayerQuitEvent event) {
        activeMenus.remove(event.getPlayer().getUniqueId());
    }
} 