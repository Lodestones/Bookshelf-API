package gg.lode.bookshelfapi;

import org.bukkit.Bukkit;

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

    public static boolean is1_21() {
        return Bukkit.getServer().getMinecraftVersion().startsWith("1.21");
    }

    public static boolean isHigher1_21_4() {
        if (is1_21()) {
            String version = Bukkit.getServer().getMinecraftVersion();
            String[] split = version.split("\\.");
            if (split.length >= 3) {
                int minor = Integer.parseInt(split[1]);
                int patch = Integer.parseInt(split[2]);
                return minor > 21 || (minor == 21 && patch >= 4);
            }
        }

        return false;
    }

}
