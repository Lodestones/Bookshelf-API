package gg.lode.bookshelfapi;

import gg.lode.bookshelfapi.api.manager.*;
import org.bukkit.plugin.java.JavaPlugin;

public interface IBookshelfAPI {

    /**
     * Initialize the API when used as a plugin.
     * This is called by the Bookshelf plugin.
     * 
     * @param plugin The JavaPlugin instance
     */
    void initialize(JavaPlugin plugin);

    /**
     * Initialize the API in standalone mode.
     * This is called when Bookshelf is shaded into another project.
     * 
     * @param plugin The JavaPlugin instance of the host plugin
     */
    void initializeStandalone(JavaPlugin plugin);

    /**
     * Fetches the Menu Manager.
     * @return The {@link IMenuManager} instance.
     */
    IMenuManager getMenuManager();

    /**
     * Fetches the Cooldown Manager.
     * @return The {@link ICooldownManager} instance.
     */
    ICooldownManager getCooldownManager();

    /**
     * Fetches the Chat Manager.
     * @return The {@link IChatManager} instance.
     */
    IChatManager getChatManager();

    /**
     * Fetches the Item Manager.
     * @return The {@link ICustomItemManager} instance.
     */
    ICustomItemManager getItemManager();

    /**
     * Fetches the Game Manager.
     * @return The {@link IGameManager} instance.
     */
    IGameManager getGameManager();

    IPlayerManager getPlayerManager();
    IVanishManager getVanishManager();

}
