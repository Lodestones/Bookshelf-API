package gg.lode.bookshelfapi.api.menu.build;

import gg.lode.bookshelfapi.api.util.MiniMessageHelper;
import net.kyori.adventure.text.Component;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TopMenuBuilder extends MenuBuilder {

    private Component title = Component.empty();
    private int rows;

    protected List<Consumer<InventoryCloseEvent>> closeActions;
    protected List<Runnable> openActions;
    protected List<Consumer<InventoryClickEvent>> clickActions;

    public TopMenuBuilder() {
        super();
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

    public TopMenuBuilder addOpenAction(Runnable consumer) {
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
        for (int i = 0; i < rows; i++) this.rowBuilders[i] = new RowBuilder();
        return this;
    }

    public TopMenuBuilder outline(ItemStack outlineItem) {
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
        this.title = MiniMessageHelper.deserialize(title, args);
        return this;
    }

    @Override
    public TopMenuBuilder buildRow(int index, Consumer<RowBuilder> consumer) {
        if ((rows > 6 || rows <= 0) || index > rows)
            throw new IllegalArgumentException(String.format("Invalid row size! Must be inserting from 1-%s", rows));

        return (TopMenuBuilder) super.buildRow(index, consumer);
    }

    public TopMenuBuilder editRow(int rowIndex, Consumer<RowBuilder> consumer) {
        if (this.rowBuilders.length < rowIndex) throw new IllegalArgumentException("Invalid row index! Must be from 0-" + (this.rowBuilders.length - 1));
        consumer.accept(this.rowBuilders[rowIndex]);
        return this;
    }

    public TopMenuBuilder insertInRow(int rowIndex, int slot, ItemStack itemStack) {
        if ((rows > 6 || rows <= 0) || rowIndex > rows - 1)
            throw new IllegalArgumentException(String.format("Invalid row size! Must be inserting from 1-%s", rows));

        return insertInRow(rowIndex, slot, itemStack, null);
    }

    public TopMenuBuilder insertInRow(int rowIndex, int slot, ItemStack itemStack, Consumer<InventoryClickEvent> consumer) {
        if ((rows > 6 || rows <= 0) || rowIndex > rows - 1)
            throw new IllegalArgumentException(String.format("Invalid row size! Must be inserting from 1-%s", rows));

        this.rowBuilders[rowIndex].setSlot(slot, itemStack, consumer);
        return this;
    }

    public Component getTitle() {
        return title;
    }

    public List<Runnable> getOpenActions() {
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
