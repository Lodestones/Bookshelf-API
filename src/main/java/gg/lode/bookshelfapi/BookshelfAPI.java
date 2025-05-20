package gg.lode.bookshelfapi;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Official API of the Bookshelf Plugin
 * This interface allows access to certain internals of the teams plugin.
 *
 * @author John Aquino
 */
public class BookshelfAPI {

    private static IBookshelfAPI api;
    private static boolean initialized = false;

    /**
     * Initialize the Bookshelf API with a plugin instance.
     * This should be called when using Bookshelf as a plugin.
     * 
     * @param plugin The JavaPlugin instance
     * @throws IllegalStateException if the API is already initialized
     */
    public static void initialize(JavaPlugin plugin) {
        if (initialized) {
            throw new IllegalStateException("Bookshelf API is already initialized!");
        }
        if (api == null) {
            throw new IllegalStateException("Bookshelf API implementation not found! Make sure Bookshelf plugin is installed.");
        }
        api.initialize(plugin);
        initialized = true;
    }

    /**
     * Initialize the Bookshelf API in standalone mode.
     * This should be called when shading Bookshelf into another project.
     * 
     * @param plugin The JavaPlugin instance of the host plugin
     * @throws IllegalStateException if the API is already initialized
     */
    public static void initializeStandalone(JavaPlugin plugin) {
        if (initialized) {
            throw new IllegalStateException("Bookshelf API is already initialized!");
        }
        if (api == null) {
            throw new IllegalStateException("Bookshelf API implementation not found! Make sure Bookshelf plugin is installed.");
        }
        api.initializeStandalone(plugin);
        initialized = true;
    }

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
     * @throws IllegalStateException if the API is not initialized
     */
    public static IBookshelfAPI getApi() {
        if (!initialized) {
            throw new IllegalStateException("Bookshelf API is not initialized! Call BookshelfAPI.initialize() first.");
        }
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
