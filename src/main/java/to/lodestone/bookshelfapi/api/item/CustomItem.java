package to.lodestone.bookshelfapi.api.item;

import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import to.lodestone.bookshelfapi.BookshelfAPI;

public abstract class CustomItem {

    protected boolean isEnchantable = false;
    protected boolean isCombinable = false;

    public abstract String id();

    public boolean isEnchantable() {
        return isEnchantable;
    }

    public boolean isCombinable() {
        return isCombinable;
    }

    public final ItemBuilder getBuilder() throws ClassNotFoundException {
        if (BookshelfAPI.getApi() == null) throw new ClassNotFoundException("Please install Bookshelf to use this method!");

        return BookshelfAPI.getApi().getItemManager().getItemBuilderById(id());
    }

    public void onUnheld(Player player, PlayerItemHeldEvent event, ItemStack item) {}

    public void onShift(Player player, PlayerToggleSneakEvent event, ItemStack item) {}

    public void onHeld(Player player, PlayerItemHeldEvent event, ItemStack item) {}

    public void onInteract(Player player, PlayerInteractEvent event, ItemStack item) {}

    public void onRightInteract(Player player, PlayerInteractEvent event, ItemStack item) {}

    public void onLeftInteract(Player player, PlayerInteractEvent event, ItemStack item) {}

    public void onBlockBreak(Player player, BlockBreakEvent event, ItemStack item) {}
    public void onBlockPlace(Player player, BlockPlaceEvent event, ItemStack item) {}

    public void onFish(Player player, PlayerFishEvent event, ItemStack item) {}

    public void onHurt(Player player, EntityDamageByEntityEvent event, ItemStack item) {}

    public void onKill(Player player, EntityDeathEvent event, ItemStack item) {}

    public void onHarvest(Player player, PlayerHarvestBlockEvent event, ItemStack item) {}

    public void onShoot(Player player, EntityShootBowEvent event, ItemStack item) {}

    public void onInventoryClick(Player player, InventoryClickEvent event, ItemStack item) {}
    public void onInventoryPlace(Player player, InventoryClickEvent event, ItemStack item) {}

    public void onInventoryHotbar(Player player, InventoryClickEvent event, ItemStack item) {}

    public void onDrop(Player player, PlayerDropItemEvent event, ItemStack item) {}

    public void onOffhand(Player player, PlayerSwapHandItemsEvent event, ItemStack item) {}

    public abstract void builder(ItemBuilder paramItemBuilder);
}
