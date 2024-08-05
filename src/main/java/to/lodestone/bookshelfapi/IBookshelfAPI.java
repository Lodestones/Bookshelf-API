package to.lodestone.bookshelfapi;

import to.lodestone.bookshelfapi.api.item.ICustomItemManager;
import to.lodestone.bookshelfapi.api.manager.IChatManager;
import to.lodestone.bookshelfapi.api.manager.ICooldownManager;
import to.lodestone.bookshelfapi.api.menu.IMenuManager;

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

}
