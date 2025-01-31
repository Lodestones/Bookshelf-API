package to.lodestone.bookshelfapi.api.menu.build;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.function.Consumer;

public class MenuBuilder {
    protected RowBuilder @NotNull[] rowBuilders;

    public MenuBuilder() {
        this.rowBuilders = new RowBuilder[4];
        Arrays.fill(this.rowBuilders, new RowBuilder());
    }

    public MenuBuilder buildRow(int index, Consumer<RowBuilder> consumer) {
        RowBuilder builder = new RowBuilder();
        consumer.accept(builder);
        rowBuilders[index] = builder;
        return this;
    }

    public void setItem(int slot, ItemStack itemStack, @Nullable Consumer<InventoryClickEvent> consumer) {
        int row = slot / 9;
        int column = slot % 9;
        rowBuilders[row].setSlot(column, itemStack, consumer);
    }

    public void setItem(int slot, ItemStack itemStack) {
        int row = slot / 9;
        int column = slot % 9;
        rowBuilders[row].setSlot(column, itemStack, null);
    }

    public void set(int row, int column, ItemStack itemStack, @Nullable Consumer<InventoryClickEvent> consumer) {
        rowBuilders[row].setSlot(column, itemStack, consumer);
    }

    public void set(int row, int column, ItemStack itemStack) {
        rowBuilders[row].setSlot(column, itemStack, null);
    }

    public RowBuilder[] getRowBuilders() {
        return rowBuilders;
    }

    public void process(int slot, InventoryClickEvent event) {
        for (int y = 0; y < rowBuilders.length; y++) {
            @NotNull RowBuilder rowBuilder = rowBuilders[y];

            RowBuilder.Slot[] slots = rowBuilder.getSlots();
            for (int x = 0; x < slots.length; x++) {
                @NotNull RowBuilder.Slot builderSlot = slots[x];
                if (builderSlot.event() == null) continue;

                if (slot == (y * 9) + x) {
                    builderSlot.event().accept(event);
                    return;
                }
            }
        }
    }
}
