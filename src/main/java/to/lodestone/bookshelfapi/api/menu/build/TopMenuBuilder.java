package to.lodestone.bookshelfapi.api.menu.build;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import to.lodestone.bookshelfapi.api.util.MiniMessageUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TopMenuBuilder extends MenuBuilder {

    private Component title = Component.empty();
    private int rows;

    protected List<Consumer<InventoryCloseEvent>> closeActions;
    protected List<Consumer<InventoryOpenEvent>> openActions;
    protected List<Consumer<InventoryClickEvent>> clickActions;

    public TopMenuBuilder() {
        this.closeActions = new ArrayList<>();
        this.openActions = new ArrayList<>();
        this.clickActions = new ArrayList<>();
    }

    public TopMenuBuilder setTitle(Component title) {
        this.title = title;
        return this;
    }

    public TopMenuBuilder addCloseAction(Consumer<InventoryCloseEvent> consumer) {
        this.closeActions.add(consumer);
        return this;
    }

    public TopMenuBuilder addOpenAction(Consumer<InventoryOpenEvent> consumer) {
        this.openActions.add(consumer);
        return this;
    }

    public TopMenuBuilder addClickAction(Consumer<InventoryClickEvent> consumer) {
        this.clickActions.add(consumer);
        return this;
    }

    public TopMenuBuilder setRows(int rows) {
        if ((rows > 6 || rows <= 0))
            throw new IllegalArgumentException("Invalid row size! Must be from 1-6");

        this.rows = rows;
        this.rowBuilders = new RowBuilder[rows];
        return this;
    }

    public TopMenuBuilder outline(ItemStack outlineItem) {
        if (this.rowBuilders == null) {
            throw new IllegalArgumentException("Use TopMenuBuilder#setRows before using this method!");
        }

        // Add top components to the top and bottom edges
        for (int y = 0; y < rows; y++) {
            int finalY = y;
            buildRow(y, rowBuilder -> {
                for (int x = 0; x < 9; x++) {
                    if (finalY == 0 || finalY == rows - 1 || x == 0 || x == 8)
                        rowBuilder.setSlot(x, outlineItem, event -> event.setCancelled(true));
                }
            });
        }

        return this;
    }

    public TopMenuBuilder fill(ItemStack fillContent) {
        if (this.rowBuilders == null) {
            throw new IllegalArgumentException("Use TopMenuBuilder#setRows before using this method!");
        }

        for (int y = 0; y < rows; y++) {
            buildRow(y, rowBuilder -> {
                for (int x = 0; x < 9; x++) {
                    rowBuilder.setSlot(x, fillContent);
                }
            });
        }

        return this;
    }

    public TopMenuBuilder setTitle(String title, Object... args) {
        this.title = MiniMessageUtil.deserialize(title, args);
        return this;
    }

    @Override
    public TopMenuBuilder buildRow(int index, Consumer<RowBuilder> consumer) {
        if (this.rowBuilders == null) {
            throw new IllegalArgumentException("Use TopMenuBuilder#setRows before using this method!");
        }

        if ((rows > 6 || rows <= 0) || index > rows)
            throw new IllegalArgumentException(String.format("Invalid row size! Must be inserting from 1-%s", rows));

        return (TopMenuBuilder) super.buildRow(index, consumer);
    }

    public TopMenuBuilder editRow(int rowIndex, Consumer<RowBuilder> consumer) {
        consumer.accept(this.rowBuilders[rowIndex]);
        return this;
    }

    public TopMenuBuilder insertInRow(int rowIndex, int slot, ItemStack itemStack) {
        if ((rows > 6 || rows <= 0) || rowIndex > rows - 1)
            throw new IllegalArgumentException(String.format("Invalid row size! Must be inserting from 1-%s", rows));

        return insertInRow(rowIndex, slot, itemStack, null);
    }

    public TopMenuBuilder insertInRow(int rowIndex, int slot, ItemStack itemStack, Consumer<InventoryClickEvent> consumer) {
        if (this.rowBuilders == null) {
            throw new IllegalArgumentException("Use TopMenuBuilder#setRows before using this method!");
        }

        if ((rows > 6 || rows <= 0) || rowIndex > rows - 1)
            throw new IllegalArgumentException(String.format("Invalid row size! Must be inserting from 1-%s", rows));

        this.rowBuilders[rowIndex].setSlot(slot, itemStack, consumer);
        return this;
    }

    public Component getTitle() {
        return title;
    }

    public List<Consumer<InventoryOpenEvent>> getOpenActions() {
        return openActions;
    }

    public List<Consumer<InventoryCloseEvent>> getCloseActions() {
        return closeActions;
    }

    public List<Consumer<InventoryClickEvent>> getClickActions() {
        return clickActions;
    }

    public int getRows() {
        if ((rows > 6 || rows <= 0))
            throw new IllegalArgumentException("Invalid row size! Must be from 1-6");

        return rows;
    }
}
