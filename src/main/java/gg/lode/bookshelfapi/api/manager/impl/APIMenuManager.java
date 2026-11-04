package gg.lode.bookshelfapi.api.manager.impl;

import gg.lode.bookshelfapi.api.Task;
import gg.lode.bookshelfapi.api.manager.IMenuManager;
import gg.lode.bookshelfapi.api.menu.Menu;
import gg.lode.bookshelfapi.api.menu.PacketMenuHandler;
import gg.lode.bookshelfapi.api.menu.build.MenuBuilder;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
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

    /**
     * Last word on clicks into a Bookshelf menu. Runs after the consumer
     * handlers so none of them can un-cancel a locked slot.
     * <p>
     * A stale view locks everything. For the live menu, only slots the menu
     * defined are locked; untouched slots keep today's passthrough.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void guardClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        Inventory top = event.getView().getTopInventory();
        Menu menu = resolveMenu(player, top);
        if (menu == null) return;

        if (!isActive(player, menu)) {
            event.setCancelled(true);
            closeStale(player, top);
            return;
        }

        int raw = event.getRawSlot();
        if (raw < 0) return; // outside the window, only drops the cursor

        if (raw < top.getSize()) {
            if (menu.isLockedTopSlot(raw)) event.setCancelled(true);
            return;
        }

        // Bottom click. Only shift-click and collect-to-cursor reach into the
        // top, and only matter when they would hit a locked slot holding a
        // matching item.
        InventoryAction action = event.getAction();
        if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            if (touchesLockedSlot(menu, top, event.getCurrentItem())) event.setCancelled(true);
        } else if (action == InventoryAction.COLLECT_TO_CURSOR) {
            ItemStack cursor = event.getCursor();
            ItemStack target = isEmpty(cursor) ? event.getCurrentItem() : cursor;
            if (touchesLockedSlot(menu, top, target)) event.setCancelled(true);
        }
    }

    /**
     * Drags are locked the same way: any touched top slot that is locked
     * cancels the whole drag.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void guardDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        Inventory top = event.getView().getTopInventory();
        Menu menu = resolveMenu(player, top);
        if (menu == null) return;

        if (!isActive(player, menu)) {
            event.setCancelled(true);
            closeStale(player, top);
            return;
        }

        int topSize = top.getSize();
        for (int raw : event.getRawSlots()) {
            if (raw < topSize && menu.isLockedTopSlot(raw)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    /**
     * @return true if a locked top slot currently holds an item matching the
     * one a shift-click or collect would move.
     */
    private boolean touchesLockedSlot(Menu menu, Inventory top, @Nullable ItemStack item) {
        if (isEmpty(item)) return false;
        for (int raw = 0; raw < top.getSize(); raw++) {
            if (menu.isLockedTopSlot(raw) && item.isSimilar(top.getItem(raw))) return true;
        }
        return false;
    }

    private static boolean isEmpty(@Nullable ItemStack item) {
        return item == null || item.getType().isAir();
    }

    /**
     * Find the Bookshelf menu behind an open top inventory. The holder is the
     * source of truth; the registry only backs up inventories built outside
     * Menu.init.
     */
    private @Nullable Menu resolveMenu(Player player, Inventory top) {
        if (top.getHolder() instanceof Menu holder) return holder;
        Menu active = activeMenus.get(player.getUniqueId());
        return active != null && top.equals(active.getInventory()) ? active : null;
    }

    private boolean isActive(Player player, Menu menu) {
        return activeMenus.get(player.getUniqueId()) == menu;
    }

    /**
     * A menu view with no live registration. Cancel the click and close the
     * view on the next tick, unless the player has already moved on.
     */
    private void closeStale(Player player, Inventory top) {
        Task.later(plugin, () -> {
            if (player.isOnline() && top.equals(player.getOpenInventory().getTopInventory())) {
                player.closeInventory();
            }
        }, 1L);
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
        // Only the view that is the active menu. A stale view closing must not
        // tear down the menu that replaced it.
        if (menu == null || !event.getInventory().equals(menu.getInventory())) {
            return;
        }
        menu.markClosed();
        if (packetMenuHandler != null && menu.requiresPacketHandler()) {
            packetMenuHandler.onClose(menu);
        }
        menu.getTopMenuBuilder().getCloseActions().forEach(closeEvent -> closeEvent.accept(event));
        // Synchronous, and only drops this menu. Close actions may have opened
        // another menu, and that one must stay registered.
        activeMenus.remove(player.getUniqueId(), menu);
    }

    @EventHandler
    public void on(PlayerQuitEvent event) {
        Menu menu = activeMenus.remove(event.getPlayer().getUniqueId());
        if (menu == null) return;
        menu.markClosed();
        if (packetMenuHandler != null && menu.requiresPacketHandler()) {
            packetMenuHandler.onClose(menu);
        }
    }
}
