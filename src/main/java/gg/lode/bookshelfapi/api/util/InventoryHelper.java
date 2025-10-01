package gg.lode.bookshelfapi.api.util;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class InventoryHelper {

    public static byte @Nullable [] serialize(ItemStack[] contents) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            dataOutput.writeInt(contents.length);
            for (ItemStack item : contents.clone()) {
                dataOutput.writeObject(item);
            }

            dataOutput.close();
            return outputStream.toByteArray();
        } catch (IOException e) {
            return null;
        }
    }

    public static void deserialize(byte @Nullable [] serializedInventory, Inventory inventory) {
        if (serializedInventory != null) {
            try {
                ByteArrayInputStream inputStream = new ByteArrayInputStream(serializedInventory);
                BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

                int size = dataInput.readInt();

                for (int i = 0; i < size; i++) {
                    ItemStack item = (ItemStack) dataInput.readObject();
                    inventory.setItem(i, item);
                }

                dataInput.close();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public static byte[] deserialize(byte @Nullable [] serializedInventory) {
        if (serializedInventory != null) {
            Inventory inventory = Bukkit.createInventory(null, 54);
            try {
                ByteArrayInputStream inputStream = new ByteArrayInputStream(serializedInventory);
                BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

                int size = dataInput.readInt();

                for (int i = 0; i < size; i++) {
                    ItemStack item = (ItemStack) dataInput.readObject();
                    inventory.setItem(i, item);
                }

                dataInput.close();
                return serialize(inventory.getContents());
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        return new byte[]{};
    }

}
