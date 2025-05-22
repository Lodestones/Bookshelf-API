package gg.lode.bookshelfapi.api.manager.impl;

import gg.lode.bookshelfapi.api.item.CustomItem;
import gg.lode.bookshelfapi.api.item.ItemBuilder;
import gg.lode.bookshelfapi.api.manager.ICustomItemManager;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class APICustomItemManager implements ICustomItemManager {
    private final ConcurrentHashMap<String, CustomItem> items = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<CustomItem, String> mirroredItems = new ConcurrentHashMap<>();
    private final Plugin plugin;
    private final NamespacedKey itemKey;

    public APICustomItemManager(Plugin plugin) {
        this.plugin = plugin;
        this.itemKey = new NamespacedKey(plugin, "custom_item");
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
} 