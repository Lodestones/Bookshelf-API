package gg.lode.bookshelfapi.api.manager;

import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.List;

public interface IKitManager {

    /**
     * Returns a list of all available kit IDs.
     */
    List<String> getKitIds();

    /**
     * Checks if a kit with the given ID exists.
     */
    boolean kitExists(String kitId);

    /**
     * Saves the player's current state (inventory, health, food, XP, potion effects, etc.) as a kit.
     *
     * @param player       The player whose state to save.
     * @param kitId        The ID for the kit.
     * @param saveLocation Whether to also save the player's current location.
     */
    void saveKit(Player player, String kitId, boolean saveLocation) throws IOException;

    /**
     * Applies a kit to a player by its ID.
     * This restores inventory, health, food, XP, potion effects, and optionally location.
     *
     * @param player The player to apply the kit to.
     * @param kitId  The ID of the kit to apply.
     * @return true if the kit was found and applied, false if the kit does not exist.
     */
    boolean applyKit(Player player, String kitId);

    /**
     * Deletes a kit by its ID.
     *
     * @return true if the kit was deleted, false if it did not exist.
     */
    boolean deleteKit(String kitId);

    /**
     * Renames a kit's display name.
     *
     * @param kitId   The ID of the kit to rename.
     * @param newName The new display name for the kit.
     * @return true if the kit was found and renamed, false if it does not exist.
     */
    boolean renameKit(String kitId, String newName);
}
