package gg.lode.bookshelfapi.api.manager;

import gg.lode.bookshelfapi.api.item.CustomItem;
import gg.lode.bookshelfapi.api.item.ItemBuilder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ICustomItemManager {

    void register(CustomItem...i);

    @Nullable
    ItemStack getItemStackByClass(Class<? extends CustomItem> itemClass);

    @Nullable
    CustomItem getItemByClass(Class<? extends CustomItem> customItem);

    boolean isCustomItem(ItemStack itemStack, Class<? extends CustomItem> itemClass);

    CustomItem getItemById(String id);

    CustomItem getItemByItemStack(ItemStack itemStack);

    String getIdByCustomItem(CustomItem customItem);

    boolean isCustomItem(ItemStack itemStack, String id);

    String getIdByItemStack(ItemStack itemStack);

    @Nullable
    ItemStack getItemStackById(String id);

    ItemBuilder getItemBuilderById(String id);

    ItemBuilder getItemBuilderByClass(Class<? extends CustomItem> itemClass);

    List<CustomItem> getItems();

    void unregister(String id);


}
