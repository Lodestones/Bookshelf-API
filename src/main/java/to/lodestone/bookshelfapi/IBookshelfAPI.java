package to.lodestone.bookshelfapi;

import org.bukkit.entity.Player;
import to.lodestone.bookshelfapi.api.manager.*;

public interface IBookshelfAPI {

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
     * @return The {@link IServerManager} instance.
     */
    IServerManager getServerManager();

    IPlayerManager getPlayerManager();

}
