package to.lodestone.bookshelfapi.api.item;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IItemManager {

    void register(CustomItem ...i);

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

    List<CustomItem> getItems();


}
