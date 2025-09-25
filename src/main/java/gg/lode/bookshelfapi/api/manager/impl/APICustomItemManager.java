package gg.lode.bookshelfapi.api.manager.impl;

import gg.lode.bookshelfapi.api.item.CustomItem;
import gg.lode.bookshelfapi.api.item.ItemBuilder;
import gg.lode.bookshelfapi.api.manager.ICustomItemManager;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class APICustomItemManager implements ICustomItemManager, Listener {

    private static final String ID = "custom_item";
    private final ConcurrentHashMap<String, CustomItem> items = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<CustomItem, String> mirroredItems = new ConcurrentHashMap<>();
    private final Plugin plugin;
    private final NamespacedKey itemKey;

    public APICustomItemManager(Plugin plugin) {
        this.plugin = plugin;
        this.itemKey = new NamespacedKey(plugin, ID);

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void register(CustomItem... i) {
        for (CustomItem item : i) {
            items.put(item.id(), item);
            mirroredItems.put(item, item.id());
        }
    }

    @Override
    @Nullable
    public ItemStack getItemStackByClass(Class<? extends CustomItem> itemClass) {
        try {
            CustomItem item = getItemByClass(itemClass);
            if (item == null) return null;
            return item.getBuilder().build();
        } catch (Exception err) {
            err.printStackTrace();
            return null;
        }
    }

    @Override
    @Nullable
    public CustomItem getItemByClass(Class<? extends CustomItem> customItem) {
        return items.values().stream().filter(i -> i.getClass() == customItem).findFirst().orElse(null);
    }

    @Override
    public boolean isCustomItem(ItemStack itemStack, Class<? extends CustomItem> itemClass) {
        return getItemByItemStack(itemStack) != null && Objects.requireNonNull(getItemByItemStack(itemStack)).getClass().hashCode() == itemClass.hashCode();
    }

    @Override
    public CustomItem getItemById(String id) {
        if (id == null) return null;
        return items.get(id);
    }

    @Override
    public CustomItem getItemByItemStack(ItemStack itemStack) {
        if (isEmpty(itemStack)) return null;
        final String id = getIdByItemStack(itemStack);
        return getItemById(id);
    }

    @Override
    public String getIdByCustomItem(CustomItem customItem) {
        return mirroredItems.get(customItem);
    }

    @Override
    public boolean isCustomItem(ItemStack itemStack, String id) {
        CustomItem customItem = getItemByItemStack(itemStack);
        return customItem != null && customItem.id().equalsIgnoreCase(id);
    }

    @Override
    public String getIdByItemStack(ItemStack itemStack) {
        if (isEmpty(itemStack) || !hasData(itemStack)) return null;
        return itemStack.getItemMeta().getPersistentDataContainer().get(itemKey, org.bukkit.persistence.PersistentDataType.STRING);
    }

    private boolean isEmpty(ItemStack item) {
        return (item == null || item.getType().isAir());
    }

    @Override
    @Nullable
    public ItemStack getItemStackById(String id) {
        ItemBuilder builder = getItemBuilderById(id);
        return builder == null ? null : builder.build();
    }

    @Override
    public ItemBuilder getItemBuilderById(String id) {
        CustomItem item = getItemById(id);
        if (item == null) return null;

        ItemBuilder builder = new ItemBuilder().tag(itemKey, item.id());
        item.builder(builder);
        return builder;
    }

    @Override
    public ItemBuilder getItemBuilderByClass(Class<? extends CustomItem> itemClass) {
        return getItemBuilderById(Objects.requireNonNull(getItemByClass(itemClass)).id());
    }

    private boolean hasData(ItemStack itemStack) {
        return (itemStack.hasItemMeta() && !itemStack.getItemMeta().getPersistentDataContainer().isEmpty());
    }

    @Override
    public List<CustomItem> getItems() {
        return new ArrayList<>(items.values());
    }

    @Override
    public void unregister(String id) {
        CustomItem customItem = items.remove(id);
        if (customItem != null) mirroredItems.remove(customItem);
    }

    @EventHandler
    public void onAnvil(PrepareAnvilEvent event) {
        CustomItem CustomItem = getItemByItemStack(event.getInventory().getFirstItem());
        if (CustomItem != null && !CustomItem.isCombinable())
            event.setResult(null);
    }

    @EventHandler
    public void on(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        CustomItem customItem = getItemByItemStack(event.getBow());

        if (customItem != null)
            customItem.onShoot(player, event, event.getBow());
    }

    @EventHandler
    public void onEnchant(PrepareItemEnchantEvent event) {
        CustomItem customItem = getItemByItemStack(event.getItem());
        if (customItem != null && !customItem.isEnchantable())
            event.setCancelled(true);
    }

    @EventHandler
    public void onHeld(PlayerItemHeldEvent event) {
        ItemStack unheldItem = event.getPlayer().getInventory().getItem(event.getPreviousSlot());
        ItemStack heldItem = event.getPlayer().getInventory().getItem(event.getNewSlot());
        Player player = event.getPlayer();

        CustomItem customUnheldItem = getItemByItemStack(unheldItem);
        if (customUnheldItem != null)
            customUnheldItem.onUnheld(player, event, unheldItem);

        CustomItem customItem = getItemByItemStack(heldItem);
        if (customItem != null)
            customItem.onHeld(player, event, heldItem);
    }

    @EventHandler
    public void onShift(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        ItemStack heldItem = player.getInventory().getItemInMainHand().getType().isEmpty() ? player.getInventory().getItemInOffHand() : player.getInventory().getItemInMainHand();

        CustomItem customItem = getItemByItemStack(heldItem);
        if (customItem != null)
            customItem.onShift(player, event, heldItem);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        ItemStack heldItem = event.getItem();
        Player player = event.getPlayer();
        Action action = event.getAction();
        if (heldItem == null) return;

        CustomItem customItem = getItemByItemStack(heldItem);
        if (customItem != null) {
            customItem.onInteract(player, event, heldItem);
            if (action.equals(Action.RIGHT_CLICK_BLOCK) || action.equals(Action.RIGHT_CLICK_AIR))
                customItem.onRightInteract(player, event, heldItem);
            if (action.equals(Action.LEFT_CLICK_BLOCK) || action.equals(Action.LEFT_CLICK_AIR))
                customItem.onLeftInteract(player, event, heldItem);
        }

    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        ItemStack heldItem = event.getPlayer().getInventory().getItemInMainHand();
        Player player = event.getPlayer();

        CustomItem customItem = getItemByItemStack(heldItem);
        if (customItem != null)
            customItem.onBlockBreak(player, event, heldItem);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        ItemStack heldItem = event.getPlayer().getInventory().getItemInMainHand();
        Player player = event.getPlayer();

        CustomItem customItem = getItemByItemStack(heldItem);
        if (customItem != null)
            customItem.onBlockPlace(player, event, heldItem);
    }

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        ItemStack heldItem = player.getInventory().getItemInMainHand().getType().isAir() ? player.getInventory().getItemInOffHand() : player.getInventory().getItemInMainHand();

        CustomItem customItem = getItemByItemStack(heldItem);
        if (customItem != null)
            customItem.onFish(player, event, heldItem);
    }

    @EventHandler
    public void onDeath(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        ItemStack heldItem = player.getInventory().getItemInMainHand().getType().isAir() ? player.getInventory().getItemInOffHand() : player.getInventory().getItemInMainHand();

        CustomItem customItem = getItemByItemStack(heldItem);
        if (customItem != null)
            customItem.onHurt(player, event, heldItem);
    }

    @EventHandler
    public void onKill(EntityDeathEvent event) {
        Player player = event.getEntity().getKiller();
        if (player == null) return;
        ItemStack heldItem = player.getInventory().getItemInMainHand().getType().isAir() ? player.getInventory().getItemInOffHand() : player.getInventory().getItemInMainHand();

        CustomItem customItem = getItemByItemStack(heldItem);
        if (customItem != null)
            customItem.onKill(player, event, heldItem);
    }

    @EventHandler
    public void on(PlayerHarvestBlockEvent event) {
        Player player = event.getPlayer();
        ItemStack heldItem = player.getInventory().getItemInMainHand().getType().isAir() ? player.getInventory().getItemInOffHand() : player.getInventory().getItemInMainHand();

        CustomItem customItem = getItemByItemStack(heldItem);
        if (customItem != null)
            customItem.onHarvest(player, event, heldItem);
    }

    @EventHandler
    public void on(PlayerDropItemEvent event) {
        ItemStack heldItem = event.getItemDrop().getItemStack();
        CustomItem customItem = getItemByItemStack(heldItem);
        if (customItem != null)
            customItem.onDrop(event.getPlayer(), event, heldItem);
    }

    @EventHandler
    public void on(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        @Nullable ItemStack mainHand = event.getMainHandItem();
        @Nullable ItemStack offHand = event.getOffHandItem();

        CustomItem mainCustomItem = getItemByItemStack(mainHand);
        if (mainCustomItem != null)
            mainCustomItem.onOffhand(player, event, mainHand);

        CustomItem offHandItem = getItemByItemStack(offHand);
        if (offHandItem != null)
            offHandItem.onOffhand(player, event, mainHand);
    }

    @EventHandler
    public void on(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        ItemStack heldItem = event.getCurrentItem();
        if (heldItem != null) {
            CustomItem customItem = getItemByItemStack(heldItem);
            if (customItem != null)
                customItem.onInventoryClick(player, event, heldItem);
        } else {
            heldItem = event.getCursor();
            if (!heldItem.isEmpty()) {
                CustomItem customItem = getItemByItemStack(heldItem);
                if (customItem != null)
                    customItem.onInventoryPlace(player, event, heldItem);
            } else if (event.getHotbarButton() != -1) {
                heldItem = player.getInventory().getItem(event.getHotbarButton());
                CustomItem customItem = getItemByItemStack(heldItem);
                if (customItem != null)
                    customItem.onInventoryHotbar(player, event, heldItem);
            }
        }
    }

} 