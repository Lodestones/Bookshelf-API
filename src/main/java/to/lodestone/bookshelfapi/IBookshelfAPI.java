package to.lodestone.bookshelfapi;

import to.lodestone.bookshelfapi.api.manager.ICooldownManager;
import to.lodestone.bookshelfapi.api.menu.IMenuManager;
import to.lodestone.bookshelfapi.api.internal.Service;

/**
 * Official API of the Bookshelf Plugin
 * This interface allows access to certain internals of the teams plugin.
 *
 * @author John Aquino
 */
public interface IBookshelfAPI extends Service {

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

}
