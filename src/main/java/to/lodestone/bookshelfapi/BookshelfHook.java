package to.lodestone.bookshelfapi;

import org.bukkit.plugin.java.JavaPlugin;
import to.lodestone.bookshelfapi.api.BookshelfProvider;

/**
 * The main class to hook into the Bookshelf API.
 * <p>
 * Example usage:
 * <pre>{@code
 * public class MainPlugin extends JavaPlugin {
 *      private BookshelfHook bookshelfHook; // Declare the Bookshelf Variable.
 *
 *      @Override
 *      public void onEnable() {
 *          bookshelfHook = new BookshelfHook(this); // Hook into the Bookshelf API.
 *      }
 *
 *      public IBookshelfAPI bookshelf() {
 *          return bookshelfHook.api(); // Retrieve the Bookshelf API from the Hook.
 *      }
 * }
 * }</pre>
 * </p>
 * @author John Aquino
 */
public class BookshelfHook {
    private final IBookshelfAPI api;

    /**
     * Constructs a BookshelfHook instance and hooks into the Bookshelf API using the provided plugin.
     *
     * @param plugin The JavaPlugin instance to initialize the Bookshelf API.
     */
    public BookshelfHook(JavaPlugin plugin) {
        BookshelfProvider provider = new BookshelfProvider(plugin);
        this.api = provider.service();
    }

    /**
     * Retrieves the instance of the Bookshelf API.
     *
     * @return The instance of the Bookshelf API.
     */
    public IBookshelfAPI api() {
        return this.api;
    }
}