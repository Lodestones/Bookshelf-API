package to.lodestone.bookshelfapi;

/**
 * Official API of the Bookshelf Plugin
 * This interface allows access to certain internals of the teams plugin.
 *
 * @author John Aquino
 */
public class BookshelfAPI {

    private static IBookshelfAPI api;

    /**
     * Internal use of the API for Bookshelf to use.
     * DO NOT TOUCH!!
     * @param api {@link IBookshelfAPI}
     */
    public static void setApi(IBookshelfAPI api) {
        BookshelfAPI.api = api;
    }

    /**
     * Retrieves the API that Bookshelf uses.
     */
    public static IBookshelfAPI getApi() {
        return api;
    }

}
