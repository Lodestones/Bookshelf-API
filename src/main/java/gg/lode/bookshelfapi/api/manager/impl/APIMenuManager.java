package gg.lode.bookshelfapi.api.manager.impl;

import gg.lode.bookshelfapi.api.Task;
import gg.lode.bookshelfapi.api.manager.IMenuManager;
import gg.lode.bookshelfapi.api.menu.Menu;
import gg.lode.bookshelfapi.api.menu.PacketMenuHandler;
import gg.lode.bookshelfapi.api.menu.build.MenuBuilder;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.UUID;

public class APIMenuManager implements IMenuManager, Listener {
    private static final HashMap<UUID, Menu> activeMenus = new HashMap<>();
    private final JavaPlugin plugin;
    private @Nullable PacketMenuHandler packetMenuHandler;

    public APIMenuManager(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void register(Player player, Menu menu) {
        this.register(player.getUniqueId(), menu);
    }

    @Override
    public void register(UUID uniqueId, Menu menu) {
        if (activeMenus.containsKey(uniqueId)) {
            Menu activeMenu = activeMenus.remove(uniqueId);
//            if (activeMenu.getInventory().getSize() != menu.getInventory().getSize())
//                activeMenu.close();
        }

        activeMenus.put(uniqueId, menu);
    }

    @Override
    public void registerAndOpen(Player player, Menu menu) {
        this.registerAndOpen(player.getUniqueId(), menu);
    }

    @Override
    public void registerAndOpen(UUID uniqueId, Menu menu) {
        this.register(uniqueId, menu);
        menu.open();
    }

    @Nullable
    @Override
    public Menu getActiveMenu(Player player) {
        return getActiveMenu(player.getUniqueId());
    }

    @Override
    @Nullable
    public Menu getActiveMenu(UUID uniqueId) {
        return activeMenus.get(uniqueId);
    }

    @EventHandler
    public void on(InventoryClickEvent event) {
        HumanEntity humanEntity = event.getWhoClicked();
        if (!(humanEntity instanceof Player player)) {
            return;
        }
        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null) {
            return;
        }
        dispatchClick(player, event);
    }

    @Override
    public void dispatchClick(Player player, InventoryClickEvent event) {
        InventoryView view = event.getView();
        Inventory clickedInventory = event.getClickedInventory();
        if (!activeMenus.containsKey(player.getUniqueId())) return;

        int slot = event.getSlot();
        Menu menu = activeMenus.get(player.getUniqueId());
        // Fire top menu click actions for all clicks (top and bottom inventory)
        menu.getTopMenuBuilder().getClickActions().forEach(c -> c.accept(event));

        if (clickedInventory != null && view.getTopInventory().equals(clickedInventory)) {
            menu.getTopMenuBuilder().process(slot, event);
        } else if (clickedInventory != null && view.getBottomInventory().equals(clickedInventory)) {
            MenuBuilder bottomMenuBuilder = menu.getBottomMenuBuilder();
            if (bottomMenuBuilder == null) {
                return;
            }
            bottomMenuBuilder.process(slot, event);
        }
    }

    @Override
    public void setPacketMenuHandler(@Nullable PacketMenuHandler handler) {
        this.packetMenuHandler = handler;
    }

    @Override
    public @Nullable PacketMenuHandler getPacketMenuHandler() {
        return packetMenuHandler;
    }

    @EventHandler
    public void on(InventoryCloseEvent event) {
        HumanEntity humanEntity = event.getPlayer();
        if (!(humanEntity instanceof Player player)) {
            return;
        }
        // OPEN_NEW: another Menu.open() already ran register() for the
        // incoming menu and primed packet state via onOpen(). activeMenus.get()
        // here returns the NEW menu, so dispatching close actions or packet
        // cleanup against it would fire the wrong handlers and wipe the new
        // menu's freshly added pendingOpens/activeWindows entries — the cause
        // of the second menu rendering as BARRIER placeholders.
        if (event.getReason().equals(InventoryCloseEvent.Reason.OPEN_NEW)) {
            return;
        }
        Menu menu = activeMenus.get(player.getUniqueId());
        if (menu == null) {
            return;
        }
        menu.getTopMenuBuilder().getCloseActions().forEach(closeEvent -> closeEvent.accept(event));
        if (packetMenuHandler != null && menu.getTopMenuBuilder().isPacketBased()) {
            packetMenuHandler.onClose(menu);
        }
        Task.later(this.plugin, () -> activeMenus.remove(player.getUniqueId()), 1L);
    }

    @EventHandler
    public void on(PlayerQuitEvent event) {
        Menu menu = activeMenus.get(event.getPlayer().getUniqueId());
        if (menu != null && packetMenuHandler != null && menu.getTopMenuBuilder().isPacketBased()) {
            packetMenuHandler.onClose(menu);
        }
        activeMenus.remove(event.getPlayer().getUniqueId());
    }
}