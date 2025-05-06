package gg.lode.bookshelfapi.api.menu.build;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.function.Consumer;

public class RowBuilder {

    private final Slot[] slots;

    public RowBuilder() {
        this.slots = new Slot[9];
        Arrays.fill(this.slots, Slot.empty());
    }

    public RowBuilder setSlot(int x, ItemStack itemStack) {
        return setSlot(x, itemStack, null);
    }

    public RowBuilder setSlot(int x, @NotNull ItemStack itemStack, @Nullable Consumer<InventoryClickEvent> consumer) {
        if (x > this.slots.length - 1 || x < 0)
            throw new ArrayIndexOutOfBoundsException(String.format("\"x\" must be from 0-8 not %s", x));

        this.slots[x] = new Slot(itemStack, consumer);
        return this;
    }

    public Slot[] getSlots() {
        return slots;
    }

    public record Slot(@Nullable ItemStack itemStack, @Nullable Consumer<InventoryClickEvent> event) {
        public static Slot empty() {
            return new Slot(null, null);
        }
    }
}
