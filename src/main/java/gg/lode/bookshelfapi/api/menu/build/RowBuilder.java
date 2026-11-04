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
        return setSlot(x, itemStack, consumer, null);
    }

    /**
     * Force this slot to be packet-based. The visual is sent only via packets;
     * the click pipeline is dropped at the network layer so no transfer is
     * possible regardless of TPS lag.
     */
    public RowBuilder setPacketSlot(int x, @NotNull ItemStack itemStack, @Nullable Consumer<InventoryClickEvent> consumer) {
        return setSlot(x, itemStack, consumer, Boolean.TRUE);
    }

    /**
     * Force this slot to be server-side: the real {@link ItemStack} lives in
     * the server inventory and clicks pass through to Bukkit normally so
     * players can grab/drop items here.
     */
    public RowBuilder setServerSlot(int x, @NotNull ItemStack itemStack, @Nullable Consumer<InventoryClickEvent> consumer) {
        return setSlot(x, itemStack, consumer, Boolean.FALSE);
    }

    /**
     * Empty server-side slot — drop zone the player can put items into.
     */
    public RowBuilder setEmptyServerSlot(int x, @Nullable Consumer<InventoryClickEvent> consumer) {
        if (x > this.slots.length - 1 || x < 0)
            throw new ArrayIndexOutOfBoundsException(String.format("\"x\" must be from 0-8 not %s", x));
        this.slots[x] = new Slot(null, consumer, Boolean.FALSE);
        return this;
    }

    /**
     * Empty packet-only slot. Client sees no item; clicks are dropped at the
     * network layer. Useful for hiding bottom-inventory cells without touching
     * the player's real items.
     */
    public RowBuilder setEmptyPacketSlot(int x, @Nullable Consumer<InventoryClickEvent> consumer) {
        if (x > this.slots.length - 1 || x < 0)
            throw new ArrayIndexOutOfBoundsException(String.format("\"x\" must be from 0-8 not %s", x));
        this.slots[x] = new Slot(null, consumer, Boolean.TRUE);
        return this;
    }

    public RowBuilder setSlot(int x, @NotNull ItemStack itemStack, @Nullable Consumer<InventoryClickEvent> consumer, @Nullable Boolean packet) {
        if (x > this.slots.length - 1 || x < 0)
            throw new ArrayIndexOutOfBoundsException(String.format("\"x\" must be from 0-8 not %s", x));

        this.slots[x] = new Slot(itemStack, consumer, packet);
        return this;
    }

    public Slot[] getSlots() {
        return slots;
    }

    /**
     * @param packet null = inherit from {@link TopMenuBuilder#isPacketBased()};
     *               true = always packet; false = always server-side.
     */
    public record Slot(@Nullable ItemStack itemStack,
                       @Nullable Consumer<InventoryClickEvent> event,
                       @Nullable Boolean packet) {
        public static Slot empty() {
            return new Slot(null, null, null);
        }
    }
}
