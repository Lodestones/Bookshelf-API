package to.lodestone.bookshelfapi.api.menu.build;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

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
