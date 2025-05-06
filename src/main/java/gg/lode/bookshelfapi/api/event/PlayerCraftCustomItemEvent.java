package gg.lode.bookshelfapi.api.event;

import gg.lode.bookshelfapi.api.item.CustomItem;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.inventory.ItemStack;

public class PlayerCraftCustomItemEvent extends BaseEvent implements Cancellable {
    private final Player player;
    private final CustomItem customItem;
    private final ItemStack itemStack;
    private boolean isCancelled;

    public PlayerCraftCustomItemEvent(Player player, CustomItem customItem, ItemStack itemStack) {
        this.player = player;
        this.customItem = customItem;
        this.itemStack = itemStack;
    }

    public Player getPlayer() {
        return player;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public CustomItem getCustomItem() {
        return customItem;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        isCancelled = b;
    }
}
