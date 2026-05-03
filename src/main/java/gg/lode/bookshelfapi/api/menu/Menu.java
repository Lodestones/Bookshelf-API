package gg.lode.bookshelfapi.api.menu;

import gg.lode.bookshelfapi.BookshelfAPI;
import gg.lode.bookshelfapi.api.manager.IMenuManager;
import gg.lode.bookshelfapi.api.menu.PacketMenuHandler;
import gg.lode.bookshelfapi.api.menu.build.MenuBuilder;
import gg.lode.bookshelfapi.api.menu.build.RowBuilder;
import gg.lode.bookshelfapi.api.menu.build.TopMenuBuilder;
import gg.lode.bookshelfapi.api.util.MiniMessageHelper;
import gg.lode.bookshelfapi.api.util.VariableContext;
import net.infumia.titleupdater.TitleUpdater;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public abstract class Menu implements InventoryHolder {

    /**
     * PDC sentinel marking the packet placeholder so the platform listener can
     * tell it apart from a real item written into the slot via inv.setItem.
     */
    public static final NamespacedKey PACKET_PLACEHOLDER_KEY =
            NamespacedKey.fromString("bookshelf:packet_placeholder");

    /**
     * Server-side stand-in for packet-only slots. Vanilla shift-click and
     * collect-to-cursor logic skips slots holding a different item, so a
     * unique placeholder keeps real player items from merging into them.
     * Tagged with {@link #PACKET_PLACEHOLDER_KEY} so the listener can detect
     * user-driven inv.setItem refreshes versus the API's own placeholder writes.
     */
    public static final ItemStack PACKET_PLACEHOLDER = createPlaceholder();

    private static ItemStack createPlaceholder() {
        ItemStack item = new ItemStack(Material.BARRIER);
        item.editPersistentDataContainer(c -> c.set(PACKET_PLACEHOLDER_KEY, PersistentDataType.BYTE, (byte) 1));
        return item;
    }

    /**
     * @return true if the given stack carries the packet-placeholder sentinel.
     */
    public static boolean isPacketPlaceholder(@Nullable ItemStack item) {
        if (item == null || item.getType() != Material.BARRIER) return false;
        return item.getPersistentDataContainer().has(PACKET_PLACEHOLDER_KEY, PersistentDataType.BYTE);
    }

    protected Inventory inventory;
    private TopMenuBuilder topMenuBuilder;
    private MenuBuilder bottomMenuBuilder;
    protected Component title;
    protected final Player player;

    public Menu(Player player) {
        this.player = player;
    }

    protected void init() {
        this.topMenuBuilder = this.getTopMenuBuilder(new TopMenuBuilder());
        final int rows = this.topMenuBuilder.getRows();

        if (this.inventory == null)
            this.inventory = Bukkit.createInventory(null, rows * 9, this.topMenuBuilder.getTitle());

        this.title = this.topMenuBuilder.getTitle();

        // Top Contents
        if (topMenuBuilder.getRowBuilders().length > 6)
            throw new IllegalArgumentException("Invalid top rows created! Size must be from 1-6");

        RowBuilder @NotNull [] rowBuilders = topMenuBuilder.getRowBuilders();
        for (int y = 0; y < rowBuilders.length; y++) {
            @Nullable RowBuilder rowBuilder = rowBuilders[y];
            if (rowBuilder == null) continue;
            @NotNull RowBuilder.Slot[] slots = rowBuilder.getSlots();
            for (int x = 0; x < slots.length; x++) {
                RowBuilder.Slot slot = slots[x];
                int rawSlot = (y * 9) + x;
                boolean packet = isPacketSlot(slot);
                if (packet) {
                    // Server inventory holds the placeholder so vanilla move logic
                    // never merges player items into a packet-only slot. The real
                    // visual is sent to the client via packet rewrite.
                    this.inventory.setItem(rawSlot, PACKET_PLACEHOLDER.clone());
                } else {
                    if (slot.itemStack() == null) {
                        this.inventory.setItem(rawSlot, null);
                    } else {
                        this.inventory.setItem(rawSlot, slot.itemStack());
                    }
                }
            }
        }

        this.bottomMenuBuilder = this.getBottomMenuBuilder(new MenuBuilder());
        if (this.bottomMenuBuilder != null) {
            RowBuilder @NotNull[] bottomRowBuilders = this.bottomMenuBuilder.getRowBuilders();
            if (bottomRowBuilders.length > 4)
                throw new IllegalArgumentException("Invalid bottom rows created! Size must be 1-4");

            for (int y = 0; y < bottomRowBuilders.length; y++) {
                @Nullable RowBuilder rowBuilder = bottomRowBuilders[y];
                if (rowBuilder == null) continue;
                RowBuilder.Slot @NotNull[] slots = rowBuilder.getSlots();
                for (int x = 0; x < slots.length; x++) {
                    @NotNull RowBuilder.Slot slot = slots[x];
                    // Packet-flagged bottom slots never touch the player's
                    // real inventory — visuals are sent via packet rewrite.
                    if (slot.packet() != null && slot.packet()) continue;
                    if (slot.itemStack() == null) continue;

                    player.getInventory().setItem((y * 9) + x, slot.itemStack());
                }
            }
        }
    }

    @Override
    @NotNull
    public Inventory getInventory() {
        return this.inventory;
    }

    public void update() {
        this.init();
        PacketMenuHandler handler = packetHandlerOrNull();
        if (handler != null && requiresPacketHandler()) {
            handler.onUpdate(this);
        }
    }

    public void rebuild() {
        this.inventory.clear();
        this.init();
        PacketMenuHandler handler = packetHandlerOrNull();
        if (handler != null && requiresPacketHandler()) {
            handler.onUpdate(this);
        }
    }

    /**
     * @return true if this menu needs the packet pipeline — either the top is
     * packet-based, or the bottom has at least one packet-flagged slot.
     */
    public boolean requiresPacketHandler() {
        if (topMenuBuilder != null && topMenuBuilder.isPacketBased()) return true;
        return hasAnyBottomPacketSlot();
    }

    private @Nullable PacketMenuHandler packetHandlerOrNull() {
        IMenuManager mm = BookshelfAPI.getApi() != null ? BookshelfAPI.getApi().getMenuManager() : null;
        return mm != null ? mm.getPacketMenuHandler() : null;
    }

    public void setTitle(String str) {
        setTitle(str, new VariableContext());
    }

    public void setTitle(String str, VariableContext context) {
        this.title = MiniMessageHelper.deserialize(str, context);
        TitleUpdater.update(player, this.title);
    }

    public void open() {
        this.init();

        IMenuManager menuManager = BookshelfAPI.getApi().getMenuManager();
//        final Menu activeMenu = menuManager.getActiveMenu(player);

        topMenuBuilder.getOpenActions().forEach(Runnable::run);
        menuManager.register(player.getUniqueId(), this);

        PacketMenuHandler handler = menuManager.getPacketMenuHandler();

        // Pre-mark for packet capture so the platform listener can record
        // the windowId from the next outbound OpenWindow packet. Required for
        // top packet-based menus AND any menu using packet bottom slots.
        if (handler != null && requiresPacketHandler()) {
            handler.onOpen(this);
        }

        // if open inventory is the current, ignore
        if (player.getOpenInventory().getTopInventory().equals(this.inventory)) return;

//        // If the open inventory is the same size as the intended inventory.
//        // Then we want to override the inventory and shit, so it's seamless
//        if (activeMenu != null && activeMenu.inventory.getSize() == this.inventory.getSize()) {
//            inventory = activeMenu.getInventory();
//            TitleUpdater.update(player, this.title);
//            rebuild();
//            return;
//        }

        player.openInventory(this.inventory);
    }

    public void close() {
        PacketMenuHandler handler = packetHandlerOrNull();
        if (handler != null && requiresPacketHandler()) {
            handler.onClose(this);
        }
        this.player.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
    }

    public TopMenuBuilder getTopMenuBuilder() {
        return topMenuBuilder;
    }

    public Player getPlayer() {
        return player;
    }

    public MenuBuilder getBottomMenuBuilder() {
        return bottomMenuBuilder;
    }

    /**
     * @return true if the given top-inventory raw slot is a packet-only slot.
     */
    public boolean isPacketSlot(int rawSlot) {
        if (topMenuBuilder == null) return false;
        int y = rawSlot / 9;
        int x = rawSlot % 9;
        RowBuilder[] rows = topMenuBuilder.getRowBuilders();
        if (y < 0 || y >= rows.length) return false;
        RowBuilder rb = rows[y];
        if (rb == null) return false;
        RowBuilder.Slot[] slots = rb.getSlots();
        if (x < 0 || x >= slots.length) return false;
        return isPacketSlot(slots[x]);
    }

    /**
     * @return the item that should be displayed to the client at this raw slot
     * if it is a packet slot; otherwise null (let server inventory state show through).
     */
    public @Nullable ItemStack getPacketDisplayItem(int rawSlot) {
        if (topMenuBuilder == null) return null;
        int y = rawSlot / 9;
        int x = rawSlot % 9;
        RowBuilder[] rows = topMenuBuilder.getRowBuilders();
        if (y < 0 || y >= rows.length) return null;
        RowBuilder rb = rows[y];
        if (rb == null) return null;
        RowBuilder.Slot[] slots = rb.getSlots();
        if (x < 0 || x >= slots.length) return null;
        RowBuilder.Slot slot = slots[x];
        return isPacketSlot(slot) ? slot.itemStack() : null;
    }

    private boolean isPacketSlot(RowBuilder.Slot slot) {
        if (slot.packet() != null) return slot.packet();
        return topMenuBuilder != null && topMenuBuilder.isPacketBased();
    }

    /**
     * Map a bottom-inventory window raw slot to the {@link MenuBuilder}
     * row/col coordinates used by Bookshelf. Bottom rows in MenuBuilder are
     * laid out in player-inventory order (row 0 = hotbar), while window slots
     * place the main inventory before the hotbar.
     *
     * @return [row, col] in MenuBuilder space, or null if out of range.
     */
    private int @Nullable [] windowToBottomRowCol(int rawSlot) {
        if (topMenuBuilder == null) return null;
        int topSize = topMenuBuilder.getRows() * 9;
        int bottomIdx = rawSlot - topSize;
        if (bottomIdx < 0 || bottomIdx >= 36) return null;
        int playerSlot = bottomIdx < 27 ? bottomIdx + 9 : bottomIdx - 27;
        return new int[] { playerSlot / 9, playerSlot % 9 };
    }

    private @Nullable RowBuilder.Slot bottomSlotAt(int rawSlot) {
        if (bottomMenuBuilder == null) return null;
        int[] rc = windowToBottomRowCol(rawSlot);
        if (rc == null) return null;
        RowBuilder[] rows = bottomMenuBuilder.getRowBuilders();
        if (rc[0] < 0 || rc[0] >= rows.length) return null;
        RowBuilder rb = rows[rc[0]];
        if (rb == null) return null;
        RowBuilder.Slot[] slots = rb.getSlots();
        if (rc[1] < 0 || rc[1] >= slots.length) return null;
        return slots[rc[1]];
    }

    /**
     * @return true if the given window raw slot is a packet-flagged bottom slot.
     */
    public boolean isBottomPacketSlot(int rawSlot) {
        RowBuilder.Slot slot = bottomSlotAt(rawSlot);
        return slot != null && slot.packet() != null && slot.packet();
    }

    /**
     * @return the visual to show the client for a packet bottom slot, or null
     * if not a packet slot. A non-null return with a null inner item means
     * "hide the slot" — listener should send {@code ItemStack.EMPTY}.
     */
    public @Nullable ItemStack getBottomPacketDisplayItem(int rawSlot) {
        RowBuilder.Slot slot = bottomSlotAt(rawSlot);
        if (slot == null || slot.packet() == null || !slot.packet()) return null;
        return slot.itemStack();
    }

    /**
     * @return click handler attached to the bottom slot at the given window raw
     * slot, or null if none / not a packet slot.
     */
    public @Nullable Consumer<org.bukkit.event.inventory.InventoryClickEvent> getBottomClickHandler(int rawSlot) {
        RowBuilder.Slot slot = bottomSlotAt(rawSlot);
        return slot == null ? null : slot.event();
    }

    /**
     * @return true if the bottom builder has at least one packet-flagged slot.
     * Used by the listener to decide whether to police shift-clicks within the
     * bottom inventory.
     */
    public boolean hasAnyBottomPacketSlot() {
        if (bottomMenuBuilder == null) return false;
        for (RowBuilder rb : bottomMenuBuilder.getRowBuilders()) {
            if (rb == null) continue;
            for (RowBuilder.Slot s : rb.getSlots()) {
                if (s.packet() != null && s.packet()) return true;
            }
        }
        return false;
    }

    /**
     * Overlay an empty bottom inventory on the client without disturbing the
     * player's real items. Replaces the bottom builder with a {@link MenuBuilder}
     * whose 36 slots are all empty packet slots. The platform listener will
     * resend the menu so the change reflects immediately.
     */
    public void hideBottomInventory() {
        this.bottomMenuBuilder = new MenuBuilder().hideAll();
        PacketMenuHandler handler = packetHandlerOrNull();
        if (handler != null) handler.onUpdate(this);
    }

    /**
     * Restore the player's real bottom inventory visuals, reverting any
     * {@link #hideBottomInventory()} or partial packet overlay. Pushes the real
     * server-side player items back to the client.
     */
    public void restoreBottomInventory() {
        this.bottomMenuBuilder = null;
        if (player != null && player.isOnline()) player.updateInventory();
    }

    /**
     * Replace the visual on a packet slot in {@link #topMenuBuilder}, preserving
     * the existing click handler and packet flag. Called by the platform listener
     * when it sees a server-driven {@code inv.setItem} refresh on a packet slot,
     * so subsequent {@link #getPacketDisplayItem(int)} lookups return the new item.
     *
     * @return true if the slot existed and was updated.
     */
    public boolean syncSlotFromServer(int rawSlot, @Nullable ItemStack newItem) {
        if (topMenuBuilder == null) return false;
        int y = rawSlot / 9;
        int x = rawSlot % 9;
        RowBuilder[] rows = topMenuBuilder.getRowBuilders();
        if (y < 0 || y >= rows.length) return false;
        RowBuilder rb = rows[y];
        if (rb == null) return false;
        RowBuilder.Slot[] slots = rb.getSlots();
        if (x < 0 || x >= slots.length) return false;
        RowBuilder.Slot existing = slots[x];
        Consumer<org.bukkit.event.inventory.InventoryClickEvent> handler = existing.event();
        Boolean packetFlag = existing.packet();
        if (newItem == null) {
            rb.setSlot(x, new ItemStack(Material.AIR), handler, packetFlag);
        } else {
            rb.setSlot(x, newItem, handler, packetFlag);
        }
        return true;
    }

    @NotNull protected abstract TopMenuBuilder getTopMenuBuilder(TopMenuBuilder builder);
    @Nullable protected abstract MenuBuilder getBottomMenuBuilder(MenuBuilder builder);
}
