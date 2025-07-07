package gg.lode.bookshelfapi.api.menu;

import gg.lode.bookshelfapi.BookshelfAPI;
import gg.lode.bookshelfapi.api.menu.build.MenuBuilder;
import gg.lode.bookshelfapi.api.menu.build.RowBuilder;
import gg.lode.bookshelfapi.api.menu.build.TopMenuBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Menu implements InventoryHolder {

    protected Inventory inventory;
    private TopMenuBuilder topMenuBuilder;
    private MenuBuilder bottomMenuBuilder;

    protected final Player player;

    public Menu(Player player) {
        this.player = player;
    }

    protected void init() {
        this.topMenuBuilder = this.getTopMenuBuilder(new TopMenuBuilder());
        final int rows = this.topMenuBuilder.getRows();

        if (this.inventory == null)
            this.inventory = Bukkit.createInventory(null, rows * 9, this.topMenuBuilder.getTitle());

        // Top Contents
        if (topMenuBuilder.getRowBuilders().length > 6)
            throw new IllegalArgumentException("Invalid top rows created! Size must be from 1-6");

        RowBuilder @NotNull[] rowBuilders = topMenuBuilder.getRowBuilders();
        for (int y = 0; y < rowBuilders.length; y++) {
            @Nullable RowBuilder rowBuilder = rowBuilders[y];
            if (rowBuilder == null) continue;
            @NotNull RowBuilder.Slot[] slots = rowBuilder.getSlots();
            for (int x = 0; x < slots.length; x++) {
                RowBuilder.Slot slot = slots[x];
                if (slot.itemStack() == null) continue;

                this.inventory.setItem((y * 9) + x, slot.itemStack());
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
    }

    public void rebuild() {
        this.inventory.clear();
        this.init();
    }

    public void open() {
        this.init();

        topMenuBuilder.getOpenActions().forEach(Runnable::run);
        BookshelfAPI.getApi().getMenuManager().register(player.getUniqueId(), this);

        // if open inventory is the current, ignore
        if (player.getOpenInventory().getTopInventory().equals(this.inventory)) return;
        player.openInventory(this.inventory);
    }

    public void close() {
        this.player.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
    }

    public TopMenuBuilder getTopMenuBuilder() {
        return topMenuBuilder;
    }

    public MenuBuilder getBottomMenuBuilder() {
        return bottomMenuBuilder;
    }

    @NotNull protected abstract TopMenuBuilder getTopMenuBuilder(TopMenuBuilder builder);
    @Nullable protected abstract MenuBuilder getBottomMenuBuilder(MenuBuilder builder);
}
