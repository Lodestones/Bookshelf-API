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
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Menu implements InventoryHolder {

    /**
     * Server-side stand-in for packet-only slots. Vanilla shift-click and
     * collect-to-cursor logic skips slots holding a different item, so a
     * unique placeholder keeps real player items from merging into them.
     */
    public static final ItemStack PACKET_PLACEHOLDER = new ItemStack(Material.BARRIER);

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
        if (handler != null && this.topMenuBuilder != null && this.topMenuBuilder.isPacketBased()) {
            handler.onUpdate(this);
        }
    }

    public void rebuild() {
        this.inventory.clear();
        this.init();
        PacketMenuHandler handler = packetHandlerOrNull();
        if (handler != null && this.topMenuBuilder != null && this.topMenuBuilder.isPacketBased()) {
            handler.onUpdate(this);
        }
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

        boolean packetBased = topMenuBuilder.isPacketBased();
        PacketMenuHandler handler = menuManager.getPacketMenuHandler();

        // Pre-mark for packet capture so the platform listener can record
        // the windowId from the next outbound OpenWindow packet.
        if (packetBased && handler != null) {
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
        if (handler != null && this.topMenuBuilder != null && this.topMenuBuilder.isPacketBased()) {
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

    @NotNull protected abstract TopMenuBuilder getTopMenuBuilder(TopMenuBuilder builder);
    @Nullable protected abstract MenuBuilder getBottomMenuBuilder(MenuBuilder builder);
}
